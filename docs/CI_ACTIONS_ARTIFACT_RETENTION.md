# GitHub Actions artifact storage

- Routine installable QA APK: **5 days**.
- Signed Android APK: **14 days** for install/signature continuity.
- Failed test reports: **2 days**.
- Visual emulator test files: **3 days**.
- Source, commits, puzzle data and local save formats are unaffected.
- Old artifacts remain under their original expiration until manually deleted.

Use [Manage old QA artifacts (manual)](../../actions/workflows/manage-qa-artifact-storage.yml): select **dry-run** to review, then **delete-obsolete** to remove older expendable QA artifacts. This is manual and avoids accidentally removing milestone builds. For important signed releases, preserve the verified APK offline before retention expiry.

Billing caps require the account owner's GitHub Settings → Billing → Budgets and alerts; this repository does not modify them.
