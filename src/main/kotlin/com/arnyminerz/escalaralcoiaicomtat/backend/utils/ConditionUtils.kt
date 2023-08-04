package com.arnyminerz.escalaralcoiaicomtat.backend.utils

/**
 * Checks if all the given items are null.
 *
 * @param items The items to check for null.
 *
 * @return True if all items are null, false otherwise. If [items] is empty, always returns true.
 */
fun <T> areAllNull(vararg items: T?): Boolean = items.all { it == null }

/**
 * Checks if any of the given items is null.
 *
 * @param items The items to be checked.
 * 
 * @return true if any of the items is null, false otherwise. If [items] is empty, always returns false.
 */
fun <T> isAnyNull(vararg items: T?): Boolean = items.any { it == null }

/**
 * Checks if all the given boolean values are false.
 *
 * @param items The boolean values to be checked.
 *
 * @return `true` if all the given boolean values are false, `false` otherwise.
 */
fun areAllFalse(vararg items: Boolean): Boolean = items.all { !it }
