---
description: # WF.WEB_RESEARCH — Web Scraping and Research Strategy
---

---
description: # WF.WEB_RESEARCH — Web Scraping and Research Strategy
---

# WF.WEB_RESEARCH — Web Scraping and Research Strategy

Purpose: Use `crawl4ai` efficiently to gather external knowledge when standard docs are unavailable.

Consent policy (mandatory):
- If the user provides exact URL(s): you MAY `scrape` those URLs without additional consent.
- If the task requires "searching/browsing" (e.g., “search for X”, “find latest”, “look up sources”):
  - You MUST ask for explicit permission before crawling/searching.
  - You MUST confirm scope (domains or seed URLs) and budget (max_pages) before running `crawl`.

When to use:
- User explicitly requests web research / latest info not covered by doc MCPs.
- Need to inspect a specific website structure for scraping.
- Official docs exist only as web pages and are not retrievable via docfork/context7.

Steps:

0) Consent Gate (Required for Search/Browse)
   - If the user says “search for X” or asks for “latest”:
     - Ask: “I can do web research via crawl4ai. Proceed? If yes, which domains should I prioritize (or provide 3–5 seed URLs)?”
     - Do not run `crawl` until the user explicitly agrees and scope is defined.

1) Target Identification
   - Prefer exact URL(s) provided by the user.
   - Do not guess URLs.
   - If you must discover URLs (search/browse):
     - Only after Step 0 permission is granted.
     - Start from user-approved domains or seed URLs.

2) Single Page Extraction (Preferred)
   - Use `scrape` on one URL.
   - Analyze markdown output.
   - Check if critical info is truncated; if so, scrape a more specific subpage (not a broad crawl yet).

3) Multi-page Crawl (Use with Caution)
   - Only if Step 2 is insufficient.
   - Use `crawl` with strict limits:
     - same-domain only
     - max_pages=3 (default)
   - Avoid crawling search result pages unless the user explicitly requested it.

4) Synthesis (No Raw Dumps)
   - Extract only what is needed:
     - exact steps, configs, API constraints, code snippets
   - Do NOT dump raw markdown to the user.
   - Return a concise summary + actionable next steps.

5) Quality Check
   - Check recency signals (page date/version notes) when available.
   - Cross-check against internal rules and existing implementation constraints.
   - Flag uncertainty explicitly if the page does not provide definitive claims.

Budget guardrails:
- Heavy tool. Default: 1 `scrape` per turn.
- Allow 1 `crawl` per turn only when strictly necessary and scoped.
