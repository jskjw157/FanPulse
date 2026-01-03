---
description: Complete checklist for sitemap, robots, manifest, and JSON-LD
---

# Ultimate Next.js SEO Setup


Next.jsSEOProductionMetadataDownloadCopy Workflow---

1. \*\*Metadata Base (Crucial)\*\*:
 - In `src/app/layout.tsx`, define `metadataBase` to resolve relative URLs.
```
export const metadata: Metadata = {
     metadataBase: new URL('https://acme.com'),
     title: 'Acme Corp',
     // ...
   };
```

2. \*\*Dynamic Sitemap (`sitemap.ts`)\*\*:
 - Create `src/app/sitemap.ts`.
```
import { MetadataRoute } from 'next';

   export default function sitemap(): MetadataRoute.Sitemap {
     return [
       {
         url: 'https://acme.com',
         lastModified: new Date(),
         changeFrequency: 'yearly',
         priority: 1,
       },
       // Add dynamic routes here
     ];
   }
```

3. \*\*Robots.txt (`robots.ts`)\*\*:
 - Create `src/app/robots.ts`.
```
import { MetadataRoute } from 'next';

   export default function robots(): MetadataRoute.Robots {
     return {
       rules: {
         userAgent: '*',
         allow: '/',
         disallow: '/private/',
       },
       sitemap: 'https://acme.com/sitemap.xml',
     };
   }
```

4. \*\*JSON-LD Structured Data\*\*:
 - Add structured data to your `layout.tsx`.
```
<script
     type="application/ld+json"
     dangerouslySetInnerHTML={{
       __html: JSON.stringify({
         '@context': 'https://schema.org',
         '@type': 'Organization',
         name: 'Acme Corp',
         url: 'https://acme.com',
         logo: 'https://acme.com/logo.png',
       }),
     }}
   />
```

5. \*\*Pro Tips\*\*:
 - Use \*\*Open Graph\*\* image generation (`opengraph-image.tsx`).
 - Verify with \*\*Google Search Console\*\*.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `setup-nextjs-seo-sitemap-robots-jsonld.md`
4. In Antigravity, type `/setup_nextjs_seo_sitemap_robots_jsonld` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Core Web Vitals Optimizer

PerformanceLCPCLS+1--- description: Audit and fix LCP, CLS, and INP issues for better ranking --- 1. \*\*Fix LCP (Large Contentful Paint)\*\*: - The largest element (usually the hero image) must load fast. - \*\*Fix:\*\* Add `priority` to your Hero image. ```tsx <Image src="/hero.png" alt="Hero" width={800} heigh...](/workflows/production/optimize-core-web-vitals-lcp-cls-inp)[### Security Hardening Checklist

SecurityHeadersCSP+1--- description: Essential security headers, CSP, and rate limiting --- 1. \*\*Security Headers (`next.config.js`)\*\*: - Add these headers to prevent common attacks. ```js module.exports = { async headers() { return [ { source: '/:path\*', headers: [ ...](/workflows/production/security-hardening-headers-csp-rate-limiting)[### Implement Rate Limiting

SecurityRate LimitingAPI--- description: Protect APIs with rate limits --- 1. \*\*Install Upstash\*\*: // turbo - Run `npm install @upstash/ratelimit @upstash/redis` 2. \*\*Setup\*\*: ```ts import { Ratelimit } from '@upstash/ratelimit'; const ratelimit = new Ratelimit({ redis, limiter: Ratelimit.sli...](/workflows/production/implement-api-rate-limiting-upstash-redis)
## Recommended Rules

[View more rules →](/rules)[### Next.js SEO & Metadata Expert

Next.jsSEOMetadataYou are an expert in Next.js SEO and metadata optimization. Key Principles: - Use Metadata API for SEO - Implement dynamic metadata - Use proper Open...](/rules/nextjs/nextjs-seo-metadata)[### Next.js App Router Best Practices

Next.jsApp RouterRoutingYou are an expert in Next.js App Router. Key Principles: - Use Server Components by default - Use Client Components only when necessary (interactivit...](/rules/nextjs/nextjs-app-router-best-practices)[### Next.js Performance Optimization

Next.jsPerformanceOptimizationYou are an expert in Next.js performance optimization. Key Principles: - Optimize images and fonts - Minimize client-side JavaScript - Use proper cac...](/rules/nextjs/nextjs-performance-optimization)