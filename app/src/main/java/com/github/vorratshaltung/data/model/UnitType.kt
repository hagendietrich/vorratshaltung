package com.github.vorratshaltung.data.model

enum class UnitType(val displayLabel: String) {
    STUECK("Stück"),
    PACKUNG("Packung"),
    GRAMM("g"),
    LITER("l"),
    MILLILITER("ml")
}

/**
 * Formatiert einen Betrag zusammen mit dem SI-Symbol der Einheit.
 * Beispiele: GRAMM.formatAmount(200f) -> "200g", LITER.formatAmount(1f) -> "1l"
 */
fun UnitType.formatAmount(amount: Float): String {
    val intAmount = amount.toInt()
    val number = if (amount == intAmount.toFloat()) intAmount.toString() else amount.toString()
    return "$number$displayLabel"
}
