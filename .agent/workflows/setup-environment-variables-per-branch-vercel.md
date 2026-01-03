---
description: Configure different env vars for dev, staging, and production
---

# Setup Environment Variables Per Branch


DevOpsEnvironmentVercelCI/CDDownloadCopy Workflow---

1. \*\*Local Development (.env.local)\*\*:
 - Create `.env.local` for local overrides (never commit this).
```
# .env.local
   DATABASE_URL=postgresql://localhost:5432/mydb
   API_URL=http://localhost:3001
   NEXT_PUBLIC_APP_URL=http://localhost:3000
```

2. \*\*Shared Defaults (.env)\*\*:
 - Create `.env` for defaults (commit this).
```
# .env
   NEXT_PUBLIC_APP_NAME=MyApp
   NEXT_PUBLIC_MAX_UPLOAD_SIZE=5242880
```

3. \*\*Vercel Environment Setup\*\*:
 - In Vercel Dashboard → Project → Settings → Environment Variables.
 - Add variables for each environment:
 - \*\*Production\*\*: `main` branch
 - \*\*Preview\*\*: All other branches
 - \*\*Development\*\*: Local only

4. \*\*Access Branch-Specific Vars\*\*:
 - Vercel automatically injects `VERCEL_ENV`.
```
const apiUrl = process.env.VERCEL_ENV === 'production'
     ? 'https://api.myapp.com'
     : 'https://staging-api.myapp.com';
```

5. \*\*GitHub Actions Setup\*\*:
 - Use secrets for CI/CD.
```
# .github/workflows/test.yml
   env:
     DATABASE_URL: ${{ secrets.DATABASE_URL }}
     API_KEY: ${{ secrets.API_KEY }}
```

6. \*\*Pro Tips\*\*:
 - Prefix public vars with `NEXT_PUBLIC_` for client-side access.
 - Use `.env.example` as a template (commit).
 - Never log environment variables in production.
 - Use Vercel CLI to pull env vars: `vercel env pull .env.local`.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `setup-environment-variables-per-branch-vercel.md`
4. In Antigravity, type `/setup_environment_variables_per_branch_vercel` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Kill Port 3000


ConfigEnvironmentSetup--- description: Safely create a local .env file from .env.example --- 1. \*\*Check for .env.example\*\*: - Ensure the example file exists. // turbo - Run `test -f .env.example && echo "✅ Found .env.example" || echo "❌ .env.example not found"` 2. \*\*Copy to .env.local\*\*: - Create your local...](/workflows/local-dev/generate-local-env-file-from-example)[### Pre-Flight Check

CI/CDTestingBuild+1--- description: Run type checking, linting, and build verification before pushing --- 1. \*\*Type Check\*\*: - Ensure there are no TypeScript errors. // turbo - Run `npx tsc --noEmit` 2. \*\*Lint Check\*\*: - Verify code quality rules. // turbo - Run `npm run lint` 3. \*\*Build Verificat...](/workflows/local-dev/run-pre-flight-check-type-lint-build)
## Recommended Rules

[View more rules →](/rules)[### Next.js Deployment & DevOps Expert

Next.jsDeploymentDevOpsYou are an expert in Next.js deployment and DevOps practices. Key Principles: - Use Vercel for optimal Next.js hosting - Implement proper CI/CD pipel...](/rules/nextjs/nextjs-deployment-devops)[### 🚀 DevOps & CI/CD Agent - Pipeline Expert

Agentic AIDevOpsCI/CDYou are an expert DevOps and CI/CD agent specialized in designing and implementing robust deployment pipelines and infrastructure. Apply systematic re...](/rules/agentic-ai/devops-cicd-agent)[### Python DevOps & Infrastructure Automation

PythonDevOpsInfrastructureYou are an expert in Python for DevOps, infrastructure automation, and CI/CD. Key Principles: - Automate repetitive tasks - Use infrastructure as cod...](/rules/python/python-devops-and-infrastructure)