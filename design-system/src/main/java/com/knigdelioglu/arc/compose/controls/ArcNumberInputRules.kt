package com.knigdelioglu.arc.compose.controls

/** Integer entry accepts a bounded value only on explicit commit. */
object ArcNumberInputRules {
    fun parse(text: String, minimum: Int, maximum: Int): Int? {
        if (minimum > maximum || text.isEmpty()) return null
        if (text == "-" || text.drop(1).any { !it.isDigit() } || (text.first() != '-' && !text.all(Char::isDigit))) return null
        return text.toIntOrNull()?.takeIf { it in minimum..maximum }
    }
}
