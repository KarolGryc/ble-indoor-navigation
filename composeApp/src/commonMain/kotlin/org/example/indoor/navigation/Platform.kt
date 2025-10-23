package org.example.indoor.navigation

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform