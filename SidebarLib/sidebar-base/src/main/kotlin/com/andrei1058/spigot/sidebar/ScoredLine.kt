package com.andrei1058.spigot.sidebar

import org.jetbrains.annotations.ApiStatus

/**
 * EXPERIMENTAL.
 * Since 1.20.3 we can replace score numbers on the sidebar with string placeholders.
 */
@ApiStatus.Experimental
fun interface ScoredLine {
    @ApiStatus.Experimental
    fun getScore(): String?
}
