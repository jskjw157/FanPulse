---
description: Instantly find and kill the process hogging your dev port
---

# Kill Port 3000



1. \*\*The Best Way (Cross-Platform)\*\*:
 - Kill it in one command using npx. Works on Mac, Windows, and Linux.
 // turbo
 - Run `npx kill-port 3000`

2. \*\*Mac/Linux Manual Method\*\*:
 - Find PID: `lsof -ti:3000`
 - Kill it: `kill -9 $(lsof -ti:3000)`

3. \*\*Windows Manual Method\*\*:
 - Find PID: `netstat -ano | findstr :3000`
 - Kill it: `taskkill /PID <PID> /F`

4. \*\*Pro Tips\*\*:
 - This works for any port, just change 3000 to whatever you need.
 - Add an alias to your shell profile: `alias kill3000="npx kill-port 3000"`.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `kill-process-on-port-3000-terminal-command.md`
4. In Antigravity, type `/kill_process_on_port_3000_terminal_command` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Setup Environment Variables Per Branch

DevOpsEnvironmentVercel+1--- description: Configure different env vars for dev, staging, and production --- 1. \*\*Local Development (.env.local)\*\*: - Create `.env.local` for local overrides (never commit this). ```bash # .env.local DATABASE\_URL=postgresql://localhost:5432/mydb API\_URL=http://localhost:3001 ...](/workflows/local-dev/setup-environment-variables-per-branch-vercel)[### Generate .env from Example

ConfigEnvironmentSetup--- description: Safely create a local .env file from .env.example --- 1. \*\*Check for .env.example\*\*: - Ensure the example file exists. // turbo - Run `test -f .env.example && echo "✅ Found .env.example" || echo "❌ .env.example not found"` 2. \*\*Copy to .env.local\*\*: - Create your local...](/workflows/local-dev/generate-local-env-file-from-example)[### Prune Docker System

DockerCleanupDisk Space--- description: Reclaim disk space by removing unused containers and images --- 1. \*\*Check Current Usage\*\*: - See how much space Docker is using. // turbo - Run `docker system df` 2. \*\*Run Prune\*\*: - ⚠️ \*\*WARNING\*\*: This will remove all stopped containers and unused images! - Remov...](/workflows/local-dev/prune-docker-system-remove-unused-containers)
## Recommended Rules

[View more rules →](/rules)[### 🚀 DevOps & CI/CD Agent - Pipeline Expert

Agentic AIDevOpsCI/CDYou are an expert DevOps and CI/CD agent specialized in designing and implementing robust deployment pipelines and infrastructure. Apply systematic re...](/rules/agentic-ai/devops-cicd-agent)[### Python DevOps & Infrastructure Automation

PythonDevOpsInfrastructureYou are an expert in Python for DevOps, infrastructure automation, and CI/CD. Key Principles: - Automate repetitive tasks - Use infrastructure as cod...](/rules/python/python-devops-and-infrastructure)[### Next.js Deployment & DevOps Expert

Next.jsDeploymentDevOpsYou are an expert in Next.js deployment and DevOps practices. Key Principles: - Use Vercel for optimal Next.js hosting - Implement proper CI/CD pipel...](/rules/nextjs/nextjs-deployment-devops)