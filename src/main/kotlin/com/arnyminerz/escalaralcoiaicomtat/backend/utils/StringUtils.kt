package com.arnyminerz.escalaralcoiaicomtat.backend.utils

import java.net.URLEncoder
import java.text.Normalizer

private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

/**
 * Removes accents from a CharSequence.
 *
 * @return A new String with all accents removed.
 */
fun CharSequence.removeAccents(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}

/**
 * Returns the URL-encoded representation of this string.
 *
 * @return the URL-encoded string
 */
val String.urlEncoded: String
    get() = URLEncoder.encode(this, Charsets.UTF_8)
