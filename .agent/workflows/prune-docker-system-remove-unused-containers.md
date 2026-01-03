---
description: Reclaim disk space by removing unused containers and images
---

# Prune Docker System


DockerCleanupDisk SpaceDownloadCopy Workflow---

1. \*\*Check Current Usage\*\*:
 - See how much space Docker is using.
 // turbo
 - Run `docker system df`

2. \*\*Run Prune\*\*:
 - ⚠️ \*\*WARNING\*\*: This will remove all stopped containers and unused images!
 - Remove all stopped containers, unused networks, and dangling images.
 // turbo
 - Run `docker system prune -a`

3. \*\*Verify Space Reclaimed\*\*:
 - Check the new disk usage.
 // turbo
 - Run `docker system df`

4. \*\*Pro Tips\*\*:
 - Add `--volumes` to also delete unused volumes (DATA LOSS WARNING!).
 - To remove only dangling images: `docker image prune`.
 - Set up automatic cleanup: add `"log-opts": {"max-size": "10m"}` to Docker daemon config.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `prune-docker-system-remove-unused-containers.md`
4. In Antigravity, type `/prune_docker_system_remove_unused_containers` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Kill Port 3000


ConfigEnvironmentSetup--- description: Safely create a local .env file from .env.example --- 1. \*\*Check for .env.example\*\*: - Ensure the example file exists. // turbo - Run `test -f .env.example && echo "✅ Found .env.example" || echo "❌ .env.example not found"` 2. \*\*Copy to .env.local\*\*: - Create your local...](/workflows/local-dev/generate-local-env-file-from-example)[### Update All Dependencies

npmMaintenanceUpdates--- description: Interactively check and update outdated packages --- 1. \*\*Check for Updates\*\*: - Use `npm-check-updates` to see what's new. // turbo - Run `npx npm-check-updates` 2. \*\*Review Changes\*\*: - ⚠️ \*\*WARNING\*\*: Always review major version changes before updating! - Check c...](/workflows/local-dev/update-npm-dependencies-check-updates)
## Recommended Rules

[View more rules →](/rules)[### Docker & Containerization Expert

DockerContainersDevOpsYou are an expert in Docker and containerization technologies. Key Principles: - Build once, run anywhere - Keep images small and secure - Use multi-...](/rules/devops-infrastructure/docker-containerization-expert)[### 🚀 DevOps & CI/CD Agent - Pipeline Expert

Agentic AIDevOpsCI/CDYou are an expert DevOps and CI/CD agent specialized in designing and implementing robust deployment pipelines and infrastructure. Apply systematic re...](/rules/agentic-ai/devops-cicd-agent)[### Strong Reasoner & Planner Agent (Official Google Template)

Agentic AIReasoningPlanningYou are a very strong reasoner and planner. Use these critical instructions to structure your plans, thoughts, and responses. 📋 Source: Google Gemin...](/rules/agentic-ai/strong-reasoner-planner-agent)