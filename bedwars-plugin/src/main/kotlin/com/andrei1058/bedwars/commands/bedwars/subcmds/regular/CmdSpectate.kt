package com.andrei1058.bedwars.commands.bedwars.subcmds.regular

import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.api.language.Language
import com.andrei1058.bedwars.arena.Arena
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.commands.bedwars.MainCommand
import com.andrei1058.bedwars.listeners.arenaselector.SpectateMenu
import java.util.ArrayList
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

/**
 * /bw spectate [arena] - pick a running game to watch.
 */
open class CmdSpectate(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        var arena: String? = null
        if (args.size >= 1) {
            arena = args[0]
            var known: Boolean = false
            for (a in Arena.getArenas()) {
                if (a.getArenaName().equals(arena, ignoreCase = true)) {
                    arena = a.getArenaName()
                    known = true
                    break
                }
            }
            if (!known) {
                p.sendMessage(Language.getMsg(p, SpectateMenu.MSG_ARENA_UNKNOWN).replace("{name}", args[0]))
                return true
            }
        }
        SpectateMenu.open(p, arena)
        return true
    }

    override fun getTabComplete(): MutableList<String>? {
        val tab: MutableList<String> = ArrayList()
        for (a in Arena.getArenas()) {
            if (!tab.contains(a.getArenaName())) {
                tab.add(a.getArenaName())
            }
        }
        return tab
    }

    override fun canSee(s: CommandSender, api: com.andrei1058.bedwars.api.BedWars): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (Arena.isInArena(p)) {
            return false
        }
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return hasPermission(s)
    }

    init {
        setPriority(19)
        showInList(false)
        setDisplayInfo(MainCommand.createTC("§6 ▪ §7/" + MainCommand.getInstance().getName() + " spectate §e[arena]", "/" + getParent().getName() + " " + getSubCommandName(), "§fOpen a menu with the games you can spectate.\n§fAdd an arena name to only list its games."))
    }
}
