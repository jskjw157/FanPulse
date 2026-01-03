---
description: Role-based permissions
---

# Setup RBAC


SecurityAuthorizationRBACDownloadCopy Workflow---

1. \*\*Define Roles\*\*:
```
enum Role {
     USER
     ADMIN
     MODERATOR
   }
```

2. \*\*Protect Routes\*\*:
```
if (session?.user?.role !== 'ADMIN') {
     return Response.json({ error: 'Forbidden' }, { status: 403 });
   }
```

3. \*\*Conditional UI\*\*:
```
{isAdmin && <AdminPanel />}
```

4. \*\*Pro Tips\*\*:
 - Use enums for type safety.
 - Cache permissions.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `setup-role-based-access-control-rbac.md`
4. In Antigravity, type `/setup_role_based_access_control_rbac` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Security Hardening Checklist

SecurityHeadersCSP+1--- description: Essential security headers, CSP, and rate limiting --- 1. \*\*Security Headers (`next.config.js`)\*\*: - Add these headers to prevent common attacks. ```js module.exports = { async headers() { return [ { source: '/:path\*', headers: [ ...](/workflows/production/security-hardening-headers-csp-rate-limiting)[### Implement Rate Limiting

SecurityRate LimitingAPI--- description: Protect APIs with rate limits --- 1. \*\*Install Upstash\*\*: // turbo - Run `npm install @upstash/ratelimit @upstash/redis` 2. \*\*Setup\*\*: ```ts import { Ratelimit } from '@upstash/ratelimit'; const ratelimit = new Ratelimit({ redis, limiter: Ratelimit.sli...](/workflows/production/implement-api-rate-limiting-upstash-redis)[### Secure API from CSRF

SecurityCSRFAPI--- description: Prevent CSRF attacks --- 1. \*\*Use SameSite Cookies\*\*: ```ts response.headers.set('Set-Cookie', 'token=abc; SameSite=Strict; HttpOnly'); ``` 2. \*\*Implement CSRF Tokens\*\*: ```ts import { randomBytes } from 'crypto'; export function generateCSRFToken() { return...](/workflows/production/secure-api-csrf-protection-samesite-cookies)
## Recommended Rules

[View more rules →](/rules)[### 🔒 Security Audit Agent - Vulnerability Detection

Agentic AISecurityVulnerabilityYou are an expert security audit agent specialized in identifying vulnerabilities and security risks. Apply systematic reasoning following OWASP guide...](/rules/agentic-ai/security-audit-agent)[### Python Security Best Practices

PythonSecurityCryptographyYou are an expert in Python security and secure coding practices. Key Principles: - Never trust user input - Use principle of least privilege - Keep ...](/rules/python/python-security-best-practices)[### Web Security Best Practices Expert

SecurityWeb DevelopmentOWASPYou are an expert in web security and secure coding practices. Key Principles: - Follow OWASP Top 10 guidelines - Implement defense in depth - Valida...](/rules/web-development/web-security-best-practices)