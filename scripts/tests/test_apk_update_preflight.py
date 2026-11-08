"""Exercise the release decision; Android executables are the only test doubles.

These tests do not claim a fake APK has a real Android signature. Native-tool and
install-over verification are separate acceptance checks on an actual build.
"""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts" / "apk_update_preflight.py"
POLICY = ROOT / "distribution" / "qa-update-baseline.json"
CERT = "949731a60cb187405282efc8233d6e68ab5b46e9bf6765f70aa2b60dcc543b2c"
SOURCE = "a" * 40


class ApkUpdatePreflightTest(unittest.TestCase):
    def setUp(self):
        self.assertTrue(SCRIPT.is_file(), "The APK update preflight command has not been implemented")
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.folder = Path(self.temp.name)
        self.apk = self.folder / "candidate with spaces.apk"
        self.apk.write_bytes(b"release-decision-test-only\x00\x01")
        self.original_bytes = self.apk.read_bytes()
        self.policy = self.folder / "policy.json"
        self.policy.write_text(POLICY.read_text())
        self.aapt = self.folder / "aapt"
        self.signer = self.folder / "apksigner"
        self.output = {
            "aapt": "package: name='com.playereden.sudokutogether.qa' versionCode='1001' versionName='0.11.0-dev3-qa.1001'\nsdkVersion:'26'\ntargetSdkVersion:'35'\napplication-label:'GridMates: Sudoku QA'\n",
            "apksigner": "Verifies\nVerified using v2 scheme (APK Signature Scheme v2): true\nSigner #1 certificate DN: CN=Android Debug\nSigner #1 certificate SHA-256 digest: " + CERT + "\nSigner #1 certificate SHA-1 digest: " + "1" * 40 + "\n"
        }
        self.codes = {"aapt": 0, "apksigner": 0}

    def tool(self, name):
        expected = (["dump", "badging", str(self.apk)] if name == "aapt" else
                    ["verify", "--min-sdk-version", "26", "--print-certs", str(self.apk)])
        content = ("#!" + sys.executable + "\nimport sys\n"
                   + "assert sys.argv[1:] == " + repr(expected) + ", sys.argv[1:]\n"
                   + "sys.stdout.write(" + repr(self.output[name]) + ")\n"
                   + "sys.exit(" + repr(self.codes[name]) + ")\n")
        path = self.folder / name
        path.write_text(content)
        path.chmod(0o700)
        return str(path)

    def run_check(self, code="1001", source=SOURCE):
        command = [sys.executable, str(SCRIPT), "--apk", str(self.apk),
                   "--policy", str(self.policy), "--aapt", self.tool("aapt"),
                   "--apksigner", self.tool("apksigner"),
                   "--expected-version-code", code, "--source-commit", source]
        result = subprocess.run(command, capture_output=True, text=True, timeout=15)
        self.assertEqual(self.original_bytes, self.apk.read_bytes(), "Preflight must never modify the APK")
        return result

    def refused(self, result, reason):
        self.assertNotEqual(0, result.returncode)
        self.assertEqual("", result.stdout, "A rejected candidate must not print an acceptance receipt")
        self.assertIn(reason, result.stderr.lower())

    def test_accepts_compatible_new_build_and_hashes_actual_input(self):
        result = self.run_check()
        self.assertEqual(0, result.returncode, result.stderr)
        receipt = json.loads(result.stdout)
        self.assertEqual("com.playereden.sudokutogether.qa", receipt["application_id"])
        self.assertEqual(1001, receipt["version_code"])
        self.assertEqual(CERT, receipt["signer_sha256"])
        self.assertEqual(SOURCE, receipt["source_commit"])
        self.assertEqual(hashlib.sha256(self.original_bytes).hexdigest(), receipt["apk_sha256"])
        self.assertFalse(receipt["install_over_tested"], "Identity compatibility alone is not a save-retention test")

    def test_rejects_production_or_other_package(self):
        self.output["aapt"] = self.output["aapt"].replace("sudokutogether.qa", "sudokutogether")
        self.refused(self.run_check(), "application id")

    def test_rejects_new_debug_certificate(self):
        self.output["apksigner"] = self.output["apksigner"].replace(CERT, "0" * 64)
        self.refused(self.run_check(), "certificate")

    def test_rejects_failed_signature_verification_even_with_expected_certificate_text(self):
        self.codes["apksigner"] = 1
        self.refused(self.run_check(), "signature verification")

    def test_rejects_unchanged_and_older_build_numbers(self):
        for code in ["19", "18"]:
            with self.subTest(code=code):
                self.output["aapt"] = self.output["aapt"].replace("versionCode='1001'", "versionCode='" + code + "'").replace("versionCode='19'", "versionCode='" + code + "'")
                self.refused(self.run_check(code), "newer")

    def test_rejects_candidate_not_matching_requested_build(self):
        self.refused(self.run_check("1002"), "requested")

    def test_rejects_multiple_signers(self):
        self.output["apksigner"] += "Signer #2 certificate SHA-256 digest: " + CERT + "\n"
        self.refused(self.run_check(), "one signer")

    def test_rejects_missing_certificate_digest(self):
        self.output["apksigner"] = "Verifies\n"
        self.refused(self.run_check(), "one signer")

    def test_rejects_higher_minimum_sdk_that_drops_existing_supported_phones(self):
        self.output["aapt"] = self.output["aapt"].replace("sdkVersion:'26'", "sdkVersion:'28'")
        self.refused(self.run_check(), "minimum sdk")

    def test_rejects_aapt_failure(self):
        self.codes["aapt"] = 1
        self.refused(self.run_check(), "apk metadata")

    def test_rejects_missing_or_malformed_manifest_metadata(self):
        self.output["aapt"] = "package: name='com.playereden.sudokutogether.qa'\nsdkVersion:'26'\n"
        self.refused(self.run_check(), "metadata")

    def test_rejects_future_policy_schema(self):
        value = json.loads(self.policy.read_text())
        value["schema_version"] = 2
        self.policy.write_text(json.dumps(value))
        self.refused(self.run_check(), "policy")

    def test_rejects_malformed_source_and_requested_code(self):
        self.refused(self.run_check(source="main"), "commit")
        self.refused(self.run_check(code="-1"), "version code")


if __name__ == "__main__":
    unittest.main()
