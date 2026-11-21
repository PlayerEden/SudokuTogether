# Beta 2 unified 75-cosmetic reviewer — safe integration stage

**Status:** staging only; not an APK, not a release, not an eight-icon binary commit. Base: draft Board PR #104 at commit f38161f783797178887219598f4a69dcd0c2614d. The parent branch contains 35 Player Icons, 6 emblems, 11 boards, 10 inks and 5 number fonts = **67 implemented cosmetics**.

## Goal

Add the eight original Ocean/Woodland candidate images from the previously delivered **GridMates_Beta2_Ocean_Woodland_8Icon_ReviewPack.zip** without changing any of the existing 67 cosmetics. Target after imported PNGs + Kotlin updates + successful Android QA:

- Woodland 17 icons (+ Chipmunk / Beaver / Woodpecker / Skunk)
- Ocean 11 icons (+ Seahorse / Dolphin / Seashell / Clownfish)
- Garden 9, Farm 6, emblems 6, boards 11, inks 10, fonts 5
- **43 Player Icons / 75 total cosmetics** (four new fonts later reach 79)

## What is committed to THIS draft branch

- \`design/beta2-content-pack/ocean-woodland-eight-review/manifest.json\` copied byte-for-byte from art PR #103 (expected IDs, unlock thresholds, 512×512 RGBA, original SHA-256s)
- \`scripts/stage_beta2_last_eight.py\` copied from PR #103. Its default is read-only SHA/size/alpha/collision verification; \`--apply\` imports the exact eight files only after all checks succeed.
- This handoff and a clean parent of the actual boards branch.

**Not yet committed:** the eight original PNG binaries, new Kotlin catalog/drawable IDs, the 75-count tests and Android review workflow. A file manifest alone does not implement the art. Do not merge before those exist, build and test.

## Source integration and expected checks

A full local checkout or Work computer with the original art ZIP can apply the **GridMates_Beta2_Unified75_IntegrationKit.zip** created in the ChatGPT Game Design conversation. It contains a complete 75-source patch (including the original eight binary assets) targeting PR #104.

On a separate working branch based on this staging branch or a rebased latest PR #104:

1. Run the kit's \`apply_unified75.py\` in dry-run mode. It must verify the eight original image hashes and each of the source anchors once, including new 67→75 and 35→43 assertions.
2. Run with \`--apply\`, inspect \`git diff\` and commit only the verified sources and PNGs. Keep the special \`gridmatesBeta2Unified75Preview=true\` test package separate from the public, Farm and Boards packages.
3. Run Gradle \`:core:test :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:assembleDebug\` with the isolated reviewer flag; run Android 16 Collection tests and capture **actual valid nonempty PNG screenshots** of the four new Woodland and Ocean characters.
4. Check portrait appearance, cropping, alpha, gallery order, accessible unlock names and durable entitlement; no two-phone LAN or production signed update is established by debug QA.
5. **Do not merge this PR or publish Beta 2** until all later gates pass: owner phone approval, last four licensed fonts, all 79 cosmetics cohesion audit (#102), V7 Versus phone visual QA, two real phone LAN, and production signer + save-preserving Beta 1→Beta 2 upgrade.

Technical warning discovered in the preceding board workflow: four files of 70 bytes contained \`run-as: unknown package\` error text, despite a green run. Reject anything without the PNG file signature and useful dimensions. Board PR #104 is adding MediaStore export plus strict PNG verification.

The goal is ONE real 75-item Android reviewer when binary art integration is complete, not another false-success text-only PR.
