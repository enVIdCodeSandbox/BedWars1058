package com.andrei1058.spigot.sidebar

import java.util.Objects
import java.util.concurrent.Callable

open class PlaceholderProvider(private val placeholder: String, private val replacement: Callable<String>) {
    /**
     * @return placeholder.
     */
    open fun getPlaceholder(): String {
        return placeholder
    }

    /**
     * @return replacement.
     */
    open fun getReplacement(): String {
        try {
            val rep: String? = replacement.call()
            return rep ?: "null"
        } catch (e: Exception) {
            return "-"
        }
    }

    override fun equals(o: Any?): Boolean {
        if (o == null) {
            return false
        }
        if (o !is PlaceholderProvider) {
            return false
        }
        val that = o as PlaceholderProvider
        return that.placeholder.equals(placeholder, ignoreCase = true)
    }

    override fun hashCode(): Int {
        return Objects.hash(placeholder, replacement)
    }
}
