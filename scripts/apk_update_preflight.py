#!/usr/bin/env python3
"""Read-only QA package/signature/version gate, not a save-retention test."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys


class Rejected(ValueError):
    pass


def digest(path):
    value = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def load_policy(path):
    try:
        policy = json.loads(path.read_text(encoding="utf-8"))
        valid = (policy["schema_version"] == 1 and policy["channel"] == "qa"
                 and isinstance(policy["application_id"], str)
                 and re.fullmatch(r"[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+\.qa", policy["application_id"])
                 and re.fullmatch(r"[0-9a-f]{64}", policy["signer_sha256"])
                 and re.fullmatch(r"[0-9a-f]{64}", policy["apk_sha256"])
                 and type(policy["version_code"]) is int and 0 < policy["version_code"] <= 2100000000
                 and type(policy["minimum_sdk"]) is int and 1 <= policy["minimum_sdk"] <= 1000)
        if not valid:
            raise ValueError()
        return policy
    except (OSError, ValueError, KeyError, TypeError):
        raise Rejected("The QA baseline policy is missing, malformed, or unsupported.") from None


def run_tool(command, purpose):
    try:
        return subprocess.run(command, capture_output=True, text=True, check=True, timeout=60).stdout
    except (OSError, subprocess.SubprocessError, UnicodeError):
        raise Rejected(purpose + " failed. No update has been accepted.") from None


def inspect(args):
    policy = load_policy(args.policy)
    if not re.fullmatch(r"[0-9a-f]{40}", args.source_commit):
        raise Rejected("An exact lowercase 40-character source commit is required.")
    if not 0 < args.expected_version_code <= 2100000000:
        raise Rejected("The requested version code is outside Android's supported range.")
    if args.expected_version_code <= policy["version_code"]:
        raise Rejected("The requested build must be newer than the last verified QA build.")
    if not args.apk.is_file() or not 0 < args.apk.stat().st_size <= 512 * 1024 * 1024:
        raise Rejected("The candidate APK is missing, empty, or too large to inspect safely.")
    apk = args.apk.resolve()
    before = digest(apk)
    certificate = run_tool([str(args.apksigner), "verify", "--min-sdk-version",
                            str(policy["minimum_sdk"]), "--print-certs", str(apk)],
                           "APK signature verification")
    signers = re.findall(r"^Signer #[0-9]+ certificate SHA-256 digest: ([0-9a-fA-F:]+)\s*$",
                         certificate, re.MULTILINE)
    if len(signers) != 1:
        raise Rejected("Exactly one signer with a certificate SHA-256 digest is required.")
    signer = signers[0].replace(":", "").lower()
    if signer != policy["signer_sha256"]:
        raise Rejected("The signing certificate differs from the installed QA identity. Do not uninstall to work around it.")
    metadata = run_tool([str(args.aapt), "dump", "badging", str(apk)], "APK metadata inspection")
    packages = re.findall(r"^package: (.+)$", metadata, re.MULTILINE)
    sdk = re.findall(r"^sdkVersion:'([0-9]+)'\s*$", metadata, re.MULTILINE)
    if len(packages) != 1 or len(sdk) != 1:
        raise Rejected("The APK metadata does not contain one package and minimum SDK.")
    fields = dict(re.findall(r"([A-Za-z][A-Za-z0-9]*)='([^']*)'", packages[0]))
    if not {"name", "versionCode", "versionName"} <= fields.keys() or not re.fullmatch(r"[1-9][0-9]*", fields["versionCode"]):
        raise Rejected("The APK metadata has missing or invalid version fields.")
    if fields["name"] != policy["application_id"]:
        raise Rejected("The application ID differs from the installed QA package.")
    version = int(fields["versionCode"])
    if version != args.expected_version_code:
        raise Rejected("The APK version does not match the requested build number.")
    if version <= policy["version_code"]:
        raise Rejected("The APK must be newer than the last verified QA build.")
    if int(sdk[0]) != policy["minimum_sdk"]:
        raise Rejected("The minimum SDK changed; review device compatibility before accepting this update.")
    if digest(apk) != before:
        raise Rejected("The candidate APK changed during inspection.")
    return {
        "receipt_schema": 1,
        "status": "update_identity_compatible",
        "application_id": fields["name"],
        "version_code": version,
        "version_name": fields["versionName"],
        "minimum_sdk": int(sdk[0]),
        "signer_sha256": signer,
        "apk_filename": apk.name,
        "apk_sha256": before,
        "source_commit": args.source_commit,
        "baseline_version_code": policy["version_code"],
        "baseline_apk_sha256": policy["apk_sha256"],
        "install_over_tested": False,
        "save_retention_tested": False,
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apk", required=True, type=Path)
    parser.add_argument("--policy", required=True, type=Path)
    parser.add_argument("--aapt", required=True, type=Path)
    parser.add_argument("--apksigner", required=True, type=Path)
    parser.add_argument("--expected-version-code", required=True, type=int)
    parser.add_argument("--source-commit", required=True)
    args = parser.parse_args()
    try:
        receipt = inspect(args)
    except (Rejected, OSError) as error:
        print("Update rejected: " + str(error), file=sys.stderr)
        return 1
    print(json.dumps(receipt, indent=2, sort_keys=True))
    return 0


if __name__ == "__main__":
    sys.exit(main())
