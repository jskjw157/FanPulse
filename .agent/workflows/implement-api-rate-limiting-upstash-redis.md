---
description: Protect APIs with rate limits
---

# Implement Rate Limiting


SecurityRate LimitingAPIDownloadCopy Workflow---

1. \*\*Install Upstash\*\*:
 // turbo
 - Run `npm install @upstash/ratelimit @upstash/redis`

2. \*\*Setup\*\*:
```
import { Ratelimit } from '@upstash/ratelimit';

   const ratelimit = new Ratelimit({
     redis,
     limiter: Ratelimit.slidingWindow(10, '10 s')
   });
```

3. \*\*Apply to Routes\*\*:
```
const { success } = await ratelimit.limit(ip);
   if (!success) return Response.json({ error: 'Too many requests' }, { status: 429 });
```

4. \*\*Pro Tips\*\*:
 - Different limits per endpoint.
 - Log violations.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `implement-api-rate-limiting-upstash-redis.md`
4. In Antigravity, type `/implement_api_rate_limiting_upstash_redis` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Secure API from CSRF

SecurityCSRFAPI--- description: Prevent CSRF attacks --- 1. \*\*Use SameSite Cookies\*\*: ```ts response.headers.set('Set-Cookie', 'token=abc; SameSite=Strict; HttpOnly'); ``` 2. \*\*Implement CSRF Tokens\*\*: ```ts import { randomBytes } from 'crypto'; export function generateCSRFToken() { return...](/workflows/production/secure-api-csrf-protection-samesite-cookies)[### Security Hardening Checklist

SecurityHeadersCSP+1--- description: Essential security headers, CSP, and rate limiting --- 1. \*\*Security Headers (`next.config.js`)\*\*: - Add these headers to prevent common attacks. ```js module.exports = { async headers() { return [ { source: '/:path\*', headers: [ ...](/workflows/production/security-hardening-headers-csp-rate-limiting)[### Setup RBAC

SecurityAuthorizationRBAC--- description: Role-based permissions --- 1. \*\*Define Roles\*\*: ```prisma enum Role { USER ADMIN MODERATOR } ``` 2. \*\*Protect Routes\*\*: ```ts if (session?.user?.role !== 'ADMIN') { return Response.json({ error: 'Forbidden' }, { status: 403 }); } ``` 3....](/workflows/production/setup-role-based-access-control-rbac)
## Recommended Rules

[View more rules →](/rules)[### 🔒 Security Audit Agent - Vulnerability Detection

Agentic AISecurityVulnerabilityYou are an expert security audit agent specialized in identifying vulnerabilities and security risks. Apply systematic reasoning following OWASP guide...](/rules/agentic-ai/security-audit-agent)[### Python Security Best Practices

PythonSecurityCryptographyYou are an expert in Python security and secure coding practices. Key Principles: - Never trust user input - Use principle of least privilege - Keep ...](/rules/python/python-security-best-practices)[### Web Security Best Practices Expert

SecurityWeb DevelopmentOWASPYou are an expert in web security and secure coding practices. Key Principles: - Follow OWASP Top 10 guidelines - Implement defense in depth - Valida...](/rules/web-development/web-security-best-practices)