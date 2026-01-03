---
description: Run type checking, linting, and build verification before pushing
---

# Pre-Flight Check


CI/CDTestingBuildQualityDownloadCopy Workflow---

1. \*\*Type Check\*\*:
 - Ensure there are no TypeScript errors.
 // turbo
 - Run `npx tsc --noEmit`

2. \*\*Lint Check\*\*:
 - Verify code quality rules.
 // turbo
 - Run `npm run lint`

3. \*\*Build Verification\*\*:
 - Ensure the project builds successfully for production.
 // turbo
 - Run `npm run build`

4. \*\*Pro Tips\*\*:
 - Use a pre-push git hook (using `husky`) to run this automatically.
 - If the build fails locally, it will definitely fail in production.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `run-pre-flight-check-type-lint-build.md`
4. In Antigravity, type `/run_pre_flight_check_type_lint_build` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Setup Husky Git Hooks

GitAutomationQuality+1--- description: Automate code quality checks with pre-commit and pre-push hooks --- 1. \*\*Install Husky\*\*: - Install husky and lint-staged. // turbo - Run `npm install --save-dev husky lint-staged` 2. \*\*Initialize Husky\*\*: - Set up git hooks. // turbo - Run `npx husky init` 3. \*...](/workflows/local-dev/setup-husky-git-hooks-pre-commit-linting)[### Fix Lint Errors

LintingESLintPrettier+1--- description: Automatically fix linting and formatting issues across the project --- 1. \*\*Run ESLint Fix\*\*: - Attempt to automatically fix all fixable ESLint errors. // turbo - Run `npm run lint -- --fix` 2. \*\*Run Prettier\*\*: - Format all files in the project to ensure consistent st...](/workflows/local-dev/fix-eslint-prettier-linting-errors-automatically)[### Setup Environment Variables Per Branch

DevOpsEnvironmentVercel+1--- description: Configure different env vars for dev, staging, and production --- 1. \*\*Local Development (.env.local)\*\*: - Create `.env.local` for local overrides (never commit this). ```bash # .env.local DATABASE\_URL=postgresql://localhost:5432/mydb API\_URL=http://localhost:3001 ...](/workflows/local-dev/setup-environment-variables-per-branch-vercel)
## Recommended Rules

[View more rules →](/rules)[### 🚀 DevOps & CI/CD Agent - Pipeline Expert

Agentic AIDevOpsCI/CDYou are an expert DevOps and CI/CD agent specialized in designing and implementing robust deployment pipelines and infrastructure. Apply systematic re...](/rules/agentic-ai/devops-cicd-agent)[### Python Testing Best Practices

PythonTestingQuality AssuranceYou are an expert in Python testing with pytest and testing best practices. Key Principles: - Write tests before or alongside code (TDD/BDD) - Aim fo...](/rules/python/python-testing-best-practices)[### Next.js Testing Strategies Expert

Next.jsTestingJestYou are an expert in Next.js testing strategies and best practices. Key Principles: - Test Server and Client Components differently - Use Jest for un...](/rules/nextjs/nextjs-testing-strategies)