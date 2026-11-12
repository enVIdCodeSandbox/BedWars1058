package com.andrei1058.spigot.sidebar.v1_20_R4

import com.andrei1058.spigot.sidebar.*
import com.andrei1058.spigot.sidebar.PlayerTab

open class PlayerListImpl(sidebar: WrappedSidebar, identifier: String, prefix: SidebarLine, suffix: SidebarLine, pushingRule: PlayerTab.PushingRule, nameTagVisibility: PlayerTab.NameTagVisibility, placeholders: MutableCollection<PlaceholderProvider>?) : com.andrei1058.spigot.sidebar.v1_20_R3.PlayerListImpl(sidebar, identifier, prefix, suffix, pushingRule, nameTagVisibility, placeholders)
