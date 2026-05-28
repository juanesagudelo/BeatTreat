package com.example.beattreat.ui.TopAlbums

data class TopAlbumUI(
    val firestoreId:  String,
    val titulo:       String,
    val artista:      String,
    val genero:       String,
    val coverImage:   String,
    val rating:       Float,
    val totalResenas: Int
)

data class TopAlbumsUIState(
    val generoSeleccionado: String           = "Todos",
    val albums:             List<TopAlbumUI> = emptyList(),
    val isLoading:          Boolean          = false,
    val errorMessage:       String?          = null
)
