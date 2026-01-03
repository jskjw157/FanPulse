---
description: Safely create a local .env file from .env.example
---

# Generate .env from Example


ConfigEnvironmentSetupDownloadCopy Workflow---

1. \*\*Check for .env.example\*\*:
 - Ensure the example file exists.
 // turbo
 - Run `test -f .env.example && echo "✅ Found .env.example" || echo "❌ .env.example not found"`

2. \*\*Copy to .env.local\*\*:
 - Create your local config without overwriting if it exists (using -n).
 // turbo
 - Run `cp -n .env.example .env.local || echo ".env.local already exists"`

3. \*\*Validate\*\*:
 - Open `.env.local` and replace all placeholder values.
 - Example: `YOUR_API_KEY_HERE` → `abc123...`

4. \*\*Pro Tips\*\*:
 - Always add `.env.local` to your `.gitignore`.
 - Never commit real secrets to `.env.example`.
 - Use `git secret` or Vercel Environment Variables for production secrets.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `generate-local-env-file-from-example.md`
4. In Antigravity, type `/generate_local_env_file_from_example` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Setup Environment Variables Per Branch

DevOpsEnvironmentVercel+1--- description: Configure different env vars for dev, staging, and production --- 1. \*\*Local Development (.env.local)\*\*: - Create `.env.local` for local overrides (never commit this). ```bash # .env.local DATABASE\_URL=postgresql://localhost:5432/mydb API\_URL=http://localhost:3001 ...](/workflows/local-dev/setup-environment-variables-per-branch-vercel)[### Setup Prettier & ESLint from Scratch

ESLintPrettierCode Quality+1--- description: Configure linting and formatting (ESLint 9 Flat Config) --- 1. \*\*Install Dependencies\*\*: - Install ESLint, Prettier, and configs. // turbo - Run `npm install --save-dev eslint @eslint/js typescript-eslint prettier eslint-config-prettier eslint-plugin-react-hooks eslint-plu...](/workflows/local-dev/setup-prettier-eslint-typescript-configuration)[### VS Code Settings Sync

VS CodeDXConfig--- description: Standardize VS Code settings across the team --- 1. \*\*Create settings.json\*\*: - Create `.vscode/settings.json` for workspace-specific settings. // turbo - Run `mkdir -p .vscode && printf '{\n "editor.formatOnSave": true,\n "editor.defaultFormatter": "esbenp.prettier-vsco...](/workflows/local-dev/sync-vscode-settings-extensions-team-consistency)
## Recommended Rules

[View more rules →](/rules)[### Computer Vision (OpenCV, YOLO)

Computer VisionOpenCVYOLOYou are an expert in Computer Vision using OpenCV and YOLO. Key Principles: - Understand image representation (Pixels, Channels, Color Spaces) - Prep...](/rules/ai-machine-learning/computer-vision-opencv-yolo)[### NLP & Transformers (Hugging Face)

NLPTransformersHugging FaceYou are an expert in Natural Language Processing (NLP) using the Hugging Face ecosystem. Key Principles: - Leverage pre-trained Transformer models (B...](/rules/ai-machine-learning/nlp-transformers-huggingface)[### MLOps & Model Deployment

MLOpsDeploymentDevOpsYou are an expert in MLOps (Machine Learning Operations) and Model Deployment. Key Principles: - Treat ML as software (Version Control, CI/CD) - Auto...](/rules/ai-machine-learning/mlops-model-deployment)