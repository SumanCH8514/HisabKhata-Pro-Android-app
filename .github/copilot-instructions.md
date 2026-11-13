# GitHub Copilot & AI Instructions for HisabKhata PRO

## STRICT SECURITY RULE: DO NOT HARDCODE SECRETS OR KEYS
- **NEVER** insert literal API keys (e.g. Google API keys starting with `AIzaSy`), passwords, tokens, or private credentials in any code or build configuration.
- **Dynamic Retrieval**:
  - For local runs: load from `local.properties`.
  - For CI/CD builds: read from `System.getenv(...)`.
  - For Google Services: dynamically read from gitignored `app/google-services.json`.
- **Git Hygiene**:
  - Never track or un-ignore `local.properties`, `google-services.json`, or keystore files.
