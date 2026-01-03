---
description: Prevent CSRF attacks
---

# Secure API from CSRF


SecurityCSRFAPIDownloadCopy Workflow---

1. \*\*Use SameSite Cookies\*\*:
```
response.headers.set('Set-Cookie', 'token=abc; SameSite=Strict; HttpOnly');
```

2. \*\*Implement CSRF Tokens\*\*:
```
import { randomBytes } from 'crypto';
   export function generateCSRFToken() {
     return randomBytes(32).toString('hex');
   }
```

3. \*\*Validate Origin\*\*:
```
const origin = request.headers.get('origin');
   if (!allowedOrigins.includes(origin)) {
     return Response.json({ error: 'Invalid origin' }, { status: 403 });
   }
```

4. \*\*Pro Tips\*\*:
 - Never use `*` in production.
 - Validate both token and origin.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `secure-api-csrf-protection-samesite-cookies.md`
4. In Antigravity, type `/secure_api_csrf_protection_samesite_cookies` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Implement Rate Limiting

SecurityRate LimitingAPI--- description: Protect APIs with rate limits --- 1. \*\*Install Upstash\*\*: // turbo - Run `npm install @upstash/ratelimit @upstash/redis` 2. \*\*Setup\*\*: ```ts import { Ratelimit } from '@upstash/ratelimit'; const ratelimit = new Ratelimit({ redis, limiter: Ratelimit.sli...](/workflows/production/implement-api-rate-limiting-upstash-redis)[### Security Hardening Checklist

SecurityHeadersCSP+1--- description: Essential security headers, CSP, and rate limiting --- 1. \*\*Security Headers (`next.config.js`)\*\*: - Add these headers to prevent common attacks. ```js module.exports = { async headers() { return [ { source: '/:path\*', headers: [ ...](/workflows/production/security-hardening-headers-csp-rate-limiting)[### Setup RBAC

SecurityAuthorizationRBAC--- description: Role-based permissions --- 1. \*\*Define Roles\*\*: ```prisma enum Role { USER ADMIN MODERATOR } ``` 2. \*\*Protect Routes\*\*: ```ts if (session?.user?.role !== 'ADMIN') { return Response.json({ error: 'Forbidden' }, { status: 403 }); } ``` 3....](/workflows/production/setup-role-based-access-control-rbac)
## Recommended Rules

[View more rules →](/rules)[### 🔒 Security Audit Agent - Vulnerability Detection

Agentic AISecurityVulnerabilityYou are an expert security audit agent specialized in identifying vulnerabilities and security risks. Apply systematic reasoning following OWASP guide...](/rules/agentic-ai/security-audit-agent)[### Python Security Best Practices

PythonSecurityCryptographyYou are an expert in Python security and secure coding practices. Key Principles: - Never trust user input - Use principle of least privilege - Keep ...](/rules/python/python-security-best-practices)[### Web Security Best Practices Expert

SecurityWeb DevelopmentOWASPYou are an expert in web security and secure coding practices. Key Principles: - Follow OWASP Top 10 guidelines - Implement defense in depth - Valida...](/rules/web-development/web-security-best-practices)