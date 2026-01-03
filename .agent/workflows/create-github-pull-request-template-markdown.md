---
description: Standardize pull request descriptions for better code reviews
---

# Create GitHub PR Template


GitGitHubTeamDocumentationDownloadCopy Workflow---

1. \*\*Create Template Directory\*\*:
 - GitHub looks for templates in `.github/` folder.
 // turbo
 - Run `mkdir -p .github`

2. \*\*Create Pull Request Template\*\*:
 - Create the template file with structured content.
 ```markdown\n ## Description\n Briefly describe what this PR does.\n \n ## Type of Change\n - [ ] Bug fix\n - [ ] New feature\n - [ ] Breaking change\n - [ ] Documentation update\n \n ## Testing\n - [ ] I have tested these changes locally\n - [ ] I have added/updated tests\n - [ ] All tests pass\n \n ## Screenshots (if applicable)\n \n ## Checklist\n - [ ] My code follows the project's style guidelines\n - [ ] I have performed a self-review\n - [ ] I have commented complex code\n - [ ] I have updated documentation\n - [ ] No new warnings generated\n \n ## Related Issues\n Closes #\n` ``\n - Save this as` .github/PULL\_REQUEST\_TEMPLATE.md`\n
3. **Commit and Push**:
 - Add the template to your repository.
 // turbo
 - Run` git add .github/PULL\_REQUEST\_TEMPLATE.md && git commit -m \"Add PR template\" && git push `4. **Test It**:
 - Create a new PR and the template will auto-populate.

5. **Pro Tips**:
 - Customize the template for your team's workflow.
 - Add a link to your contributing guidelines.
 - Use multiple templates for different PR types (create` .github/PULL\_REQUEST\_TEMPLATE/` folder).By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `create-github-pull-request-template-markdown.md`
4. In Antigravity, type `/create_github_pull_request_template_markdown` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Setup Husky Git Hooks

GitAutomationQuality+1--- description: Automate code quality checks with pre-commit and pre-push hooks --- 1. \*\*Install Husky\*\*: - Install husky and lint-staged. // turbo - Run `npm install --save-dev husky lint-staged` 2. \*\*Initialize Husky\*\*: - Set up git hooks. // turbo - Run `npx husky init` 3. \*...](/workflows/local-dev/setup-husky-git-hooks-pre-commit-linting)[### Kill Port 3000


ConfigEnvironmentSetup--- description: Safely create a local .env file from .env.example --- 1. \*\*Check for .env.example\*\*: - Ensure the example file exists. // turbo - Run `test -f .env.example && echo "✅ Found .env.example" || echo "❌ .env.example not found"` 2. \*\*Copy to .env.local\*\*: - Create your local...](/workflows/local-dev/generate-local-env-file-from-example)
## Recommended Rules

[View more rules →](/rules)[### Git Feature Branch Workflow

WorkflowsGitVersion ControlCreate a workflow to start new feature branches synchronized with main. Workflow File: .agent/workflows/start\_feature.md ```markdown --- description...](/rules/antigravity-workflows/git-feature-branch-workflow)[### CI/CD Pipelines (GitHub Actions, GitLab CI)

CI/CDGitHub ActionsGitLab CIYou are an expert in CI/CD pipelines, specifically GitHub Actions and GitLab CI. Key Principles: - Fail fast and provide feedback - Automate everythi...](/rules/devops-infrastructure/ci-cd-pipelines-automation)[### GitOps & ArgoCD Expert

GitOpsArgoCDKubernetesYou are an expert in GitOps methodology and ArgoCD. Key Principles: - Git as the single source of truth - Declarative infrastructure and applications...](/rules/devops-infrastructure/gitops-argocd-expert)