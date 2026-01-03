---
description: Audit and fix LCP, CLS, and INP issues for better ranking
---

# Core Web Vitals Optimizer


PerformanceLCPCLSNext.jsDownloadCopy Workflow---

1. \*\*Fix LCP (Large Contentful Paint)\*\*:
 - The largest element (usually the hero image) must load fast.
 - \*\*Fix:\*\* Add `priority` to your Hero image.
```
<Image src="/hero.png" alt="Hero" width={800} height={600} priority />
```

2. \*\*Fix CLS (Cumulative Layout Shift)\*\*:
 - Elements jumping around as they load cause CLS.
 - \*\*Fix:\*\* Always define `width` and `height` for images (or use `fill` with a parent container).
 - \*\*Fix:\*\* Reserve space for ads or dynamic content using CSS `min-height`.

3. \*\*Optimize Fonts\*\*:
 - Fonts loading late cause layout shifts (FOUT/FOIT).
 - \*\*Fix:\*\* Use `next/font` which automatically optimizes and hosts fonts.
```
import { Inter } from 'next/font/google';
   const inter = Inter({ subsets: ['latin'] });
   // Use inter.className in your body or layout
```

4. \*\*Pro Tips\*\*:
 - Run a \*\*Lighthouse\*\* audit in Chrome DevTools (Incognito mode) to get a baseline score.
 - Use `@next/third-parties` to load scripts like Google Analytics efficiently.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `optimize-core-web-vitals-lcp-cls-inp.md`
4. In Antigravity, type `/optimize_core_web_vitals_lcp_cls_inp` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Ultimate Next.js SEO Setup

Next.jsSEOProduction+1--- description: Complete checklist for sitemap, robots, manifest, and JSON-LD --- 1. \*\*Metadata Base (Crucial)\*\*: - In `src/app/layout.tsx`, define `metadataBase` to resolve relative URLs. ```tsx export const metadata: Metadata = { metadataBase: new URL('https://acme.com'), titl...](/workflows/production/setup-nextjs-seo-sitemap-robots-jsonld)[### Security Hardening Checklist

SecurityHeadersCSP+1--- description: Essential security headers, CSP, and rate limiting --- 1. \*\*Security Headers (`next.config.js`)\*\*: - Add these headers to prevent common attacks. ```js module.exports = { async headers() { return [ { source: '/:path\*', headers: [ ...](/workflows/production/security-hardening-headers-csp-rate-limiting)[### Implement Rate Limiting

SecurityRate LimitingAPI--- description: Protect APIs with rate limits --- 1. \*\*Install Upstash\*\*: // turbo - Run `npm install @upstash/ratelimit @upstash/redis` 2. \*\*Setup\*\*: ```ts import { Ratelimit } from '@upstash/ratelimit'; const ratelimit = new Ratelimit({ redis, limiter: Ratelimit.sli...](/workflows/production/implement-api-rate-limiting-upstash-redis)
## Recommended Rules

[View more rules →](/rules)[### Next.js Performance Optimization

Next.jsPerformanceOptimizationYou are an expert in Next.js performance optimization. Key Principles: - Optimize images and fonts - Minimize client-side JavaScript - Use proper cac...](/rules/nextjs/nextjs-performance-optimization)[### ⚡ Performance Optimization Agent - Speed Expert

Agentic AIPerformanceOptimizationYou are an expert performance optimization agent specialized in identifying and fixing performance bottlenecks. Apply systematic reasoning to measure,...](/rules/agentic-ai/performance-optimization-agent)[### Web Performance Optimization Expert

PerformanceWeb VitalsOptimizationYou are an expert in web performance optimization and Core Web Vitals. Key Principles: - Optimize for Core Web Vitals - Minimize Time to First Byte (...](/rules/web-development/web-performance-optimization)