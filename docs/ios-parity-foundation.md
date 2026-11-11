# iOS parity foundation (cheat-sheet for screen ports)

The shared design layer in `:core-ui` now mirrors the iOS WebBlock app (`Theme.swift`, `Components.swift`).
Use these; do not hand-roll colours, radii or buttons in features. All text goes through `stringResource()`.

## Tokens

```kotlin
val wb = MaterialTheme.wb          // import com.kshrd.coreui.theme.wb
wb.brand wb.brandDeep wb.background wb.card wb.text wb.muted wb.border wb.brandSoft wb.soft
wb.green wb.greenSoft wb.amber wb.amberSoft wb.red wb.redSoft wb.purple wb.purpleSoft
wb.canvas       // page canvas: white / 0B1220
wb.buttonBlue   // 0x3F72D0 (primary button + selected chip fill)
wb.isDark
```

`MaterialTheme.colorScheme` is mapped to the same tokens (primary = brand, onSurface = text, onSurfaceVariant = muted,
outline = border, surfaceVariant/surfaceContainer = soft, errorContainer = redSoft, tertiary = green...).
Dark mode follows the system (`AppTheme`).

- `Space.xxs/xs/s/m/screen/l/xl` = 4/8/12/16/18/24/32 dp. Screen horizontal padding = `Space.screen`.
- `Radius.control` = 14dp, `Radius.card` = 24dp. `MaterialTheme.shapes`: medium 14, large 24, extraLarge 32. `PillShape` for capsules.
- Typography (M3 name → iOS): headlineLarge = system 28 bold, headlineSmall/titleLarge = title2 22 bold,
  titleMedium = headline 17 semibold, titleSmall/labelLarge = subheadline semibold 15, bodyLarge = body 17,
  bodyMedium = subheadline 15, bodySmall = footnote 13, labelMedium = caption 12, labelSmall = caption2 11.

## Components (package `com.kshrd.coreui.components.*`)

| Composable | iOS | Notes |
|---|---|---|
| `card.WbCard { }`, `Modifier.wbCard(padding)` | `wbCard()` | r24, card@92%, brand@12% border, soft shadow; fills width |
| `Modifier.glass(shape, tint)`, `glassFill(tint)` | `.glass(in:tint:)` | near-opaque Liquid-Glass stand-in |
| `card.AppCard(onClick, tint, brush)` | – | existing API, now r24 + brand@12% border |
| `decor.AuthBackground { }` / `LiquidBackdrop { }` | `AuthBackground`/`liquidBackdrop()` | canvas + `FloatingDecor`; `showCircle=false` for onboarding |
| `decor.FloatingDecor(showCircle)` | `FloatingDecor` | drift animations stop when system animations are off |
| `decor.BrandMark(size)` | `BrandMark` | blue square, four dots |
| `brand.WebBlocksLogo(height = 48.dp)` | `Image("webblocks-logo")` | iOS: 48 onboarding, 56 sign-in |
| `button.PrimaryButton(text, onClick, enabled, loading)` | `wbPrimary` + `LoadingButton` | 50dp pill, spinner replaces label |
| `button.SecondaryButton(..., destructive)` | `wbSecondary`/`wbDestructive` | |
| `button.GoogleButton(text, onClick)` / `GoogleGlyph` | `wbGoogle` | 54dp white pill |
| `button.AppTextAction` | – | brand text action |
| `card.Pill(text, icon, tint, background, uppercase)` / `ResultPill(succeeded, okLabel, failLabel)` | `Pill`/`ResultPill` | |
| `card.StatusBadge(text, BadgeTone)` | – | now capsule, wb soft colours |
| `card.FilterChip(title, selected, onClick, count)` + `ChipRow { }` | `FilterChip`/`ChipRow` | `PillChip` is an alias |
| `card.WbFlowRow { }` | `FlowLayout` | |
| `card.SegmentedTabs` | – | brand pill slider |
| `text.SectionTitle / InfoRow / DetailField(isLink) / PageHeader(title, subtitle)` | same names | |
| `card.IconTile(icon, tint, container, size=44.dp)` | `IconTile` | |
| `card.AvatarView(initials, size=64.dp)` | `AvatarView` | |
| `card.UsageRow(title, used, limit, leftLabel, nearLimitLabel, limitReachedLabel)` | `UsageDonut` | blue / amber ≥80% / red at limit |
| `form.LabeledField(title, value, onValueChange, placeholder, error, hint, isOptional, isSecure, revealable, readOnly, minLines, keyboardType, capitalization, imeAction, onImeAction)` | `LabeledField` | capsule 50dp; `minLines>1` = r22 multiline. `AppTextField` delegates to it |
| `feedback.LoadingView / ErrorStateView(message, onRetry) / EmptyStateView(title, message, actionLabel, onAction)` | state views | |
| `feedback.WbToastHost(toast: ToastData?, onDismiss)` + `ToastData(kind, title, message)` | `.toast` | auto-dismiss 3.5s; put last in a full-size `Box` |
| `nav.AppBottomBar(items, selectedIndex, onSelect)` | `GlassDock` | notched dock, 92dp tall incl. lift; pad scrolling content by ~100dp |
| `nav.LargeTitleHeader`, `nav.BackTopBar`, `nav.NotificationBell` | page title / back bar | brand-blue title, chevron back |

Not ported yet (do in the screen forks): `OpeningHoursEditor` / `OpeningHoursList`, `CardPressStyle` (spotify hover card),
`appearAnimated` stagger, the onboarding illustrations, `DockShape` is exposed as `nav.DockShape` if needed elsewhere.

## Assets

`webblocks_logo` → `core-ui` (`com.kshrd.coreui.R.drawable.webblocks_logo`).
Everything else is in `features/src/main/res/drawable-nodpi` (`com.kshrd.features.R.drawable.*`), name = iOS asset with `-` → `_`:

- Logos: `bean_brew_logo`, `golden_crumb_logo`, `petal_stem_logo`, `studio_lotus_logo`
- Flowers (Petal & Stem): `blue_lily_stem`, `gift_bouquet`, `lily_bunch`, `spring_tulip_bunch`, `stone_vase`, `table_arrangement`
- Bakery/coffee samples: `sample_banana_bread`, `sample_cinnamon_roll`, `sample_croissant`, `sample_flat_white`,
  `sample_iced_latte`, `sample_matcha_latte`, `sample_pain_au_chocolat`, `sample_sourdough`
- Yoga: `sample_yoga_dropin`, `sample_yoga_monthly`, `sample_yoga_pass`, `sample_yoga_private`

(`AppIcon.png` was not wired as the launcher icon.)

## Conventions

- `PreviewSurface` + `@ThemePreviews` for every component (already done for the ones above).
- Old call sites kept compiling: `WebBlocksLogo(markSize=)` is a legacy alias of `height`; `wordmarkSize` was removed;
  `features/.../AuthBackdrop` now delegates to `AuthBackground`.
