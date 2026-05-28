package com.example.beattreat.ui.Descubre

object DescubreData {

    val categorias = listOf(
        CategoriaUI(1, "Nuevos\nlanzamientos", 0xFF6366F1, ""),
        CategoriaUI(2, "Géneros y\nEstadísticas", 0xFF8B5CF6, ""),
        CategoriaUI(3, "Rankings", 0xFF10B981, ""),
        CategoriaUI(4, "Podcasts", 0xFFEC4899, "")
    )

    val generos = listOf(
        GeneroUI(1, "Reggaetón",           0xFF6366F1),
        GeneroUI(2, "Popular Colombiana",  0xFFEC4899),
        GeneroUI(3, "Corridos Mexicanos",  0xFFDC2626),
        GeneroUI(4, "Vallenato",           0xFF06B6D4),
        GeneroUI(5, "Trap Latino",         0xFFF97316),
        GeneroUI(6, "Urbano",              0xFFEF4444),
        GeneroUI(7, "Cumbia",              0xFF9333EA),
        GeneroUI(8, "Latin Pop",           0xFF14B8A6)
    )

    val nuevosLanzamientos = listOf(
        AlbumDescubreUI(1, "Un Verano Sin Ti",      "Bad Bunny",       "https://cdn.phototourl.com/free/2026-04-16-f5b9a8aa-ad44-4c97-8521-3752902c1411.webp"),
        AlbumDescubreUI(2, "Génesis",               "Peso Pluma",      "https://cdn.phototourl.com/free/2026-05-05-b1ca2e29-5c59-4432-af9a-97160509e385.png"),
        AlbumDescubreUI(3, "F.A.M.E.",              "Maluma",          "https://cdn.phototourl.com/free/2026-05-05-5b02f800-905a-4f84-bc1a-e3551f321c9b.jpg"),
        AlbumDescubreUI(4, "Pa Las Baby's y belikeada",         "Fuerza Regida",   "https://cdn.phototourl.com/free/2026-05-05-cc5ef307-9564-4beb-8721-ca9425dc7d8e.jpg"),
        AlbumDescubreUI(5, "Todo de mi",            "Yeison Jiménez",  "https://cdn.phototourl.com/free/2026-05-05-7a4e65f8-a826-4620-9cc3-ab7d0a41dff3.webp")
    )
}