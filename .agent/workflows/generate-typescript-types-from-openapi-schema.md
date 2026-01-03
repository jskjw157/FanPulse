---
description: Auto-generate type-safe API client from OpenAPI/Swagger spec
---

# Generate TypeScript Types from API


TypeScriptAPICodegenDXDownloadCopy Workflow---

1. \*\*Get Your API Schema\*\*:
 - Most APIs expose OpenAPI spec at `/swagger.json` or `/openapi.json`.
 - Download it or use the URL directly.

2. \*\*Install openapi-typescript\*\*:
 - Best tool for generating types.
 // turbo
 - Run `npm install --save-dev openapi-typescript`

3. \*\*Generate Types\*\*:
 - Create types from schema URL.
 // turbo
 - Run `npx openapi-typescript https://api.example.com/openapi.json -o src/types/api.ts`

4. \*\*Use Generated Types\*\*:
 - Import and use in your API calls.
```
import type { paths } from './types/api';

   type UserResponse = paths['/users']['get']['responses']['200']['content']['application/json'];

   async function getUsers(): Promise<UserResponse> {
     const res = await fetch('/api/users');
     return res.json();
   }
```

5. \*\*Auto-Regenerate on Change\*\*:
 - Add script to package.json.
```
{
     "scripts": {
       "generate:types": "openapi-typescript https://api.example.com/openapi.json -o src/types/api.ts"
     }
   }
```

6. \*\*Alternative: tRPC\*\*:
 - For Next.js apps, use tRPC for end-to-end type safety.
 // turbo
 - Run `npm install @trpc/server @trpc/client @trpc/react-query @trpc/next`

7. \*\*Pro Tips\*\*:
 - Run `npm run generate:types` before starting development.
 - Commit generated types to git for team consistency.
 - Use `openapi-fetch` for a fully typed fetch wrapper.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `generate-typescript-types-from-openapi-schema.md`
4. In Antigravity, type `/generate_typescript_types_from_openapi_schema` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### VS Code Settings Sync

VS CodeDXConfig--- description: Standardize VS Code settings across the team --- 1. \*\*Create settings.json\*\*: - Create `.vscode/settings.json` for workspace-specific settings. // turbo - Run `mkdir -p .vscode && printf '{\n "editor.formatOnSave": true,\n "editor.defaultFormatter": "esbenp.prettier-vsco...](/workflows/local-dev/sync-vscode-settings-extensions-team-consistency)[### Kill Port 3000


ConfigEnvironmentSetup--- description: Safely create a local .env file from .env.example --- 1. \*\*Check for .env.example\*\*: - Ensure the example file exists. // turbo - Run `test -f .env.example && echo "✅ Found .env.example" || echo "❌ .env.example not found"` 2. \*\*Copy to .env.local\*\*: - Create your local...](/workflows/local-dev/generate-local-env-file-from-example)
## Recommended Rules

[View more rules →](/rules)[### NestJS TypeScript Backend Expert

TypeScriptNestJSBackendYou are an expert in NestJS backend development with TypeScript. Key Principles: - Use decorators with proper TypeScript types - Leverage dependency ...](/rules/typescript/nestjs-typescript-backend)[### GraphQL with TypeScript Expert

TypeScriptGraphQLAPIYou are an expert in GraphQL development with TypeScript. Key Principles: - Use GraphQL Code Generator for type safety - Type all resolvers and queri...](/rules/typescript/graphql-typescript-expert)[### TypeScript Node.js Backend Expert

TypeScriptNode.jsBackendYou are an expert in TypeScript Node.js backend development. Key Principles: - Use strict TypeScript configuration - Type all Express middleware and ...](/rules/typescript/typescript-nodejs-backend)