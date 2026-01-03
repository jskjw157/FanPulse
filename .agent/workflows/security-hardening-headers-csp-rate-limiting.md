---
description: Essential security headers, CSP, and rate limiting
---

# Security Hardening Checklist


SecurityHeadersCSPProductionDownloadCopy Workflow---

1. \*\*Security Headers (`next.config.js`)\*\*:
 - Add these headers to prevent common attacks.
```
module.exports = {
     async headers() {
       return [
         {
           source: '/:path*',
           headers: [
             { key: 'X-DNS-Prefetch-Control', value: 'on' },
             { key: 'Strict-Transport-Security', value: 'max-age=63072000; includeSubDomains; preload' },
             { key: 'X-Content-Type-Options', value: 'nosniff' },
             { key: 'X-Frame-Options', value: 'SAMEORIGIN' },
             { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' }
           ]
         }
       ]
     }
   }
```

2. \*\*Content Security Policy (CSP)\*\*:
 - Create `src/middleware.ts`.
```
import { NextResponse } from 'next/server';
   import type { NextRequest } from 'next/server';

   export function middleware(request: NextRequest) {
     const nonce = Buffer.from(crypto.randomUUID()).toString('base64');
     const cspHeader =
       default-src 'self';
       script-src 'self' 'nonce-${nonce}' 'strict-dynamic';
       style-src 'self' 'nonce-${nonce}';
       img-src 'self' blob: data:;
       font-src 'self';
       object-src 'none';
       base-uri 'self';
       form-action 'self';
       frame-ancestors 'none';
       upgrade-insecure-requests;
     .replace(/\s{2,}/g, ' ').trim();

     const requestHeaders = new Headers(request.headers);
     requestHeaders.set('x-nonce', nonce);
     requestHeaders.set('Content-Security-Policy', cspHeader);

     const response = NextResponse.next({
       request: {
         headers: requestHeaders,
       },
     });
     response.headers.set('Content-Security-Policy', cspHeader);
     return response;
   }
```

3. \*\*Rate Limiting (API Routes)\*\*:
 - Prevent abuse with simple in-memory rate limiting.
```
// lib/rate-limit.ts
   const rateLimit = new Map();

   export function checkRateLimit(ip: string, limit = 10) {
     const now = Date.now();
     const windowMs = 60 * 1000; // 1 minute
     const record = rateLimit.get(ip) || { count: 0, resetTime: now + windowMs };

     if (now > record.resetTime) {
       record.count = 1;
       record.resetTime = now + windowMs;
     } else {
       record.count++;
     }

     rateLimit.set(ip, record);
     return record.count <= limit;
   }
```

4. \*\*Pro Tips\*\*:
 - Never commit `.env` files.
 - Regularly audit your dependencies: `npm audit fix`.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `security-hardening-headers-csp-rate-limiting.md`
4. In Antigravity, type `/security_hardening_headers_csp_rate_limiting` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Ultimate Next.js SEO Setup

Next.jsSEOProduction+1--- description: Complete checklist for sitemap, robots, manifest, and JSON-LD --- 1. \*\*Metadata Base (Crucial)\*\*: - In `src/app/layout.tsx`, define `metadataBase` to resolve relative URLs. ```tsx export const metadata: Metadata = { metadataBase: new URL('https://acme.com'), titl...](/workflows/production/setup-nextjs-seo-sitemap-robots-jsonld)[### Implement Rate Limiting

SecurityRate LimitingAPI--- description: Protect APIs with rate limits --- 1. \*\*Install Upstash\*\*: // turbo - Run `npm install @upstash/ratelimit @upstash/redis` 2. \*\*Setup\*\*: ```ts import { Ratelimit } from '@upstash/ratelimit'; const ratelimit = new Ratelimit({ redis, limiter: Ratelimit.sli...](/workflows/production/implement-api-rate-limiting-upstash-redis)[### Setup RBAC

SecurityAuthorizationRBAC--- description: Role-based permissions --- 1. \*\*Define Roles\*\*: ```prisma enum Role { USER ADMIN MODERATOR } ``` 2. \*\*Protect Routes\*\*: ```ts if (session?.user?.role !== 'ADMIN') { return Response.json({ error: 'Forbidden' }, { status: 403 }); } ``` 3....](/workflows/production/setup-role-based-access-control-rbac)
## Recommended Rules

[View more rules →](/rules)[### 🔒 Security Audit Agent - Vulnerability Detection

Agentic AISecurityVulnerabilityYou are an expert security audit agent specialized in identifying vulnerabilities and security risks. Apply systematic reasoning following OWASP guide...](/rules/agentic-ai/security-audit-agent)[### Python Security Best Practices

PythonSecurityCryptographyYou are an expert in Python security and secure coding practices. Key Principles: - Never trust user input - Use principle of least privilege - Keep ...](/rules/python/python-security-best-practices)[### Web Security Best Practices Expert

SecurityWeb DevelopmentOWASPYou are an expert in web security and secure coding practices. Key Principles: - Follow OWASP Top 10 guidelines - Implement defense in depth - Valida...](/rules/web-development/web-security-best-practices)