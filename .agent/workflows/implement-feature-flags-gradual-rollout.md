---
description: Safely release features with toggles for gradual rollouts
---

# Implement Feature Flags


Feature FlagsDeploymentA/B TestingDevOpsDownloadCopy Workflow---

1. \*\*Simple Approach: Environment Variables\*\*:
 - Use env vars for basic flags.
```
// lib/features.ts
   export const features = {
     newDashboard: process.env.NEXT_PUBLIC_FEATURE_NEW_DASHBOARD === 'true',
     aiChat: process.env.NEXT_PUBLIC_FEATURE_AI_CHAT === 'true',
   };
```

2. \*\*Use in Components\*\*:
 - Conditionally render features.
```
import { features } from '@/lib/features';

   export default function Dashboard() {
     if (features.newDashboard) {
       return <NewDashboard />;
     }
     return <OldDashboard />;
   }
```

3. \*\*Advanced: Vercel Edge Config\*\*:
 - Dynamic flags without redeployment.
 // turbo
 - Run `npm install @vercel/edge-config`
```
import { get } from '@vercel/edge-config';

   export async function getFeatureFlags() {
     const flags = await get('features');
     return flags || {};
   }
```

4. \*\*Production-Ready: LaunchDarkly/PostHog\*\*:
 - Install SDK.
 // turbo
 - Run `npm install launchdarkly-react-client-sdk`
```
import { useLDClient, useFlags } from 'launchdarkly-react-client-sdk';

   function MyComponent() {
     const { newFeature } = useFlags();

     if (newFeature) {
       return <NewFeature />;
     }
     return <OldFeature />;
   }
```

5. \*\*Gradual Rollout Pattern\*\*:
 - Enable for percentage of users.
```
function isFeatureEnabled(userId: string, rolloutPercent: number) {
     const hash = userId.split('').reduce((a, b) => a + b.charCodeAt(0), 0);
     return (hash % 100) < rolloutPercent;
   }

   const showNewUI = isFeatureEnabled(user.id, 25); // 25% rollout
```

6. \*\*Pro Tips\*\*:
 - Always have a kill switch for new features.
 - Log feature flag usage for analytics.
 - Clean up old flags after full rollout.
 - Use flags for A/B testing experiments.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `implement-feature-flags-gradual-rollout.md`
4. In Antigravity, type `/implement_feature_flags_gradual_rollout` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Implement Blue-Green Deployment

DeploymentDevOpsZero-Downtime--- description: Zero-downtime deploys --- 1. \*\*Setup Two Environments\*\*: - Blue: Current (v1.0) - Green: New (v1.1) 2. \*\*Route Traffic Gradually\*\*: ```ts const rolloutPercent = await get('green\_rollout') || 0; if (Math.random() \* 100 < rolloutPercent) { return NextResponse.rew...](/workflows/devops/implement-blue-green-deployment-strategy)[### Setup Preview Deployments

CI/CDGitHub ActionsDeployment--- description: Auto-deploy PRs --- 1. \*\*Create GitHub Action\*\*: ```yaml name: Preview on: pull\_request: types: [opened, synchronize] jobs: deploy: runs-on: ubuntu-latest steps: - uses: actions/checkout@v4 - uses: actions/setup-node@v4 ...](/workflows/devops/setup-github-actions-preview-deployments)[### Deploy to Vercel Preview

VercelDeploymentCI/CD--- description: Push current branch to a Vercel preview URL --- 1. \*\*Install Vercel CLI\*\*: - Ensure you have the CLI. // turbo - Run `npm i -g vercel` 2. \*\*Deploy\*\*: - Deploy the current directory. // turbo - Run `vercel` 3. \*\*Pro Tips\*\*: - Use `vercel --prod` to deploy to p...](/workflows/devops/deploy-branch-to-vercel-preview-environment)
## Recommended Rules

[View more rules →](/rules)[### Next.js Deployment & DevOps Expert

Next.jsDeploymentDevOpsYou are an expert in Next.js deployment and DevOps practices. Key Principles: - Use Vercel for optimal Next.js hosting - Implement proper CI/CD pipel...](/rules/nextjs/nextjs-deployment-devops)[### MLOps & Model Deployment

MLOpsDeploymentDevOpsYou are an expert in MLOps (Machine Learning Operations) and Model Deployment. Key Principles: - Treat ML as software (Version Control, CI/CD) - Auto...](/rules/ai-machine-learning/mlops-model-deployment)[### Database Migration Strategies

MigrationsDevOpsDatabaseYou are an expert in database migrations and schema evolution. Key Principles: - Treat database changes as code - Version control all migrations - En...](/rules/database-data/database-migration-strategies)