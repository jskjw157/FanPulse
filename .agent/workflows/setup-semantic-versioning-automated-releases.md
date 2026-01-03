---
description: Automate version bumps and changelog generation
---

# Setup Semantic Versioning


VersioningReleasesGitAutomationDownloadCopy Workflow---

1. \*\*Install semantic-release\*\*:
 - Automate versioning based on commit messages.
 // turbo
 - Run `npm install --save-dev semantic-release @semantic-release/changelog @semantic-release/git`

2. \*\*Configure Commit Convention\*\*:
 - Use Conventional Commits.
```
# Format: <type>(<scope>): <description>
   feat: add dark mode support
   fix: resolve hydration error
   docs: update README
   chore: upgrade dependencies
```

3. \*\*Create Release Config\*\*:
 - Create `.releaserc.json`.
```
{
     "branches": ["main"],
     "plugins": [
       "@semantic-release/commit-analyzer",
       "@semantic-release/release-notes-generator",
       "@semantic-release/changelog",
       "@semantic-release/npm",
       "@semantic-release/github",
       ["@semantic-release/git", {
         "assets": ["CHANGELOG.md", "package.json"],
         "message": "chore(release): ${nextRelease.version} [skip ci]"
       }]
     ]
   }
```

4. \*\*Setup GitHub Actions\*\*:
 - Automate releases on merge to main.
```
# .github/workflows/release.yml
   name: Release
   on:
     push:
       branches: [main]
   jobs:
     release:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v3
         - uses: actions/setup-node@v3
         - run: npm ci
         - run: npx semantic-release
           env:
             GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

5. \*\*Commit with Convention\*\*:
 - Follow the format.
```
git commit -m "feat: add user authentication"
   # Will bump MINOR version (0.1.0 -> 0.2.0)

   git commit -m "fix: resolve login bug"
   # Will bump PATCH version (0.2.0 -> 0.2.1)

   git commit -m "feat!: redesign API\n\nBREAKING CHANGE: API endpoints changed"
   # Will bump MAJOR version (0.2.1 -> 1.0.0)
```

6. \*\*Pro Tips\*\*:
 - Use Commitizen for interactive commit messages: `npx cz`.
 - CHANGELOG.md is auto-generated, don't edit manually.
 - Releases create Git tags automatically.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `setup-semantic-versioning-automated-releases.md`
4. In Antigravity, type `/setup_semantic_versioning_automated_releases` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Setup Vercel Cron Jobs

VercelCronAutomation--- description: Create and test scheduled tasks in Next.js --- 1. \*\*Create Cron Config\*\*: - Add `crons` to `vercel.json`. ```json { "crons": [ { "path": "/api/cron/daily-report", "schedule": "0 10 \* \* \*" } ] } ``` 2. \*\*Create API Route\*\*: ...](/workflows/devops/setup-vercel-cron-jobs-scheduled-tasks)[### Analyze Bundle Size

PerformanceNext.jsOptimization--- description: Visualize and reduce your production build size --- 1. \*\*Install Analyzer\*\*: - Install the Next.js bundle analyzer. // turbo - Run `npm install @next/bundle-analyzer` 2. \*\*Configure next.config.js\*\*: - Wrap your config. ```js const withBundleAnalyzer = require('@...](/workflows/devops/analyze-nextjs-bundle-size-optimization)[### Database Migration Rollback

DatabasePrismaEmergency--- description: Revert the last database migration if something goes wrong --- 1. \*\*Identify Migration\*\*: - Check migration status. // turbo - Run `npx prisma migrate status` 2. \*\*Resolve Migration\*\*: - Mark a failed migration as resolved (if stuck). // turbo - Run `npx prisma m...](/workflows/devops/rollback-prisma-database-migration-emergency)
## Recommended Rules

[View more rules →](/rules)[### Python Automation & Scripting Expert

PythonAutomationScriptingYou are an expert in Python automation and scripting. Key Principles: - Write robust, error-resistant scripts - Implement proper logging - Handle edg...](/rules/python/python-automation-and-scripting-expert)[### Python DevOps & Infrastructure Automation

PythonDevOpsInfrastructureYou are an expert in Python for DevOps, infrastructure automation, and CI/CD. Key Principles: - Automate repetitive tasks - Use infrastructure as cod...](/rules/python/python-devops-and-infrastructure)[### CI/CD Pipelines (GitHub Actions, GitLab CI)

CI/CDGitHub ActionsGitLab CIYou are an expert in CI/CD pipelines, specifically GitHub Actions and GitLab CI. Key Principles: - Fail fast and provide feedback - Automate everythi...](/rules/devops-infrastructure/ci-cd-pipelines-automation)