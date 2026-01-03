---
description: Auto-deploy PRs
---

# Setup Preview Deployments


CI/CDGitHub ActionsDeploymentDownloadCopy Workflow---

1. \*\*Create GitHub Action\*\*:
```
name: Preview
   on:
     pull_request:
       types: [opened, synchronize]
   jobs:
     deploy:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v4
         - uses: actions/setup-node@v4
         - run: npm ci
         - run: npm run build
         - uses: amondnet/vercel-action@v25
```

2. \*\*Comment PR\*\*:
 - Add deployment URL to PR comments.

3. \*\*Pro Tips\*\*:
 - Add E2E tests.
 - Clean up old previews.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `setup-github-actions-preview-deployments.md`
4. In Antigravity, type `/setup_github_actions_preview_deployments` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Deploy to Vercel Preview

VercelDeploymentCI/CD--- description: Push current branch to a Vercel preview URL --- 1. \*\*Install Vercel CLI\*\*: - Ensure you have the CLI. // turbo - Run `npm i -g vercel` 2. \*\*Deploy\*\*: - Deploy the current directory. // turbo - Run `vercel` 3. \*\*Pro Tips\*\*: - Use `vercel --prod` to deploy to p...](/workflows/devops/deploy-branch-to-vercel-preview-environment)[### Implement Blue-Green Deployment

DeploymentDevOpsZero-Downtime--- description: Zero-downtime deploys --- 1. \*\*Setup Two Environments\*\*: - Blue: Current (v1.0) - Green: New (v1.1) 2. \*\*Route Traffic Gradually\*\*: ```ts const rolloutPercent = await get('green\_rollout') || 0; if (Math.random() \* 100 < rolloutPercent) { return NextResponse.rew...](/workflows/devops/implement-blue-green-deployment-strategy)[### Implement Feature Flags

Feature FlagsDeploymentA/B Testing+1--- description: Safely release features with toggles for gradual rollouts --- 1. \*\*Simple Approach: Environment Variables\*\*: - Use env vars for basic flags. ```ts // lib/features.ts export const features = { newDashboard: process.env.NEXT\_PUBLIC\_FEATURE\_NEW\_DASHBOARD === 'true', ...](/workflows/devops/implement-feature-flags-gradual-rollout)
## Recommended Rules

[View more rules →](/rules)[### CI/CD Pipelines (GitHub Actions, GitLab CI)

CI/CDGitHub ActionsGitLab CIYou are an expert in CI/CD pipelines, specifically GitHub Actions and GitLab CI. Key Principles: - Fail fast and provide feedback - Automate everythi...](/rules/devops-infrastructure/ci-cd-pipelines-automation)[### Next.js Deployment & DevOps Expert

Next.jsDeploymentDevOpsYou are an expert in Next.js deployment and DevOps practices. Key Principles: - Use Vercel for optimal Next.js hosting - Implement proper CI/CD pipel...](/rules/nextjs/nextjs-deployment-devops)[### 🚀 DevOps & CI/CD Agent - Pipeline Expert

Agentic AIDevOpsCI/CDYou are an expert DevOps and CI/CD agent specialized in designing and implementing robust deployment pipelines and infrastructure. Apply systematic re...](/rules/agentic-ai/devops-cicd-agent)