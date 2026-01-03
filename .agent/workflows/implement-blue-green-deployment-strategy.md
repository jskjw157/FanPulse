---
description: Zero-downtime deploys
---

# Implement Blue-Green Deployment


DeploymentDevOpsZero-DowntimeDownloadCopy Workflow---

1. \*\*Setup Two Environments\*\*:
 - Blue: Current (v1.0)
 - Green: New (v1.1)

2. \*\*Route Traffic Gradually\*\*:
```
const rolloutPercent = await get('green_rollout') || 0;
   if (Math.random() * 100 < rolloutPercent) {
     return NextResponse.rewrite(new URL('/green', request.url));
   }
```

3. \*\*Monitor Metrics\*\*:
```
Sentry.setTag('environment', isGreen ? 'green' : 'blue');
```

4. \*\*Pro Tips\*\*:
 - Test thoroughly before routing.
 - Keep blue for rollback.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `implement-blue-green-deployment-strategy.md`
4. In Antigravity, type `/implement_blue_green_deployment_strategy` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Implement Feature Flags

Feature FlagsDeploymentA/B Testing+1--- description: Safely release features with toggles for gradual rollouts --- 1. \*\*Simple Approach: Environment Variables\*\*: - Use env vars for basic flags. ```ts // lib/features.ts export const features = { newDashboard: process.env.NEXT\_PUBLIC\_FEATURE\_NEW\_DASHBOARD === 'true', ...](/workflows/devops/implement-feature-flags-gradual-rollout)[### Setup Preview Deployments

CI/CDGitHub ActionsDeployment--- description: Auto-deploy PRs --- 1. \*\*Create GitHub Action\*\*: ```yaml name: Preview on: pull\_request: types: [opened, synchronize] jobs: deploy: runs-on: ubuntu-latest steps: - uses: actions/checkout@v4 - uses: actions/setup-node@v4 ...](/workflows/devops/setup-github-actions-preview-deployments)[### Deploy to Vercel Preview

VercelDeploymentCI/CD--- description: Push current branch to a Vercel preview URL --- 1. \*\*Install Vercel CLI\*\*: - Ensure you have the CLI. // turbo - Run `npm i -g vercel` 2. \*\*Deploy\*\*: - Deploy the current directory. // turbo - Run `vercel` 3. \*\*Pro Tips\*\*: - Use `vercel --prod` to deploy to p...](/workflows/devops/deploy-branch-to-vercel-preview-environment)
## Recommended Rules

[View more rules →](/rules)[### Next.js Deployment & DevOps Expert

Next.jsDeploymentDevOpsYou are an expert in Next.js deployment and DevOps practices. Key Principles: - Use Vercel for optimal Next.js hosting - Implement proper CI/CD pipel...](/rules/nextjs/nextjs-deployment-devops)[### MLOps & Model Deployment

MLOpsDeploymentDevOpsYou are an expert in MLOps (Machine Learning Operations) and Model Deployment. Key Principles: - Treat ML as software (Version Control, CI/CD) - Auto...](/rules/ai-machine-learning/mlops-model-deployment)[### Database Migration Strategies

MigrationsDevOpsDatabaseYou are an expert in database migrations and schema evolution. Key Principles: - Treat database changes as code - Version control all migrations - En...](/rules/database-data/database-migration-strategies)