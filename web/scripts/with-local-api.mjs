#!/usr/bin/env node
/**
 * Runs a Next.js command with local API defaults, without overriding explicit settings.
 *
 * Next.js inlines NEXT_PUBLIC_API_BASE_URL into the client bundle and evaluates the
 * `rewrites()` proxy target at build time, so both must be present for the build that
 * will be served locally. `npm run dev:local | build:local | start:local` use this wrapper.
 */
import { spawn } from "node:child_process";
import { createRequire } from "node:module";
import path from "node:path";

const require = createRequire(import.meta.url);

const [command, ...args] = process.argv.slice(2);
if (!command) {
  console.error("usage: node scripts/with-local-api.mjs <next-command> [...args]");
  process.exit(2);
}

const env = {
  ...process.env,
  NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || "/api/v1",
  API_PROXY_TARGET: process.env.API_PROXY_TARGET || "http://localhost:8080",
};

console.log(
  `[local-api] NEXT_PUBLIC_API_BASE_URL=${env.NEXT_PUBLIC_API_BASE_URL} ` +
    `API_PROXY_TARGET=${env.API_PROXY_TARGET}`
);

const nextPkg = require.resolve("next/package.json");
const nextBin = path.join(path.dirname(nextPkg), "dist", "bin", "next");

const child = spawn(process.execPath, [nextBin, command, ...args], {
  stdio: "inherit",
  env,
});

child.on("exit", (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }
  process.exit(code ?? 1);
});
