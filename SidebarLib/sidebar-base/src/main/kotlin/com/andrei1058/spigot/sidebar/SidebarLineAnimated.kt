package com.andrei1058.spigot.sidebar

open class SidebarLineAnimated(private val lines: Array<String>) : SidebarLine() {
    private var pos: Int = -1

    /**
     * @return message.
     */
    override fun getLine(): String {
        return lines[if (++pos == lines.size) (0.also { pos = it }) else pos]
    }

    open fun getLines(): Array<String> {
        return lines
    }
}
