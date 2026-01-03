---
description: Populate your database with realistic test data
---

# Setup Database Seeding


DatabasePrismaDevelopmentTestingDownloadCopy Workflow---

1. \*\*Install Faker\*\*:
 - Generate realistic fake data.
 // turbo
 - Run `npm install --save-dev @faker-js/faker`

2. \*\*Create Seed Script\*\*:
 - Create `prisma/seed.ts`.
```
import { PrismaClient } from '@prisma/client';
   import { faker } from '@faker-js/faker';

   const prisma = new PrismaClient();

   async function main() {
     // Clear existing data
     await prisma.post.deleteMany();
     await prisma.user.deleteMany();

     // Create 10 users
     const users = await Promise.all(
       Array.from({ length: 10 }).map(() =>
         prisma.user.create({
           data: {
             email: faker.internet.email(),
             name: faker.person.fullName(),
             avatar: faker.image.avatar(),
           },
         })
       )
     );

     // Create 50 posts
     await Promise.all(
       Array.from({ length: 50 }).map(() =>
         prisma.post.create({
           data: {
             title: faker.lorem.sentence(),
             content: faker.lorem.paragraphs(3),
             authorId: faker.helpers.arrayElement(users).id,
           },
         })
       )
     );

     console.log('✅ Database seeded successfully');
   }

   main()
     .catch(console.error)
     .finally(() => prisma.$disconnect());
```

3. \*\*Configure package.json\*\*:
 - Add seed command.
```
{
     "prisma": {
       "seed": "ts-node --compiler-options {\"module\":\"CommonJS\"} prisma/seed.ts"
     },
     "scripts": {
       "db:seed": "prisma db seed"
     }
   }
```

4. \*\*Run Seed\*\*:
 - Populate your database.
 // turbo
 - Run `npm run db:seed`

5. \*\*Reset Database\*\*:
 - Wipe and re-seed.
 // turbo
 - Run `npx prisma migrate reset` (runs seed automatically)

6. \*\*Pro Tips\*\*:
 - Create different seed files for dev/staging/test.
 - Use deterministic seeds for consistent testing.
 - Seed only in development; never in production!
 - Consider using snapshots of production data (anonymized).By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `setup-prisma-database-seeding-faker.md`
4. In Antigravity, type `/setup_prisma_database_seeding_faker` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Database Migration Rollback

DatabasePrismaEmergency--- description: Revert the last database migration if something goes wrong --- 1. \*\*Identify Migration\*\*: - Check migration status. // turbo - Run `npx prisma migrate status` 2. \*\*Resolve Migration\*\*: - Mark a failed migration as resolved (if stuck). // turbo - Run `npx prisma m...](/workflows/devops/rollback-prisma-database-migration-emergency)[### Analyze Bundle Size

PerformanceNext.jsOptimization--- description: Visualize and reduce your production build size --- 1. \*\*Install Analyzer\*\*: - Install the Next.js bundle analyzer. // turbo - Run `npm install @next/bundle-analyzer` 2. \*\*Configure next.config.js\*\*: - Wrap your config. ```js const withBundleAnalyzer = require('@...](/workflows/devops/analyze-nextjs-bundle-size-optimization)[### Setup Vercel Cron Jobs

VercelCronAutomation--- description: Create and test scheduled tasks in Next.js --- 1. \*\*Create Cron Config\*\*: - Add `crons` to `vercel.json`. ```json { "crons": [ { "path": "/api/cron/daily-report", "schedule": "0 10 \* \* \*" } ] } ``` 2. \*\*Create API Route\*\*: ...](/workflows/devops/setup-vercel-cron-jobs-scheduled-tasks)
## Recommended Rules

[View more rules →](/rules)[### Next.js Database Integration Expert

Next.jsDatabasePrismaYou are an expert in Next.js database integration with Prisma and modern ORMs. Key Principles: - Use Prisma for type-safe database access - Implement...](/rules/nextjs/nextjs-database-integration)[### 📊 Database Design Agent - Schema & Query Expert

Agentic AIDatabaseSQLYou are an expert database design agent specialized in creating efficient, scalable, and well-normalized database schemas. Apply systematic reasoning ...](/rules/agentic-ai/database-design-agent)[### Python Testing Best Practices

PythonTestingQuality AssuranceYou are an expert in Python testing with pytest and testing best practices. Key Principles: - Write tests before or alongside code (TDD/BDD) - Aim fo...](/rules/python/python-testing-best-practices)