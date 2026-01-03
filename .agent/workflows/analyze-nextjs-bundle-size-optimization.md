---
description: Visualize and reduce your production build size
---

# Analyze Bundle Size


PerformanceNext.jsOptimizationDownloadCopy Workflow---

1. \*\*Install Analyzer\*\*:
 - Install the Next.js bundle analyzer.
 // turbo
 - Run `npm install @next/bundle-analyzer`

2. \*\*Configure next.config.js\*\*:
 - Wrap your config.
```
const withBundleAnalyzer = require('@next/bundle-analyzer')({
     enabled: process.env.ANALYZE === 'true',
   })
   module.exports = withBundleAnalyzer({
     // Other config options
   })
```

3. \*\*Run Analysis\*\*:
 - Build with analysis enabled.
 // turbo
 - Run `ANALYZE=true npm run build`

4. \*\*Pro Tips\*\*:
 - Works with \*\*TurboPack\*\* in Next.js 15.
 - Look for large libraries (like `lodash` or `moment`) that can be tree-shaken or replaced.
 - Use `import { x } from 'package'` instead of `import package from 'package'`.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `analyze-nextjs-bundle-size-optimization.md`
4. In Antigravity, type `/analyze_nextjs_bundle_size_optimization` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Setup Vercel Cron Jobs

VercelCronAutomation--- description: Create and test scheduled tasks in Next.js --- 1. \*\*Create Cron Config\*\*: - Add `crons` to `vercel.json`. ```json { "crons": [ { "path": "/api/cron/daily-report", "schedule": "0 10 \* \* \*" } ] } ``` 2. \*\*Create API Route\*\*: ...](/workflows/devops/setup-vercel-cron-jobs-scheduled-tasks)[### Database Migration Rollback

DatabasePrismaEmergency--- description: Revert the last database migration if something goes wrong --- 1. \*\*Identify Migration\*\*: - Check migration status. // turbo - Run `npx prisma migrate status` 2. \*\*Resolve Migration\*\*: - Mark a failed migration as resolved (if stuck). // turbo - Run `npx prisma m...](/workflows/devops/rollback-prisma-database-migration-emergency)[### Deploy to Vercel Preview

VercelDeploymentCI/CD--- description: Push current branch to a Vercel preview URL --- 1. \*\*Install Vercel CLI\*\*: - Ensure you have the CLI. // turbo - Run `npm i -g vercel` 2. \*\*Deploy\*\*: - Deploy the current directory. // turbo - Run `vercel` 3. \*\*Pro Tips\*\*: - Use `vercel --prod` to deploy to p...](/workflows/devops/deploy-branch-to-vercel-preview-environment)
## Recommended Rules

[View more rules →](/rules)[### Next.js Performance Optimization

Next.jsPerformanceOptimizationYou are an expert in Next.js performance optimization. Key Principles: - Optimize images and fonts - Minimize client-side JavaScript - Use proper cac...](/rules/nextjs/nextjs-performance-optimization)[### ⚡ Performance Optimization Agent - Speed Expert

Agentic AIPerformanceOptimizationYou are an expert performance optimization agent specialized in identifying and fixing performance bottlenecks. Apply systematic reasoning to measure,...](/rules/agentic-ai/performance-optimization-agent)[### Web Performance Optimization Expert

PerformanceWeb VitalsOptimizationYou are an expert in web performance optimization and Core Web Vitals. Key Principles: - Optimize for Core Web Vitals - Minimize Time to First Byte (...](/rules/web-development/web-performance-optimization)