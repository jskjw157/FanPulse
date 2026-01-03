---
description: Interactively check and update outdated packages
---

# Update All Dependencies


npmMaintenanceUpdatesDownloadCopy Workflow---

1. \*\*Check for Updates\*\*:
 - Use `npm-check-updates` to see what's new.
 // turbo
 - Run `npx npm-check-updates`

2. \*\*Review Changes\*\*:
 - ⚠️ \*\*WARNING\*\*: Always review major version changes before updating!
 - Check changelogs for breaking changes.
 - Look for major version bumps (e.g., 1.x.x → 2.x.x).

3. \*\*Update package.json\*\*:
 - Update versions in your package file.
 // turbo
 - Run `npx npm-check-updates -u`

4. \*\*Install New Versions\*\*:
 - Install the updated packages.
 // turbo
 - Run `npm install`

5. \*\*Test Thoroughly\*\*:
 - Run your tests immediately after updating.
 // turbo
 - Run `npm test`

6. \*\*Pro Tips\*\*:
 - Use `-i` for interactive mode to selectively update: `npx ncu -i`.
 - Update only minor/patch versions: `npx ncu -u --target minor`.
 - Check for peer dependency conflicts with `npm ls`.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `update-npm-dependencies-check-updates.md`
4. In Antigravity, type `/update_npm_dependencies_check_updates` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Kill Port 3000


ConfigEnvironmentSetup--- description: Safely create a local .env file from .env.example --- 1. \*\*Check for .env.example\*\*: - Ensure the example file exists. // turbo - Run `test -f .env.example && echo "✅ Found .env.example" || echo "❌ .env.example not found"` 2. \*\*Copy to .env.local\*\*: - Create your local...](/workflows/local-dev/generate-local-env-file-from-example)[### Prune Docker System

DockerCleanupDisk Space--- description: Reclaim disk space by removing unused containers and images --- 1. \*\*Check Current Usage\*\*: - See how much space Docker is using. // turbo - Run `docker system df` 2. \*\*Run Prune\*\*: - ⚠️ \*\*WARNING\*\*: This will remove all stopped containers and unused images! - Remov...](/workflows/local-dev/prune-docker-system-remove-unused-containers)
## Recommended Rules

[View more rules →](/rules)[### iOS Swift Development

iOSSwiftSwiftUIYou are an expert in iOS development using Swift and SwiftUI/UIKit. Key Principles: - Follow Apple's Human Interface Guidelines (HIG) - Write safe, f...](/rules/mobile-development/ios-swift-development)[### Android Kotlin Development

AndroidKotlinJetpack ComposeYou are an expert in Android development using Kotlin and Jetpack Compose. Key Principles: - Follow Material Design guidelines - Build responsive and...](/rules/mobile-development/android-kotlin-development)[### Mobile UI/UX Best Practices

UI/UXMobileDesignYou are an expert in Mobile UI/UX design and implementation. Key Principles: - Design for touch (fingers are not cursors) - Content first, chrome sec...](/rules/mobile-development/mobile-ui-ux-best-practices)