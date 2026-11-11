# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Small business owners who are not technical (cafés, shops, small agencies). They have one or a few
websites built with WebBlocks and use the app on the go to check on them and make quick edits.

## Product Purpose

WebBlocks Owner is the mobile companion to the WebBlocks web dashboard. An owner signs in, picks one
of their websites, and manages it from three portal tabs: Products, Business (info and opening
hours) and Monitor (site status and activity), plus Notifications. Success means an owner can
check on their site and make a quick change in under a minute without opening a laptop.

## Positioning

It is the same product as the WebBlocks web dashboard, on a phone. An owner who knows the web
dashboard should feel at home immediately.

## Operating Context

- Flow: Splash → Onboarding → Sign in (/ Forgot password) → My Websites → pick a website →
  portal tabs Products · Business · Monitor (+ Notifications).
- The selected website is global (`SessionStore`); switching it from the website chip on any tab
  reloads that tab.
- Website states: Live, Draft, Offline.
- No backend yet; all data comes from in-memory sources (mock accounts in `README.md`).

## Capabilities and Constraints

- Native Android, Kotlin + Jetpack Compose (Material 3), Min SDK 26, light and dark mode follow the system.
- Shared design layer lives in `:core-ui` (`MaterialTheme.wb` tokens, `Space`, `Radius`, shared components);
  features must use it, not hand-roll styles. All text through `stringResource()`.
- A parallel iOS app exists; `docs/ios-parity-foundation.md` maps the shared components.

## Brand Commitments

- Name: WebBlocks. Existing logo (`WebBlocksLogo`) and brand mark (`BrandMark`) stay.
- Brand blue stays (current `wb.brand` #4379D6 family).
- Visual direction must follow the WebBlocks web dashboard (user-confirmed, 2026-11-11).
- Requested change is a refresh of the current look ("feels old"), not a rebrand.

## Evidence on Hand

- Mock data only (Coffee House Live, Fashion Store Draft, Digital Agency Offline). No real
  customer stats, testimonials or metrics; do not invent them.

## Product Principles

1. Same product, smaller screen: match the web dashboard's language before inventing new patterns.
2. Glanceable first: website status and what needs attention are visible without digging.
3. Quick edits, low risk: changes are simple, clearly confirmed, and easy to undo.
4. Plain language for non-technical owners.
