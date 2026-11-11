@file:Suppress("MagicNumber", "MaxLineLength", "ktlint:standard:max-line-length")

package com.kshrd.coreui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp

// GENERATED from the web dashboard's Figma icons ("Local-WebBlock-UI") by gen-webicons.mjs.
// Paths and stroke weights are copied exactly; edit the Figma icon and regenerate rather than hand-editing.

private val Ink = SolidColor(Color.Black)

private inline fun icon(
    name: String,
    viewportWidth: Float,
    viewportHeight: Float,
    block: ImageVector.Builder.() -> Unit,
): ImageVector =
    ImageVector
        .Builder(
            name = "WebIcons.$name",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
        ).apply(block)
        .build()

/**
 * The web dashboard's icon set, so the app's icons match the website exactly. Monochrome: tint them with
 * `Icon(WebIcons.Package, contentDescription = …, tint = …)` like any Material icon.
 */
object WebIcons {
    /** Web dashboard `icon/package`. */
    val Package: ImageVector by lazy {
        icon("Package", 22f, 22f) {
            addPath(pathData = addPathNodes("M10.0833 19.9192C10.362 20.0801 10.6782 20.1648 11 20.1648C11.3218 20.1648 11.638 20.0801 11.9167 19.9192L18.3333 16.2525C18.6118 16.0918 18.843 15.8606 19.0039 15.5823C19.1648 15.3039 19.2497 14.9882 19.25 14.6667V7.33333C19.2497 7.01183 19.1648 6.69608 19.0039 6.41773C18.843 6.13939 18.6118 5.90825 18.3333 5.7475L11.9167 2.08083C11.638 1.91992 11.3218 1.83521 11 1.83521C10.6782 1.83521 10.362 1.91992 10.0833 2.08083L3.66667 5.7475C3.38824 5.90825 3.15698 6.13939 2.99609 6.41773C2.8352 6.69608 2.75033 7.01183 2.75 7.33333V14.6667C2.75033 14.9882 2.8352 15.3039 2.99609 15.5823C3.15698 15.8606 3.38824 16.0918 3.66667 16.2525L10.0833 19.9192Z"), stroke = Ink, strokeLineWidth = 1.8333f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M11 20.1667V11"), stroke = Ink, strokeLineWidth = 1.8333f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M3.025 6.41667L10.0861 10.7562C10.3641 10.9161 10.6793 11.0002 11 11.0002C11.3207 11.0002 11.6359 10.9161 11.9139 10.7562L18.975 6.41667"), stroke = Ink, strokeLineWidth = 1.8333f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/store`. */
    val Store: ImageVector by lazy {
        icon("Store", 15f, 15f) {
            addPath(pathData = addPathNodes("M1.25 4.375L4.00625 1.61875C4.12254 1.50177 4.26082 1.40896 4.41314 1.34567C4.56547 1.28239 4.72881 1.24987 4.89375 1.25H10.1062C10.2712 1.24987 10.4345 1.28239 10.5869 1.34567C10.7392 1.40896 10.8775 1.50177 10.9937 1.61875L13.75 4.375"), stroke = Ink, strokeLineWidth = 1.25f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M2.5 7.5V12.5C2.5 12.8315 2.6317 13.1495 2.86612 13.3839C3.10054 13.6183 3.41848 13.75 3.75 13.75H11.25C11.5815 13.75 11.8995 13.6183 12.1339 13.3839C12.3683 13.1495 12.5 12.8315 12.5 12.5V7.5"), stroke = Ink, strokeLineWidth = 1.25f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M1.25 4.375H13.75V6.25C13.7502 6.65288 13.6207 7.04512 13.3805 7.36862C13.1404 7.69212 12.8025 7.92967 12.4168 8.04608C12.0311 8.16249 11.6181 8.15157 11.2391 8.01494C10.8601 7.87831 10.5352 7.62324 10.3125 7.2875C10.2072 7.54915 10.0442 7.78367 9.8357 7.9736C9.62719 8.16352 9.37852 8.30396 9.1082 8.38446C8.83789 8.46495 8.5529 8.48343 8.27445 8.4385C7.99601 8.39358 7.73129 8.28642 7.5 8.125C7.26871 8.28642 7.00399 8.39358 6.72555 8.4385C6.4471 8.48343 6.16211 8.46495 5.8918 8.38446C5.62148 8.30396 5.37281 8.16352 5.1643 7.9736C4.95578 7.78367 4.79281 7.54915 4.6875 7.2875C4.4648 7.62324 4.13988 7.87831 3.76087 8.01494C3.38186 8.15157 2.96893 8.16249 2.58323 8.04608C2.19753 7.92967 1.85959 7.69212 1.61945 7.36862C1.37932 7.04512 1.24977 6.65288 1.25 6.25V4.375Z"), stroke = Ink, strokeLineWidth = 1.25f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/activity`. */
    val Activity: ImageVector by lazy {
        icon("Activity", 22f, 22f) {
            addPath(pathData = addPathNodes("M2.75 11H6.41667L9.16667 3.66667L12.8333 18.3333L15.5833 11H19.25"), stroke = Ink, strokeLineWidth = 1.65f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/globe`. */
    val Globe: ImageVector by lazy {
        icon("Globe", 18f, 18f) {
            addPath(pathData = addPathNodes("M16.5 9C16.5 13.1421 13.1421 16.5 9 16.5C4.85786 16.5 1.5 13.1421 1.5 9C1.5 4.85786 4.85786 1.5 9 1.5C13.1421 1.5 16.5 4.85786 16.5 9Z"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M9 1.5C7.07418 3.52212 6 6.20756 6 9C6 11.7924 7.07418 14.4779 9 16.5C10.9258 14.4779 12 11.7924 12 9C12 6.20756 10.9258 3.52212 9 1.5Z"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M1.5 9H16.5"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/more-vertical`. */
    val MoreVertical: ImageVector by lazy {
        icon("MoreVertical", 18f, 18f) {
            addPath(pathData = addPathNodes("M9 5.1C9.74558 5.1 10.35 4.49558 10.35 3.75C10.35 3.00442 9.74558 2.4 9 2.4C8.25442 2.4 7.65 3.00442 7.65 3.75C7.65 4.49558 8.25442 5.1 9 5.1Z"), fill = Ink)
            addPath(pathData = addPathNodes("M9 10.35C9.74558 10.35 10.35 9.74558 10.35 9C10.35 8.25442 9.74558 7.65 9 7.65C8.25442 7.65 7.65 8.25442 7.65 9C7.65 9.74558 8.25442 10.35 9 10.35Z"), fill = Ink)
            addPath(pathData = addPathNodes("M9 15.6C9.74558 15.6 10.35 14.9956 10.35 14.25C10.35 13.5044 9.74558 12.9 9 12.9C8.25442 12.9 7.65 13.5044 7.65 14.25C7.65 14.9956 8.25442 15.6 9 15.6Z"), fill = Ink)
        }
    }

    /** Web dashboard `icon/bell`. */
    val Bell: ImageVector by lazy {
        icon("Bell", 18f, 18f) {
            addPath(pathData = addPathNodes("M4.5 6C4.5 4.80653 4.97411 3.66193 5.81802 2.81802C6.66193 1.97411 7.80653 1.5 9 1.5C10.1935 1.5 11.3381 1.97411 12.182 2.81802C13.0259 3.66193 13.5 4.80653 13.5 6C13.5 11.25 15.75 12.75 15.75 12.75H2.25C2.25 12.75 4.5 11.25 4.5 6Z"), stroke = Ink, strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M7.725 15.75C7.85054 15.9783 8.03509 16.1688 8.25937 16.3014C8.48365 16.434 8.73943 16.504 9 16.504C9.26057 16.504 9.51635 16.434 9.74063 16.3014C9.96491 16.1688 10.1495 15.9783 10.275 15.75"), stroke = Ink, strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/chevron`. */
    val ChevronDown: ImageVector by lazy {
        icon("ChevronDown", 24f, 24f) {
            addPath(pathData = addPathNodes("M6 9L12 15L18 9"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/chev`. */
    val ChevronRight: ImageVector by lazy {
        icon("ChevronRight", 16f, 16f) {
            addPath(pathData = addPathNodes("M6 12L10 8L6 4"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/chev (mirrored)`. */
    val ChevronLeft: ImageVector by lazy {
        icon("ChevronLeft", 16f, 16f) {
        group(scaleX = -1f, pivotX = 8f) {
                addPath(pathData = addPathNodes("M6 12L10 8L6 4"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
        }
    }

    /** Web dashboard `icon/trash`. */
    val Trash: ImageVector by lazy {
        icon("Trash", 24f, 24f) {
            addPath(pathData = addPathNodes("M3 6H5M5 6H9M5 6L6 20C6 20.5304 6.21071 21.0391 6.58579 21.4142C6.96086 21.7893 7.46957 22 8 22H16C16.5304 22 17.0391 21.7893 17.4142 21.4142C17.7893 21.0391 18 20.5304 18 20L19 6M9 6H15M9 6V4C9 3.73478 9.10536 3.48043 9.29289 3.29289C9.48043 3.10536 9.73478 3 10 3H14C14.2652 3 14.5196 3.10536 14.7071 3.29289C14.8946 3.48043 15 3.73478 15 4V6M15 6H19M19 6H21M10 11V17M14 11V17"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/pencil`. */
    val Pencil: ImageVector by lazy {
        icon("Pencil", 24f, 24f) {
            addPath(pathData = addPathNodes("M12 20H21"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M16.5 3.5C16.8978 3.10218 17.4374 2.87868 18 2.87868C18.5626 2.87868 19.1022 3.10218 19.5 3.5C19.8978 3.89782 20.1213 4.43739 20.1213 5C20.1213 5.56261 19.8978 6.10218 19.5 6.5L7 19L3 20L4 16L16.5 3.5Z"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/plus`. */
    val Plus: ImageVector by lazy {
        icon("Plus", 24f, 24f) {
            addPath(pathData = addPathNodes("M12 5V12M12 12V19M12 12H5M12 12H19"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/x`. */
    val X: ImageVector by lazy {
        icon("X", 18f, 18f) {
            addPath(pathData = addPathNodes("M13.5 4.5L9 9M9 9L4.5 13.5M9 9L4.5 4.5M9 9L13.5 13.5"), stroke = Ink, strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/search`. */
    val Search: ImageVector by lazy {
        icon("Search", 18f, 18f) {
            addPath(pathData = addPathNodes("M13.5 8.25C13.5 11.1495 11.1495 13.5 8.25 13.5C5.35051 13.5 3 11.1495 3 8.25C3 5.35051 5.35051 3 8.25 3C11.1495 3 13.5 5.35051 13.5 8.25Z"), stroke = Ink, strokeLineWidth = 1.65f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter)
            addPath(pathData = addPathNodes("M15 15L12.375 12.375"), stroke = Ink, strokeLineWidth = 1.65f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Miter)
        }
    }

    /** Web dashboard `icon/check`. */
    val Check: ImageVector by lazy {
        icon("Check", 16f, 16f) {
            addPath(pathData = addPathNodes("M13.3333 4L6 11.3333L2.66667 8"), stroke = Ink, strokeLineWidth = 1.6667f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/circle-check`. */
    val CircleCheck: ImageVector by lazy {
        icon("CircleCheck", 30f, 30f) {
            addPath(pathData = addPathNodes("M27.5 15C27.5 21.9036 21.9036 27.5 15 27.5C8.09644 27.5 2.5 21.9036 2.5 15C2.5 8.09644 8.09644 2.5 15 2.5C21.9036 2.5 27.5 8.09644 27.5 15Z"), stroke = Ink, strokeLineWidth = 2.5f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter)
            addPath(pathData = addPathNodes("M11.25 15L13.75 17.5L18.75 12.5"), stroke = Ink, strokeLineWidth = 2.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/clock (badge fill drawn as outline)`. */
    val Clock: ImageVector by lazy {
        icon("Clock", 26f, 26f) {
            addPath(pathData = addPathNodes("M13 23.8333C18.9831 23.8333 23.8333 18.9831 23.8333 13C23.8333 7.01691 18.9831 2.16667 13 2.16667C7.01691 2.16667 2.16667 7.01691 2.16667 13C2.16667 18.9831 7.01691 23.8333 13 23.8333Z"), stroke = Ink, strokeLineWidth = 2.1667f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter)
            addPath(pathData = addPathNodes("M13 7.58333V13L16.25 15.1667"), stroke = Ink, strokeLineWidth = 2.1667f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/phone`. */
    val Phone: ImageVector by lazy {
        icon("Phone", 24f, 24f) {
            addPath(pathData = addPathNodes("M7 2H17C18.1046 2 19 2.89543 19 4V20C19 21.1046 18.1046 22 17 22H7C5.89543 22 5 21.1046 5 20V4C5 2.89543 5.89543 2 7 2Z"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M12 18H12.01"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/mail`. */
    val Mail: ImageVector by lazy {
        icon("Mail", 18f, 18f) {
            addPath(pathData = addPathNodes("M3 3H15C15.8284 3 16.5 3.67157 16.5 4.5V13.5C16.5 14.3284 15.8284 15 15 15H3C2.17157 15 1.5 14.3284 1.5 13.5V4.5C1.5 3.67157 2.17157 3 3 3Z"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M16.5 5.25L9.7725 9.525C9.54095 9.67007 9.27324 9.74701 9 9.74701C8.72676 9.74701 8.45905 9.67007 8.2275 9.525L1.5 5.25"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/pin`. */
    val Pin: ImageVector by lazy {
        icon("Pin", 18f, 18f) {
            addPath(pathData = addPathNodes("M14.625 7.5C14.625 11.625 9 16.5 9 16.5C9 16.5 3.375 11.625 3.375 7.5C3.375 6.00816 3.96763 4.57742 5.02252 3.52253C6.07742 2.46763 7.50816 1.875 9 1.875C10.4918 1.875 11.9226 2.46763 12.9775 3.52253C14.0324 4.57742 14.625 6.00816 14.625 7.5Z"), stroke = Ink, strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M11.1 7.5C11.1 8.6598 10.1598 9.6 9 9.6C7.8402 9.6 6.9 8.6598 6.9 7.5C6.9 6.3402 7.8402 5.4 9 5.4C10.1598 5.4 11.1 6.3402 11.1 7.5Z"), stroke = Ink, strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/lock`. */
    val Lock: ImageVector by lazy {
        icon("Lock", 18f, 18f) {
            addPath(pathData = addPathNodes("M5.625 7.5H12.375C13.4105 7.5 14.25 8.33947 14.25 9.375V13.125C14.25 14.1605 13.4105 15 12.375 15H5.625C4.58947 15 3.75 14.1605 3.75 13.125V9.375C3.75 8.33947 4.58947 7.5 5.625 7.5Z"), stroke = Ink, strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter)
            addPath(pathData = addPathNodes("M6 7.5V5.625C6 4.82935 6.31607 4.06629 6.87868 3.50368C7.44129 2.94107 8.20435 2.625 9 2.625C9.79565 2.625 10.5587 2.94107 11.1213 3.50368C11.6839 4.06629 12 4.82935 12 5.625V7.5"), stroke = Ink, strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Miter)
        }
    }

    /** Web dashboard `icon/eye`. */
    val Eye: ImageVector by lazy {
        icon("Eye", 16f, 16f) {
            addPath(pathData = addPathNodes("M1.66667 8C1.66667 8 4 3.33333 8 3.33333C12 3.33333 14.3333 8 14.3333 8C14.3333 8 12 12.6667 8 12.6667C4 12.6667 1.66667 8 1.66667 8Z"), stroke = Ink, strokeLineWidth = 1.1667f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M10 8C10 9.10457 9.10457 10 8 10C6.89543 10 6 9.10457 6 8C6 6.89543 6.89543 6 8 6C9.10457 6 10 6.89543 10 8Z"), stroke = Ink, strokeLineWidth = 1.1667f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/eye + slash`. */
    val EyeOff: ImageVector by lazy {
        icon("EyeOff", 16f, 16f) {
            addPath(pathData = addPathNodes("M1.66667 8C1.66667 8 4 3.33333 8 3.33333C12 3.33333 14.3333 8 14.3333 8C14.3333 8 12 12.6667 8 12.6667C4 12.6667 1.66667 8 1.66667 8Z"), stroke = Ink, strokeLineWidth = 1.1667f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M10 8C10 9.10457 9.10457 10 8 10C6.89543 10 6 9.10457 6 8C6 6.89543 6.89543 6 8 6C9.10457 6 10 6.89543 10 8Z"), stroke = Ink, strokeLineWidth = 1.1667f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            // Slash for the "hidden" state, same weight and caps as the eye.
            addPath(pathData = addPathNodes("M2 2L14 14"), stroke = Ink, strokeLineWidth = 1.1667f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/external-link`. */
    val ExternalLink: ImageVector by lazy {
        icon("ExternalLink", 16f, 16f) {
            addPath(pathData = addPathNodes("M14 6V2H10M14 2L6.66667 9.33333M12 8.66667V12.6667C12 13.0203 11.8595 13.3594 11.6095 13.6095C11.3594 13.8595 11.0203 14 10.6667 14H3.33333C2.97971 14 2.64057 13.8595 2.39052 13.6095C2.14048 13.3594 2 13.0203 2 12.6667V5.33333C2 4.97971 2.14048 4.64057 2.39052 4.39052C2.64057 4.14048 2.97971 4 3.33333 4H7.33333"), stroke = Ink, strokeLineWidth = 1.3333f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/user`. */
    val User: ImageVector by lazy {
        icon("User", 18f, 18f) {
            addPath(pathData = addPathNodes("M14.25 15.75V14.25C14.25 13.4544 13.9339 12.6913 13.3713 12.1287C12.8087 11.5661 12.0456 11.25 11.25 11.25H6.75C5.95435 11.25 5.19129 11.5661 4.62868 12.1287C4.06607 12.6913 3.75 13.4544 3.75 14.25V15.75"), stroke = Ink, strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M12 5.25C12 6.90685 10.6569 8.25 9 8.25C7.34315 8.25 6 6.90685 6 5.25C6 3.59315 7.34315 2.25 9 2.25C10.6569 2.25 12 3.59315 12 5.25Z"), stroke = Ink, strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/camera`. */
    val Camera: ImageVector by lazy {
        icon("Camera", 14f, 14f) {
            addPath(pathData = addPathNodes("M8.45833 2.33333H5.54167L4.08333 4.08333H2.33333C2.02391 4.08333 1.72717 4.20625 1.50838 4.42504C1.28958 4.64383 1.16667 4.94058 1.16667 5.25V10.5C1.16667 10.8094 1.28958 11.1062 1.50838 11.325C1.72717 11.5437 2.02391 11.6667 2.33333 11.6667H11.6667C11.9761 11.6667 12.2728 11.5437 12.4916 11.325C12.7104 11.1062 12.8333 10.8094 12.8333 10.5V5.25C12.8333 4.94058 12.7104 4.64383 12.4916 4.42504C12.2728 4.20625 11.9761 4.08333 11.6667 4.08333H9.91667L8.45833 2.33333Z"), stroke = Ink, strokeLineWidth = 1.1667f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M8.75 7.58333C8.75 8.54983 7.9665 9.33333 7 9.33333C6.0335 9.33333 5.25 8.54983 5.25 7.58333C5.25 6.61684 6.0335 5.83333 7 5.83333C7.9665 5.83333 8.75 6.61684 8.75 7.58333Z"), stroke = Ink, strokeLineWidth = 1.1667f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter)
        }
    }

    /** Web dashboard `Icon · grid`. */
    val Grid: ImageVector by lazy {
        icon("Grid", 18f, 18f) {
            addPath(pathData = addPathNodes("M3.5 2.8H6.5A0.7 0.7 0 0 1 7.2 3.5V6.5A0.7 0.7 0 0 1 6.5 7.2H3.5A0.7 0.7 0 0 1 2.8 6.5V3.5A0.7 0.7 0 0 1 3.5 2.8Z"), stroke = Ink, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter)
            addPath(pathData = addPathNodes("M11.5 2.8H14.500000000000002A0.7 0.7 0 0 1 15.200000000000001 3.5V6.5A0.7 0.7 0 0 1 14.500000000000002 7.2H11.5A0.7 0.7 0 0 1 10.8 6.5V3.5A0.7 0.7 0 0 1 11.5 2.8Z"), stroke = Ink, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter)
            addPath(pathData = addPathNodes("M3.5 10.8H6.5A0.7 0.7 0 0 1 7.2 11.5V14.500000000000002A0.7 0.7 0 0 1 6.5 15.200000000000001H3.5A0.7 0.7 0 0 1 2.8 14.500000000000002V11.5A0.7 0.7 0 0 1 3.5 10.8Z"), stroke = Ink, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter)
            addPath(pathData = addPathNodes("M11.5 10.8H14.500000000000002A0.7 0.7 0 0 1 15.200000000000001 11.5V14.500000000000002A0.7 0.7 0 0 1 14.500000000000002 15.200000000000001H11.5A0.7 0.7 0 0 1 10.8 14.500000000000002V11.5A0.7 0.7 0 0 1 11.5 10.8Z"), stroke = Ink, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter)
        }
    }

    /** Web dashboard `Icon · list`. */
    val List: ImageVector by lazy {
        icon("List", 18f, 18f) {
            addPath(pathData = addPathNodes("M2.8 4H15.2M2.8 9H15.2M2.8 14H15.2"), stroke = Ink, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Miter)
        }
    }

    /** Web dashboard `icon/refresh`. */
    val Refresh: ImageVector by lazy {
        icon("Refresh", 24f, 24f) {
            addPath(pathData = addPathNodes("M20 11C19.9985 9.14924 19.3554 7.35623 18.1802 5.92646C17.005 4.49669 15.3705 3.51862 13.555 3.15887C11.7395 2.79913 9.85551 3.07998 8.22389 3.95356C6.59227 4.82715 5.31402 6.23943 4.60691 7.94978C3.8998 9.66014 3.80759 11.5628 4.34597 13.3335C4.88436 15.1042 6.02004 16.6335 7.55952 17.6608C9.09901 18.6881 10.947 19.1498 12.7888 18.9673C14.6305 18.7848 16.352 17.9694 17.66 16.66"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M20 4V11H13"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/tag`. */
    val Tag: ImageVector by lazy {
        icon("Tag", 18f, 18f) {
            addPath(pathData = addPathNodes("M9.4395 1.9395C9.15826 1.65818 8.77679 1.50008 8.379 1.5H3C2.60218 1.5 2.22064 1.65804 1.93934 1.93934C1.65804 2.22064 1.5 2.60218 1.5 3V8.379C1.50008 8.77679 1.65818 9.15826 1.9395 9.4395L8.4675 15.9675C8.80839 16.3062 9.26943 16.4964 9.75 16.4964C10.2306 16.4964 10.6916 16.3062 11.0325 15.9675L15.9675 11.0325C16.3062 10.6916 16.4964 10.2306 16.4964 9.75C16.4964 9.26943 16.3062 8.80839 15.9675 8.4675L9.4395 1.9395Z"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M6 5.625C6 5.83211 5.83211 6 5.625 6C5.41789 6 5.25 5.83211 5.25 5.625C5.25 5.41789 5.41789 5.25 5.625 5.25C5.83211 5.25 6 5.41789 6 5.625Z"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/info`. */
    val Info: ImageVector by lazy {
        icon("Info", 24f, 24f) {
            addPath(pathData = addPathNodes("M22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2C17.5228 2 22 6.47715 22 12Z"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M12 16V12M12 8H12.01"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/alert`. */
    val Alert: ImageVector by lazy {
        icon("Alert", 24f, 24f) {
            addPath(pathData = addPathNodes("M10.3 3.9L1.8 18C1.62576 18.3017 1.53335 18.6437 1.53192 18.9921C1.53049 19.3404 1.6201 19.6832 1.79186 19.9863C1.96362 20.2894 2.21158 20.5424 2.51119 20.7202C2.8108 20.898 3.15165 20.9944 3.5 21H20.5C20.8483 20.9944 21.1892 20.898 21.4888 20.7202C21.7884 20.5424 22.0364 20.2894 22.2081 19.9863C22.3799 19.6832 22.4695 19.3404 22.4681 18.9921C22.4667 18.6437 22.3742 18.3017 22.2 18L13.7 3.9C13.5206 3.61058 13.2703 3.37174 12.9728 3.20612C12.6754 3.0405 12.3405 2.95357 12 2.95357C11.6595 2.95357 11.3246 3.0405 11.0272 3.20612C10.7297 3.37174 10.4794 3.61058 10.3 3.9Z"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
            addPath(pathData = addPathNodes("M12 9V13M12 17H12.01"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/filter`. */
    val Filter: ImageVector by lazy {
        icon("Filter", 14f, 14f) {
            addPath(pathData = addPathNodes("M1.75 2.91667H12.25L8.16667 7.875V11.0833L5.83333 11.9583V7.875L1.75 2.91667Z"), stroke = Ink, strokeLineWidth = 1.1667f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }

    /** Web dashboard `icon/log-out`. */
    val LogOut: ImageVector by lazy {
        icon("LogOut", 24f, 24f) {
            addPath(pathData = addPathNodes("M9 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H9M16 7L21 12L16 17M21 12H9"), stroke = Ink, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }
}
