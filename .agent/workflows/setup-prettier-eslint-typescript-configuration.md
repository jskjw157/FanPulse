---
description: Configure linting and formatting (ESLint 9 Flat Config)
---

# Setup Prettier & ESLint from Scratch


ESLintPrettierCode QualitySetupDownloadCopy Workflow---

1. \*\*Install Dependencies\*\*:
 - Install ESLint, Prettier, and configs.
 // turbo
 - Run `npm install --save-dev eslint @eslint/js typescript-eslint prettier eslint-config-prettier eslint-plugin-react-hooks eslint-plugin-react-refresh globals`

2. \*\*Create `eslint.config.js` (Flat Config)\*\*:
 - The new standard for ESLint 9.
```
import js from '@eslint/js';
   import globals from 'globals';
   import reactHooks from 'eslint-plugin-react-hooks';
   import reactRefresh from 'eslint-plugin-react-refresh';
   import tseslint from 'typescript-eslint';

   export default tseslint.config(
     { ignores: ['dist', '.next'] },
     {
       extends: [js.configs.recommended, ...tseslint.configs.recommended],
       files: ['**/*.{ts,tsx}'],
       languageOptions: {
         ecmaVersion: 2020,
         globals: globals.browser,
       },
       plugins: {
         'react-hooks': reactHooks,
         'react-refresh': reactRefresh,
       },
       rules: {
         ...reactHooks.configs.recommended.rules,
         'react-refresh/only-export-components': [
           'warn',
           { allowConstantExport: true },
         ],
       },
     },
   );
```

3. \*\*Create `.prettierrc`\*\*:
```
{
     "semi": true,
     "singleQuote": true,
     "tabWidth": 2,
     "trailingComma": "es5",
     "printWidth": 100,
     "plugins": ["prettier-plugin-tailwindcss"]
   }
```

4. \*\*Add Scripts\*\*:
```
{
     "scripts": {
       "lint": "eslint .",
       "lint:fix": "eslint . --fix",
       "format": "prettier --write ."
     }
   }
```

5. \*\*Pro Tips\*\*:
 - Install VS Code extensions: ESLint, Prettier.
 - Enable "Format on Save" in VS Code settings.
 - ESLint 9 is a major change; old `.eslintrc` files are deprecated.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `setup-prettier-eslint-typescript-configuration.md`
4. In Antigravity, type `/setup_prettier_eslint_typescript_configuration` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Fix Lint Errors

LintingESLintPrettier+1--- description: Automatically fix linting and formatting issues across the project --- 1. \*\*Run ESLint Fix\*\*: - Attempt to automatically fix all fixable ESLint errors. // turbo - Run `npm run lint -- --fix` 2. \*\*Run Prettier\*\*: - Format all files in the project to ensure consistent st...](/workflows/local-dev/fix-eslint-prettier-linting-errors-automatically)[### Generate .env from Example

ConfigEnvironmentSetup--- description: Safely create a local .env file from .env.example --- 1. \*\*Check for .env.example\*\*: - Ensure the example file exists. // turbo - Run `test -f .env.example && echo "✅ Found .env.example" || echo "❌ .env.example not found"` 2. \*\*Copy to .env.local\*\*: - Create your local...](/workflows/local-dev/generate-local-env-file-from-example)[### Setup Husky Git Hooks

GitAutomationQuality+1--- description: Automate code quality checks with pre-commit and pre-push hooks --- 1. \*\*Install Husky\*\*: - Install husky and lint-staged. // turbo - Run `npm install --save-dev husky lint-staged` 2. \*\*Initialize Husky\*\*: - Set up git hooks. // turbo - Run `npx husky init` 3. \*...](/workflows/local-dev/setup-husky-git-hooks-pre-commit-linting)
## Recommended Rules

[View more rules →](/rules)[### 🔄 Refactoring Agent - Safe Code Improvement

Agentic AIRefactoringClean CodeYou are an expert refactoring agent specialized in safely improving code quality without changing behavior. Apply systematic reasoning to identify ref...](/rules/agentic-ai/refactoring-agent)[### Strong Reasoner & Planner Agent (Official Google Template)

Agentic AIReasoningPlanningYou are a very strong reasoner and planner. Use these critical instructions to structure your plans, thoughts, and responses. 📋 Source: Google Gemin...](/rules/agentic-ai/strong-reasoner-planner-agent)[### 🤖 AI Prompt Engineer Agent - LLM Expert

Agentic AIPrompt EngineeringLLMYou are an expert AI prompt engineer agent specialized in crafting effective prompts for Large Language Models. Apply systematic reasoning to design p...](/rules/agentic-ai/ai-prompt-engineer-agent)