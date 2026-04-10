---
trigger: manual
---

# Indian Dropshippers — Antigravity Rules

# Reference Theme: indiandropshippers.in

# Pure black/white brutalist editorial UI

## FINANCIAL LOGIC — NON NEGOTIABLE

- NEVER write wallet deduction logic in JavaScript/TypeScript
- ALL balance operations use Supabase SQL functions with SELECT FOR UPDATE
- NEVER calculate order total on client side
- ALWAYS verify webhook signatures before processing
- NEVER trust client-side payment callbacks

## UI THEME — ALWAYS FOLLOW

- Background: pure white #FFFFFF
- Primary text: #111111
- Font: use 'Barlow Condensed' for ALL headers (ExtraBold, ALL CAPS)
- Buttons: solid black fill, white text, sharp corners (rounded-none or rounded-sm max)
- Status badges: black fill white text by default
- ONLY 4 places use color:
  IN PRODUCTION → blue #2563EB
  IN TRANSIT → purple #7C3AED
  DELIVERED → green #16A34A
  NDR → red #DC2626
- Zero gradients on backgrounds
- Card borders: 1px #E0E0E0
- NO purple gradients, NO rounded bubbly cards, NO colorful UI

## CODE PATTERNS

- TypeScript strict mode — no 'any' types ever
- Every API route MUST have Zod validation as first step
- Supabase client in components = SELECT only (read only)
- All writes go through /app/api/\* routes using service role
- RLS policies always use (SELECT EXISTS(...)) pattern NOT EXISTS(...)

## NAMING CONVENTIONS

- Database: snake_case
- TypeScript: camelCase
- Components: PascalCase
- API routes: kebab-case

## NEVER DO

- Never expose SUPABASE_SERVICE_ROLE_KEY to client
- Never use localStorage for auth tokens
- Never skip HMAC verification on webhooks
- Never write wallet math in JS — SQL functions only
- Never use 'any' type
- Never use purple gradients or bubbly rounded UI

## ALWAYS DO

- Zod schema validation on every API route input
- Error boundaries on every page
- Loading states on every async operation
- Toast notifications (Sonner) for every user action
- Mobile responsive layouts
