package kzs.th000.solitaire_collection

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform