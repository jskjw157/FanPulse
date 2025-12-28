---
description: # WF.DETAIL_PAGE_FACTORY — Product Detail Page Builder Purpose: Generate consistent, reusable detail pages with minimal tokens.
---

# WF.DETAIL_PAGE_FACTORY — Product Detail Page Builder
Purpose: Generate consistent, reusable detail pages with minimal tokens.

Input schema (per product):
- product_name
- category
- top benefits (3 bullets)
- materials
- size/spec (do not guess)
- care/washing
- shipping/returns (prefer shared boilerplate)

Rules:
- Never invent unknown specs. Use placeholders: [NEED: ...]
- Avoid absolute claims unless user provided verified claims.
- Reuse boilerplate blocks for shipping/returns/FAQ.

Output:
1) Title + short intro
2) “Why you’ll love it” (3 bullets)
3) Specs table
4) Care guide
5) FAQ (3 Q/A)
6) CTA

Cafe24-friendly HTML:
- Basic tags only: div/h2/h3/p/ul/li/table
- No external JS/CSS unless explicitly requested
