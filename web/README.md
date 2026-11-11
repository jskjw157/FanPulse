This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Getting Started

First, run the development server:

```bash
npm install
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

## API target (local vs production)

`NEXT_PUBLIC_API_BASE_URL` is inlined into the client bundle at **build** time, and the
`/api/v1` rewrite target (`API_PROXY_TARGET`) is resolved by the same build. Both must be set
before `next build` when the build will serve a local stack.

Local (recommended):

```bash
npm run dev:local      # NEXT_PUBLIC_API_BASE_URL=/api/v1, API_PROXY_TARGET=http://localhost:8080
npm run build:local    # production build with the same defaults
npm run start:local    # serves the build produced above
```

Manual equivalent (`web/.env.local`, see `.env.example`):

```bash
NEXT_PUBLIC_API_BASE_URL=/api/v1
# Optional: point the rewrite proxy at a different backend (defaults to localhost:8080)
API_PROXY_TARGET=http://localhost:8080
```

With a relative value, browser requests to `/api/v1/*` stay same-origin and are proxied to the
backend by the Next.js rewrite.

Safety rules when the value is missing or wrong:

- Local page, nothing configured → same-origin `/api/v1` through the rewrite proxy. The client
  never falls back to the production API just because the variable is missing.
- Local page, explicitly configured with a **remote** API (for example the production domain) →
  requests reject with `LocalApiTargetError` before any network call. Rebuild with `/api/v1`.
- Non-local host → previous behaviour is unchanged: an explicit absolute URL is used as-is and an
  unset value still defaults to `https://api.fanpulse.app/api/v1`, so existing deployments are
  unaffected.

Because `NEXT_PUBLIC_*` is inlined, changing it requires a new build, not just a restart.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
