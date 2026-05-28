package com.example.beattreat.ui.MapaResenas

data class ResenaMapaUI(
    val firestoreDocId: String,
    val albumId:        String,
    val autorNombre:    String,
    val autorUsuario:   String,
    val autorFotoUrl:   String,
    val albumNombre:    String,
    val calificacion:   Float,
    val texto:          String,
    val fecha:          String,
    val latitud:        Double,
    val longitud:       Double
)

data class MapaResenasUIState(
    val resenas:      List<ResenaMapaUI> = emptyList(),
    val isLoading:    Boolean            = false,
    val errorMessage: String?            = null
)