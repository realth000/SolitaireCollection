package kzs.th000.solitaire_collection

import kotlinx.datetime.Clock
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.byUnicodePattern


class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }

    companion object {
        fun time(): String =
            Clock.System.now().format(DateTimeComponents.Format {
                byUnicodePattern("yyyy-MM-dd HH:mm:ss")
            })

    }
}