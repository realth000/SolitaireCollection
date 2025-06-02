package kzs.th000.solitaire_collection

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SolitaireCollection",
    ) {
        App()
    }
}