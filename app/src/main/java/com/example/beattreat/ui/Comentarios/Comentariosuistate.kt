package com.example.beattreat.ui.Comentarios

import com.example.beattreat.ui.Resena.ResenaDetalladaUI

data class ComentarioFirestoreUI(
    val id:           String,
    val autorNombre:  String,
    val autorUsuario: String,
    val autorFotoUrl: String,
    val texto:        String,
    val likes:        Int,
    val fecha:        String
)

data class ComentariosUIState(
    val resena:               ResenaDetalladaUI?       = null,
    val comentarios:          List<ComentarioFirestoreUI> = emptyList(),
    val nuevoComentario:      String                   = "",
    val comentariosLikeados:  Set<String>              = emptySet(),
    val isLoading:            Boolean                  = false,
    val enviando:             Boolean                  = false,
    val errorMessage:         String?                  = null
)