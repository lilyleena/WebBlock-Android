package com.kshrd.features.mock

/** Real sample photos (Unsplash) standing in for uploaded images until the backend exists. */
object MockImages {
    private fun photo(id: String) = "https://images.unsplash.com/photo-$id?auto=format&fit=crop&w=800&q=75"

    // Websites
    val CAFE_INTERIOR = photo("1554118811-1e0d58224f24")
    val CLOTHING_STORE = photo("1441986300917-64674bd600d8")
    val TEAM_MEETING = photo("1522202176988-66273c2fd55f")

    // Coffee House
    val LATTE = photo("1509042239860-f550ce710b93")
    val COLD_BREW = photo("1461023058943-07fcbe16d735")
    val ICED_LATTE = photo("1517701604599-bb29b565090c")
    val TEA_COOKIES = photo("1544787219-7f47ccb76574")
    val ICED_TEA = photo("1556679343-c7306c1976bc")
    val CROISSANTS = photo("1555507036-ab1f4038808a")
    val CUPCAKES = photo("1563729784474-d77dbb933a9e")
    val BLACK_COFFEE = photo("1514432324607-a09d9b4aefdd")

    // Fashion Store
    val LOUNGE_SET = photo("1515886657613-9f3515b0c78f")
    val GREEN_TEES = photo("1523381210434-271e8be1f52b")
    val BLACK_SNEAKER = photo("1491553895911-0055eca6402d")
    val RED_TRAINER = photo("1542291026-7eec264c27ff")
    val HANDBAG = photo("1584917865442-de89df76afd3")

    // Digital Agency
    val CODE_LAPTOP = photo("1498050108023-c5249f4df085")
    val DASHBOARD_LAPTOP = photo("1460925895917-afdab827c52f")
    val ANALYTICS = photo("1551288049-bebda4e38f71")
    val DESIGN_DESK = photo("1432888498266-38ffec3eaf0a")
    val COLOR_SWATCHES = photo("1561070791-2526d30994b5")
}
