# Security Policy - HisabKhata PRO

## Secret & Key Management Rules

1. **No Hardcoded Secrets Policy**:
   - Hardcoding any API keys, client secrets, tokens, or credentials in source code or build files is strictly prohibited.
   - Any secret found in git commits will trigger automated GitHub secret scanning alerts and must be revoked immediately.

2. **Configuration & Credentials Hierarchy**:
   - **Local Development**: Define keys in `local.properties` (e.g. `FIREBASE_API_KEY=your_key_here`). This file is gitignored.
   - **CI/CD Builds**: Use encrypted GitHub Repository Secrets injected via environment variables.
   - **Google Services**: Store `google-services.json` in `app/` locally without committing it to git.

3. **Automated Enforcement**:
   - The Gradle build system enforces automated pre-build secret scanning (`verifyNoHardcodedSecrets`) to prevent accidental commits of keys.

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please report it privately via GitHub Security Advisories or by contacting `sumanonline@example.com`.
