---
description: Track down and fix infinite loops in useEffect and component rendering
---

# Debugging Infinite Re-renders


ReactDebuggingPerformanceHooksDownloadCopy Workflow---

1. \*\*Check `useEffect` Dependencies\*\*:
 - The most common culprit is a `useEffect` that updates a state variable which is also in its dependency array.
 - \*\*Bad Pattern:\*\*
```
useEffect(() => {
       setCount(count + 1);
     }, [count]); // Depends on 'count' -> Infinite Loop!
```

 - \*\*Fix:\*\* Use the functional update form or remove the dependency if not needed.

2. \*\*Unstable Object References\*\*:
 - If you pass an object or array as a dependency, React compares it by reference. Creating a new object on every render causes the effect to run every time.
 - \*\*Fix:\*\* Wrap the object in `useMemo` or move it outside the component.
```
const options = useMemo(() => ({ id: 1 }), []);
```

3. \*\*Use `useTraceUpdate` Hook\*\*:
 - Copy this hook to debug exactly which prop is changing.
```
function useTraceUpdate(props) {
     const prev = useRef(props);
     useEffect(() => {
       const changedProps = Object.entries(props).reduce((ps, [k, v]) => {
         if (prev.current[k] !== v) ps[k] = [prev.current[k], v];
         return ps;
       }, {});
       if (Object.keys(changedProps).length > 0) {
         console.log('Changed props:', changedProps);
       }
       prev.current = props;
     });
   }
```

4. \*\*Pro Tips\*\*:
 - Install the \*\*eslint-plugin-react-hooks\*\* package. It will automatically warn you about missing or circular dependencies.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `debug-react-infinite-rerenders-useeffect-loop.md`
4. In Antigravity, type `/debug_react_infinite_rerenders_useeffect_loop` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Fix Next.js Hydration Errors

Next.jsDebuggingHydration+1--- description: Systematically debug and fix 'Text content does not match server-rendered HTML' errors --- 1. \*\*Check for Invalid HTML Nesting\*\*: - The most common cause is invalid HTML, like a `<div>` inside a `<p>` tag. - \*\*React 19 Update:\*\* React 19 provides much better hydration error d...](/workflows/emergency/fix-nextjs-hydration-error-text-content-mismatch)[### Nuke & Reinstall

npmTroubleshootingDependencies+1--- description: The nuclear option for when dependencies are completely broken --- 1. \*\*Remove node\_modules\*\*: - Delete the existing `node\_modules` folder to clear installed packages. // turbo - Run `rm -rf node\_modules` 2. \*\*Remove Lock File\*\*: - Delete `package-lock.json`, `yarn.loc...](/workflows/emergency/nuke-node-modules-and-reinstall-dependencies)
## Recommended Rules

[View more rules →](/rules)[### React Hooks Best Practices

ReactHooksBest PracticesYou are an expert in React Hooks. Key Principles: - Follow Rules of Hooks strictly - Use custom hooks for reusable logic - Optimize dependency arrays...](/rules/react/react-hooks-best-practices)[### React Performance Optimization

ReactPerformanceOptimizationYou are an expert in React performance optimization. Key Principles: - Measure before optimizing - Minimize re-renders - Optimize bundle size - Use c...](/rules/react/react-performance-optimization)[### React Hook Generator Workflow

WorkflowsReactHooksScaffold custom React hooks with standard boilerplate code. Workflow File: .agent/workflows/create\_hook.md ```markdown --- description: Create a new...](/rules/antigravity-workflows/react-hook-generator-workflow)