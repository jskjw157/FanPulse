---
description: Automatically fix linting and formatting issues across the project
---

# Fix Lint Errors


LintingESLintPrettierQualityDownloadCopy Workflow---

1. \*\*Run ESLint Fix\*\*:
 - Attempt to automatically fix all fixable ESLint errors.
 // turbo
 - Run `npm run lint -- --fix`

2. \*\*Run Prettier\*\*:
 - Format all files in the project to ensure consistent style.
 // turbo
 - Run `npx prettier --write .`

3. \*\*Pro Tips\*\*:
 - Run this before every commit to keep your codebase clean.
 - Configure your editor to 'Format on Save' for real-time feedback.By Antigravity Team
### How to Use This Workflow

1. Click **"Download"** above
2. In your project, create the directory: `.agent/workflows/`
3. Save the file as `fix-eslint-prettier-linting-errors-automatically.md`
4. In Antigravity, type `/fix_eslint_prettier_linting_errors_automatically` or just describe what you want to do

[Learn more about workflows →](/blog/workflows)

## Related Workflows

[### Setup Prettier & ESLint from Scratch

ESLintPrettierCode Quality+1--- description: Configure linting and formatting (ESLint 9 Flat Config) --- 1. \*\*Install Dependencies\*\*: - Install ESLint, Prettier, and configs. // turbo - Run `npm install --save-dev eslint @eslint/js typescript-eslint prettier eslint-config-prettier eslint-plugin-react-hooks eslint-plu...](/workflows/local-dev/setup-prettier-eslint-typescript-configuration)[### Pre-Flight Check

CI/CDTestingBuild+1--- description: Run type checking, linting, and build verification before pushing --- 1. \*\*Type Check\*\*: - Ensure there are no TypeScript errors. // turbo - Run `npx tsc --noEmit` 2. \*\*Lint Check\*\*: - Verify code quality rules. // turbo - Run `npm run lint` 3. \*\*Build Verificat...](/workflows/local-dev/run-pre-flight-check-type-lint-build)[### Setup Husky Git Hooks

GitAutomationQuality+1--- description: Automate code quality checks with pre-commit and pre-push hooks --- 1. \*\*Install Husky\*\*: - Install husky and lint-staged. // turbo - Run `npm install --save-dev husky lint-staged` 2. \*\*Initialize Husky\*\*: - Set up git hooks. // turbo - Run `npx husky init` 3. \*...](/workflows/local-dev/setup-husky-git-hooks-pre-commit-linting)
## Recommended Rules

[View more rules →](/rules)[### Rust Testing & Benchmarking

RustTestingBenchmarkingYou are an expert in Rust testing, benchmarking, and quality assurance. Key Principles: - Tests are first-class citizens in Rust - Unit tests go in t...](/rules/rust/rust-testing-benchmarking)[### Rust Performance Optimization

RustPerformanceOptimizationYou are an expert in optimizing Rust code for performance. Key Principles: - Zero-cost abstractions - Memory layout matters - Minimize allocations - ...](/rules/rust/rust-performance-optimization)[### Rust WebAssembly (WASM) Development

RustWebAssemblyWASMYou are an expert in compiling Rust to WebAssembly and interacting with JavaScript. Key Principles: - Rust is a first-class language for WASM - High ...](/rules/rust/rust-webassembly-development)