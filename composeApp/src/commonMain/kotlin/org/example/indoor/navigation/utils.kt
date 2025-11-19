package org.example.indoor.navigation

import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun nowMillis(): Long {
    val now = kotlin.time.Clock.System.now()
    return now.epochSeconds * 1000 + now.nanosecondsOfSecond / 1_000_000
}

fun hasPassedMillis(sinceMillis: Long, durationMillis: Long): Boolean {
    val now = nowMillis()
    return now - sinceMillis >= durationMillis
}