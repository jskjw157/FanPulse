---
description: The nuclear option for when dependencies are completely broken
---

# Nuke & Reinstall


npmTroubleshootingDependenciesTurboDownloadCopy Workflow---

1. \*\*Remove node\_modules\*\*:
 - Delete the existing `node_modules` folder to clear installed packages.
 // turbo
 - Run `rm -rf node_modules`

2. \*\*Remove Lock File\*\*:
 - Delete `package-lock.json`, `yarn.lock`, `pnpm-lock.yaml`, or `bun.lockb`.
 // turbo
 - Run `rm package-lock.json yarn.lock pnpm-lock.yaml bun.lockb`

3. \*\*Clean Cache\*\*:
 - Clean the package manager cache.
 // turbo
 - Run `npm cache clean --force`

4. \*\*Reinstall Dependencies\*\*:
 - Install dependencies from scratch.
 // turbo
 - Run `npm install`

5. \*\*Pro Tips\*\*:
 - \*\*Yarn:\*\* `yarn install`
 - \*\*pnpm:\*\* `pnpm install`
 - \*\*Bun:\*\* `bun install`
 - Restart your VS Code window after this to ensure the TypeScript server picks up the new modules.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `nuke-node-modules-and-reinstall-dependencies.md`
4. In Antigravity, type `/nuke_node_modules_and_reinstall_dependencies` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Fix Next.js Hydration Errors

Next.jsDebuggingHydration+1--- description: Systematically debug and fix 'Text content does not match server-rendered HTML' errors --- 1. \*\*Check for Invalid HTML Nesting\*\*: - The most common cause is invalid HTML, like a `<div>` inside a `<p>` tag. - \*\*React 19 Update:\*\* React 19 provides much better hydration error d...](/workflows/emergency/fix-nextjs-hydration-error-text-content-mismatch)[### Debugging Infinite Re-renders

ReactDebuggingPerformance+1--- description: Track down and fix infinite loops in useEffect and component rendering --- 1. \*\*Check `useEffect` Dependencies\*\*: - The most common culprit is a `useEffect` that updates a state variable which is also in its dependency array. - \*\*Bad Pattern:\*\* ```tsx useEffect(() =...](/workflows/emergency/debug-react-infinite-rerenders-useeffect-loop)
## Recommended Rules

[View more rules →](/rules)[### 🐛 Debugging Agent - Systematic Bug Hunter

Agentic AIDebuggingTroubleshootingYou are an expert debugging agent specialized in systematic bug hunting and root cause analysis. Apply rigorous reasoning to identify, isolate, and fi...](/rules/agentic-ai/debugging-agent)[### 📦 Code Migration Agent - Safe Upgrade Expert

Agentic AIMigrationUpgradeYou are an expert code migration agent specialized in safely upgrading frameworks, languages, and dependencies. Apply systematic reasoning to plan and...](/rules/agentic-ai/code-migration-agent)[### Dependency Reset Workflow

WorkflowsNode.jsTroubleshootingFix "it works on my machine" issues by resetting dependencies. Workflow File: .agent/workflows/reset\_deps.md ```markdown --- description: Completely...](/rules/antigravity-workflows/dependency-reset-workflow)