---
description: Verify SSL certificate validity and expiration
---

# Check SSL Certificates


SecurityDevOpsSSLDownloadCopy Workflow---

1. \*\*Check Expiry\*\*:
 - Use openssl to check a domain. Replace `google.com` with your domain.
 // turbo
 - Run `echo | openssl s_client -servername google.com -connect google.com:443 2>/dev/null | openssl x509 -noout -dates`

2. \*\*Pro Tips\*\*:
 - Set up a monitoring alert (like UptimeRobot) to notify you 7 days before expiry.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `check-ssl-certificate-expiry-openssl.md`
4. In Antigravity, type `/check_ssl_certificate_expiry_openssl` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Implement Feature Flags

Feature FlagsDeploymentA/B Testing+1--- description: Safely release features with toggles for gradual rollouts --- 1. \*\*Simple Approach: Environment Variables\*\*: - Use env vars for basic flags. ```ts // lib/features.ts export const features = { newDashboard: process.env.NEXT\_PUBLIC\_FEATURE\_NEW\_DASHBOARD === 'true', ...](/workflows/devops/implement-feature-flags-gradual-rollout)[### Implement Blue-Green Deployment

DeploymentDevOpsZero-Downtime--- description: Zero-downtime deploys --- 1. \*\*Setup Two Environments\*\*: - Blue: Current (v1.0) - Green: New (v1.1) 2. \*\*Route Traffic Gradually\*\*: ```ts const rolloutPercent = await get('green\_rollout') || 0; if (Math.random() \* 100 < rolloutPercent) { return NextResponse.rew...](/workflows/devops/implement-blue-green-deployment-strategy)[### Analyze Bundle Size

PerformanceNext.jsOptimization--- description: Visualize and reduce your production build size --- 1. \*\*Install Analyzer\*\*: - Install the Next.js bundle analyzer. // turbo - Run `npm install @next/bundle-analyzer` 2. \*\*Configure next.config.js\*\*: - Wrap your config. ```js const withBundleAnalyzer = require('@...](/workflows/devops/analyze-nextjs-bundle-size-optimization)
## Recommended Rules

[View more rules →](/rules)[### Security & Compliance in DevOps

DevSecOpsSecurityComplianceYou are an expert in DevSecOps, security automation, and compliance. Key Principles: - Shift security left (start early) - Security as Code - Automat...](/rules/devops-infrastructure/devsecops-security-compliance)[### 🔒 Security Audit Agent - Vulnerability Detection

Agentic AISecurityVulnerabilityYou are an expert security audit agent specialized in identifying vulnerabilities and security risks. Apply systematic reasoning following OWASP guide...](/rules/agentic-ai/security-audit-agent)[### 🚀 DevOps & CI/CD Agent - Pipeline Expert

Agentic AIDevOpsCI/CDYou are an expert DevOps and CI/CD agent specialized in designing and implementing robust deployment pipelines and infrastructure. Apply systematic re...](/rules/agentic-ai/devops-cicd-agent)