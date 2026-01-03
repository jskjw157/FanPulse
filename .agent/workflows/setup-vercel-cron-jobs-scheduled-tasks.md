---
description: Create and test scheduled tasks in Next.js
---

# Setup Vercel Cron Jobs


VercelCronAutomationDownloadCopy Workflow---

1. \*\*Create Cron Config\*\*:
 - Add `crons` to `vercel.json`.
```
{
     "crons": [
       {
         "path": "/api/cron/daily-report",
         "schedule": "0 10 * * *"
       }
     ]
   }
```

2. \*\*Create API Route\*\*:
 - Create the endpoint at `src/app/api/cron/daily-report/route.ts`.
```
import { NextResponse } from 'next/server';

   export async function GET(request: Request) {
     // Verify the request is from Vercel Cron
     const authHeader = request.headers.get('authorization');
     if (authHeader !== Bearer ${process.env.CRON_SECRET}) {
       return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
     }

     // Your cron job logic here
     console.log('Daily report cron executed');

     return NextResponse.json({ success: true });
   }
```

3. \*\*Set Environment Variable\*\*:
 - Add `CRON_SECRET` to your `.env.local` and Vercel project settings.
 - Generate a secure random string: `openssl rand -base64 32`

4. \*\*Pro Tips\*\*:
 - Vercel sends an `Authorization: Bearer <token>` header with cron requests.
 - Test locally by manually calling the endpoint with the correct header.
 - Cron expressions use UTC timezone.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `setup-vercel-cron-jobs-scheduled-tasks.md`
4. In Antigravity, type `/setup_vercel_cron_jobs_scheduled_tasks` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Deploy to Vercel Preview

VercelDeploymentCI/CD--- description: Push current branch to a Vercel preview URL --- 1. \*\*Install Vercel CLI\*\*: - Ensure you have the CLI. // turbo - Run `npm i -g vercel` 2. \*\*Deploy\*\*: - Deploy the current directory. // turbo - Run `vercel` 3. \*\*Pro Tips\*\*: - Use `vercel --prod` to deploy to p...](/workflows/devops/deploy-branch-to-vercel-preview-environment)[### Setup Semantic Versioning

VersioningReleasesGit+1--- description: Automate version bumps and changelog generation --- 1. \*\*Install semantic-release\*\*: - Automate versioning based on commit messages. // turbo - Run `npm install --save-dev semantic-release @semantic-release/changelog @semantic-release/git` 2. \*\*Configure Commit Convention...](/workflows/devops/setup-semantic-versioning-automated-releases)[### Analyze Bundle Size

PerformanceNext.jsOptimization--- description: Visualize and reduce your production build size --- 1. \*\*Install Analyzer\*\*: - Install the Next.js bundle analyzer. // turbo - Run `npm install @next/bundle-analyzer` 2. \*\*Configure next.config.js\*\*: - Wrap your config. ```js const withBundleAnalyzer = require('@...](/workflows/devops/analyze-nextjs-bundle-size-optimization)
## Recommended Rules

[View more rules →](/rules)[### Python Automation & Scripting Expert

PythonAutomationScriptingYou are an expert in Python automation and scripting. Key Principles: - Write robust, error-resistant scripts - Implement proper logging - Handle edg...](/rules/python/python-automation-and-scripting-expert)[### Python DevOps & Infrastructure Automation

PythonDevOpsInfrastructureYou are an expert in Python for DevOps, infrastructure automation, and CI/CD. Key Principles: - Automate repetitive tasks - Use infrastructure as cod...](/rules/python/python-devops-and-infrastructure)[### Test Automation Frameworks

AutomationTestingArchitectureYou are an expert in designing and building Test Automation Frameworks. Key Principles: - Maintainability and Scalability - Reusability of code - Rep...](/rules/testing-quality-assurance/test-automation-frameworks)