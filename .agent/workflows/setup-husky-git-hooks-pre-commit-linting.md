---
description: Automate code quality checks with pre-commit and pre-push hooks
---

# Setup Husky Git Hooks


GitAutomationQualityCI/CDDownloadCopy Workflow---

1. \*\*Install Husky\*\*:
 - Install husky and lint-staged.
 // turbo
 - Run `npm install --save-dev husky lint-staged`

2. \*\*Initialize Husky\*\*:
 - Set up git hooks.
 // turbo
 - Run `npx husky init`

3. \*\*Create Pre-Commit Hook\*\*:
 - Run linting on staged files before commit.
 // turbo
 - Run `npx husky add .husky/pre-commit "npx lint-staged"`

4. \*\*Configure lint-staged\*\*:
 - Add to `package.json`.
```
{
     "lint-staged": {
       "*.{ts,tsx}": [
         "eslint --fix",
         "prettier --write"
       ],
       "*.{json,md}": [
         "prettier --write"
       ]
     }
   }
```

5. \*\*Create Pre-Push Hook (Optional)\*\*:
 - Run tests before pushing.
 // turbo
 - Run `npx husky add .husky/pre-push "npm test"`

6. \*\*Advanced: Commit Message Validation\*\*:
 - Install commitlint.
 // turbo
 - Run `npm install --save-dev @commitlint/cli @commitlint/config-conventional`
 - Create commitlint.config.js:
```
module.exports = { extends: ['@commitlint/config-conventional'] };
```

 - Add hook:
 // turbo
 - Run `npx husky add .husky/commit-msg "npx commitlint --edit $1"`

7. \*\*Pro Tips\*\*:
 - Skip hooks if needed: `git commit --no-verify`.
 - Hooks run locally, so they're fast feedback loops.
 - Combine with CI/CD for double protection.
 - If husky commands fail, manually create files in `.husky/` directory.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `setup-husky-git-hooks-pre-commit-linting.md`
4. In Antigravity, type `/setup_husky_git_hooks_pre_commit_linting` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Pre-Flight Check

CI/CDTestingBuild+1--- description: Run type checking, linting, and build verification before pushing --- 1. \*\*Type Check\*\*: - Ensure there are no TypeScript errors. // turbo - Run `npx tsc --noEmit` 2. \*\*Lint Check\*\*: - Verify code quality rules. // turbo - Run `npm run lint` 3. \*\*Build Verificat...](/workflows/local-dev/run-pre-flight-check-type-lint-build)[### Create GitHub PR Template

GitGitHubTeam+1--- description: Standardize pull request descriptions for better code reviews --- 1. \*\*Create Template Directory\*\*: - GitHub looks for templates in `.github/` folder. // turbo - Run `mkdir -p .github` 2. \*\*Create Pull Request Template\*\*: - Create the template file with structured cont...](/workflows/local-dev/create-github-pull-request-template-markdown)[### Fix Lint Errors

LintingESLintPrettier+1--- description: Automatically fix linting and formatting issues across the project --- 1. \*\*Run ESLint Fix\*\*: - Attempt to automatically fix all fixable ESLint errors. // turbo - Run `npm run lint -- --fix` 2. \*\*Run Prettier\*\*: - Format all files in the project to ensure consistent st...](/workflows/local-dev/fix-eslint-prettier-linting-errors-automatically)
## Recommended Rules

[View more rules →](/rules)[### CI/CD Pipelines (GitHub Actions, GitLab CI)

CI/CDGitHub ActionsGitLab CIYou are an expert in CI/CD pipelines, specifically GitHub Actions and GitLab CI. Key Principles: - Fail fast and provide feedback - Automate everythi...](/rules/devops-infrastructure/ci-cd-pipelines-automation)[### Python DevOps & Infrastructure Automation

PythonDevOpsInfrastructureYou are an expert in Python for DevOps, infrastructure automation, and CI/CD. Key Principles: - Automate repetitive tasks - Use infrastructure as cod...](/rules/python/python-devops-and-infrastructure)[### 🚀 DevOps & CI/CD Agent - Pipeline Expert

Agentic AIDevOpsCI/CDYou are an expert DevOps and CI/CD agent specialized in designing and implementing robust deployment pipelines and infrastructure. Apply systematic re...](/rules/agentic-ai/devops-cicd-agent)