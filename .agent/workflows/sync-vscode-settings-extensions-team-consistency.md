---
description: Standardize VS Code settings across the team
---

# VS Code Settings Sync


VS CodeDXConfigDownloadCopy Workflow---

1. \*\*Create settings.json\*\*:
 - Create `.vscode/settings.json` for workspace-specific settings.
 // turbo
 - Run `mkdir -p .vscode && printf '{\n "editor.formatOnSave": true,\n "editor.defaultFormatter": "esbenp.prettier-vscode",\n "editor.codeActionsOnSave": {\n "source.fixAll.eslint": true\n }\n}' > .vscode/settings.json`

2. \*\*Create extensions.json\*\*:
 - Recommend extensions for the team.
 // turbo
 - Run `printf '{\n "recommendations": [\n "dbaeumer.vscode-eslint",\n "esbenp.prettier-vscode",\n "bradlc.vscode-tailwindcss"\n ]\n}' > .vscode/extensions.json`

3. \*\*Pro Tips\*\*:
 - Commit the `.vscode` folder (excluding user-specific files) to git.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `sync-vscode-settings-extensions-team-consistency.md`
4. In Antigravity, type `/sync_vscode_settings_extensions_team_consistency` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Generate .env from Example

ConfigEnvironmentSetup--- description: Safely create a local .env file from .env.example --- 1. \*\*Check for .env.example\*\*: - Ensure the example file exists. // turbo - Run `test -f .env.example && echo "✅ Found .env.example" || echo "❌ .env.example not found"` 2. \*\*Copy to .env.local\*\*: - Create your local...](/workflows/local-dev/generate-local-env-file-from-example)[### Generate TypeScript Types from API

TypeScriptAPICodegen+1--- description: Auto-generate type-safe API client from OpenAPI/Swagger spec --- 1. \*\*Get Your API Schema\*\*: - Most APIs expose OpenAPI spec at `/swagger.json` or `/openapi.json`. - Download it or use the URL directly. 2. \*\*Install openapi-typescript\*\*: - Best tool for generating types. ...](/workflows/local-dev/generate-typescript-types-from-openapi-schema)[### Kill Port 3000

## Recommended Rules

[View more rules →](/rules)[### Google Cloud Platform Expert

GCPGoogle CloudCloudYou are an expert in Google Cloud Platform (GCP) services and architecture. Key Principles: - Leverage Google's global infrastructure - Use managed o...](/rules/devops-infrastructure/google-cloud-platform-expert)[### Monitoring & Observability (Prometheus, Grafana)

MonitoringObservabilityPrometheusYou are an expert in Monitoring and Observability using Prometheus and Grafana. Key Principles: - Monitor the four golden signals (Latency, Traffic, ...](/rules/devops-infrastructure/monitoring-observability-prometheus-grafana)[### GitOps & ArgoCD Expert

GitOpsArgoCDKubernetesYou are an expert in GitOps methodology and ArgoCD. Key Principles: - Git as the single source of truth - Declarative infrastructure and applications...](/rules/devops-infrastructure/gitops-argocd-expert)