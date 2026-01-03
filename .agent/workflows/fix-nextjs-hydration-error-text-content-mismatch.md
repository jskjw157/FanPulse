---
description: Systematically debug and fix 'Text content does not match server-rendered HTML' errors
---

# Fix Next.js Hydration Errors


Next.jsDebuggingHydrationReactDownloadCopy Workflow---

1. \*\*Check for Invalid HTML Nesting\*\*:
 - The most common cause is invalid HTML, like a `<div>` inside a `<p>` tag.
 - \*\*React 19 Update:\*\* React 19 provides much better hydration error diffs. Check the console for the exact mismatch location.
 - \*\*Bad:\*\* `<p><div>Content</div></p>`
 - \*\*Good:\*\* `<div><div>Content</div></div>` or `<p><span>Content</span></p>`

2. \*\*Handle Random Values (Dates, Math.random)\*\*:
 - If you render data that changes between server and client (like `new Date()` or `Math.random()`), it will cause a mismatch.
 - \*\*Fix:\*\* Use a `useEffect` to set the value only on the client.
```
const [mounted, setMounted] = useState(false);
   useEffect(() => setMounted(true), []);
   if (!mounted) return null; // or a loading skeleton
```

3. \*\*Fix Browser-Only APIs\*\*:
 - Accessing `window` or `localStorage` during the initial render will fail on the server.
 - \*\*Fix:\*\* Ensure these are only accessed inside `useEffect` or event handlers.

4. \*\*Suppress Warning (Last Resort)\*\*:
 - If you absolutely cannot fix the mismatch (e.g., a timestamp that \*must\* be dynamic), you can suppress the warning on a specific element.
```
<div suppressHydrationWarning>
     {new Date().toLocaleTimeString()}
   </div>
```

5. \*\*Pro Tips\*\*:
 - Use the \*\*React DevTools\*\* Profiler to see exactly what props are changing.
 - Check your browser extensions; sometimes they inject HTML that messes with hydration (try Incognito mode).By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `fix-nextjs-hydration-error-text-content-mismatch.md`
4. In Antigravity, type `/fix_nextjs_hydration_error_text_content_mismatch` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Debugging Infinite Re-renders

ReactDebuggingPerformance+1--- description: Track down and fix infinite loops in useEffect and component rendering --- 1. \*\*Check `useEffect` Dependencies\*\*: - The most common culprit is a `useEffect` that updates a state variable which is also in its dependency array. - \*\*Bad Pattern:\*\* ```tsx useEffect(() =...](/workflows/emergency/debug-react-infinite-rerenders-useeffect-loop)[### Nuke & Reinstall

npmTroubleshootingDependencies+1--- description: The nuclear option for when dependencies are completely broken --- 1. \*\*Remove node\_modules\*\*: - Delete the existing `node\_modules` folder to clear installed packages. // turbo - Run `rm -rf node\_modules` 2. \*\*Remove Lock File\*\*: - Delete `package-lock.json`, `yarn.loc...](/workflows/emergency/nuke-node-modules-and-reinstall-dependencies)
## Recommended Rules

[View more rules →](/rules)[### 🐛 Debugging Agent - Systematic Bug Hunter

Agentic AIDebuggingTroubleshootingYou are an expert debugging agent specialized in systematic bug hunting and root cause analysis. Apply rigorous reasoning to identify, isolate, and fi...](/rules/agentic-ai/debugging-agent)[### Next.js App Router Best Practices

Next.jsApp RouterRoutingYou are an expert in Next.js App Router. Key Principles: - Use Server Components by default - Use Client Components only when necessary (interactivit...](/rules/nextjs/nextjs-app-router-best-practices)[### Next.js Performance Optimization

Next.jsPerformanceOptimizationYou are an expert in Next.js performance optimization. Key Principles: - Optimize images and fonts - Minimize client-side JavaScript - Use proper cac...](/rules/nextjs/nextjs-performance-optimization)