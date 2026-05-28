package com.example.beattreat.model

import com.example.beattreat.ui.Chat.MensajeUI

object MensajesData {

    val mensajesPorGrupo = mapOf(
        "Bad Bunny" to listOf(
            MensajeUI(id = 1, texto = "Un Verano Sin Ti es una obra maestra 🔥", autor = "juan manuel", hora = "3:20 p.m", esPropio = false),
            MensajeUI(id = 2, texto = "Totalmente, cada canción es un hit", autor = "juanesa", hora = "3:22 p.m", esPropio = true),
            MensajeUI(id = 3, texto = "Tití Me Preguntó no para de sonar en mi cabeza", autor = "juan manuel", hora = "3:45 p.m", esPropio = false),
            MensajeUI(id = 4, texto = "Jajaja a todos nos pasó lo mismo", autor = "juanesa", hora = "3:50 p.m", esPropio = true)
        ),
        "Peso Pluma" to listOf(
            MensajeUI(id = 1, texto = "Génesis cambió el juego de los corridos", autor = "juan manuel", hora = "4:10 p.m", esPropio = false),
            MensajeUI(id = 2, texto = "Sí, Peso Pluma llegó a otro nivel", autor = "juanesa", hora = "4:12 p.m", esPropio = true),
            MensajeUI(id = 3, texto = "Bzrp Music Session también estuvo increíble", autor = "juan manuel", hora = "4:30 p.m", esPropio = false),
            MensajeUI(id = 4, texto = "De las mejores sesiones del año 🎵", autor = "juanesa", hora = "4:35 p.m", esPropio = true)
        ),
        "Maluma" to listOf(
            MensajeUI(id = 1, texto = "F.A.M.E. es uno de los mejores álbumes de reggaetón", autor = "juan manuel", hora = "5:00 p.m", esPropio = false),
            MensajeUI(id = 2, texto = "Hawái fue el himno de la pandemia", autor = "juanesa", hora = "5:05 p.m", esPropio = true),
            MensajeUI(id = 3, texto = "Papi Juancho también estuvo muy bueno", autor = "juan manuel", hora = "5:20 p.m", esPropio = false),
            MensajeUI(id = 4, texto = "Maluma nunca decepciona 🙌", autor = "juanesa", hora = "5:25 p.m", esPropio = true)
        ),
        "Fuerza Regida" to listOf(
            MensajeUI(id = 1, texto = "Pa Las Baby's no para de sonar 🎶", autor = "juan manuel", hora = "6:00 p.m", esPropio = false),
            MensajeUI(id = 2, texto = "Del Barrio Hasta Aquí también está brutal", autor = "juanesa", hora = "6:05 p.m", esPropio = true),
            MensajeUI(id = 3, texto = "Fuerza Regida representando México 🇲🇽", autor = "juan manuel", hora = "6:20 p.m", esPropio = false),
            MensajeUI(id = 4, texto = "Los corridos tumbados son otro nivel", autor = "juanesa", hora = "6:25 p.m", esPropio = true)
        ),
        "Yeison Jiménez" to listOf(
            MensajeUI(id = 1, texto = "Aventurero es un clásico de la música popular 🇨🇴", autor = "juan manuel", hora = "7:00 p.m", esPropio = false),
            MensajeUI(id = 2, texto = "Yeison Jiménez representa a Colombia entera", autor = "juanesa", hora = "7:05 p.m", esPropio = true),
            MensajeUI(id = 3, texto = "Resistiré también está muy bonita", autor = "juan manuel", hora = "7:20 p.m", esPropio = false),
            MensajeUI(id = 4, texto = "La música popular nunca falla ❤️", autor = "juanesa", hora = "7:25 p.m", esPropio = true)
        )
    )

    fun getMensajes(nombreGrupo: String): List<MensajeUI> {
        return mensajesPorGrupo[nombreGrupo] ?: mensajesPorGrupo.values.first()
    }
}