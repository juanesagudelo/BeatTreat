package com.example.beattreat.ui.Estadisticas

data class GeneroEstadisticaUI(
    val nombre:    String,
    val cantidad:  Int,
    val porcentaje: Float
)

data class EstadisticasUIState(
    val totalResenas:     Int                      = 0,
    val ratingPromedio:   Float                    = 0f,
    val ratingMaximo:     Float                    = 0f,
    val ratingMinimo:     Float                    = 0f,
    val generosFavoritos: List<GeneroEstadisticaUI> = emptyList(),
    val albumMasReciente: String                   = "",
    val rachaActual:      Int                      = 0,
    val isLoading:        Boolean                  = false,
    val errorMessage:     String?                  = null
)
