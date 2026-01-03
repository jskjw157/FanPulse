---
description: Revert the last database migration if something goes wrong
---

# Database Migration Rollback


DatabasePrismaEmergencyDownloadCopy Workflow---

1. \*\*Identify Migration\*\*:
 - Check migration status.
 // turbo
 - Run `npx prisma migrate status`

2. \*\*Resolve Migration\*\*:
 - Mark a failed migration as resolved (if stuck).
 // turbo
 - Run `npx prisma migrate resolve --rolled-back "migration_name"`

3. \*\*Down Migration (Manual)\*\*:
 - Prisma doesn't support auto-down. You must manually execute SQL to revert changes.
 // turbo
 - Run `psql -d mydb -f ./migrations/down.sql`

4. \*\*Pro Tips\*\*:
 - Always test migrations on a copy of production data first.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `rollback-prisma-database-migration-emergency.md`
4. In Antigravity, type `/rollback_prisma_database_migration_emergency` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Setup Database Seeding

DatabasePrismaDevelopment+1--- description: Populate your database with realistic test data --- 1. \*\*Install Faker\*\*: - Generate realistic fake data. // turbo - Run `npm install --save-dev @faker-js/faker` 2. \*\*Create Seed Script\*\*: - Create `prisma/seed.ts`. ```ts import { PrismaClient } from '@prisma/cli...](/workflows/devops/setup-prisma-database-seeding-faker)[### Analyze Bundle Size

PerformanceNext.jsOptimization--- description: Visualize and reduce your production build size --- 1. \*\*Install Analyzer\*\*: - Install the Next.js bundle analyzer. // turbo - Run `npm install @next/bundle-analyzer` 2. \*\*Configure next.config.js\*\*: - Wrap your config. ```js const withBundleAnalyzer = require('@...](/workflows/devops/analyze-nextjs-bundle-size-optimization)[### Setup Vercel Cron Jobs

VercelCronAutomation--- description: Create and test scheduled tasks in Next.js --- 1. \*\*Create Cron Config\*\*: - Add `crons` to `vercel.json`. ```json { "crons": [ { "path": "/api/cron/daily-report", "schedule": "0 10 \* \* \*" } ] } ``` 2. \*\*Create API Route\*\*: ...](/workflows/devops/setup-vercel-cron-jobs-scheduled-tasks)
## Recommended Rules

[View more rules →](/rules)[### Next.js Database Integration Expert

Next.jsDatabasePrismaYou are an expert in Next.js database integration with Prisma and modern ORMs. Key Principles: - Use Prisma for type-safe database access - Implement...](/rules/nextjs/nextjs-database-integration)[### 📊 Database Design Agent - Schema & Query Expert

Agentic AIDatabaseSQLYou are an expert database design agent specialized in creating efficient, scalable, and well-normalized database schemas. Apply systematic reasoning ...](/rules/agentic-ai/database-design-agent)[### Go Database Integration

GoDatabaseGORMYou are an expert in Go database integration using database/sql, GORM, and sqlx. Key Principles: - Use connection pooling - Use prepared statements -...](/rules/go/go-database-integration)