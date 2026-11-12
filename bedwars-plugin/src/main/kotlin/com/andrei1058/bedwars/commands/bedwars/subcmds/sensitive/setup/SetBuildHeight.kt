package com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.setup

import com.andrei1058.bedwars.BedWars.Companion.mainCmd
import com.andrei1058.bedwars.api.BedWars
import com.andrei1058.bedwars.api.command.ParentCommand
import com.andrei1058.bedwars.api.command.SubCommand
import com.andrei1058.bedwars.arena.SetupSession
import com.andrei1058.bedwars.configuration.Permissions
import java.util.Arrays
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class SetBuildHeight(parent: ParentCommand, name: String) : SubCommand(parent, name) {
    /**
     * Add your sub-command code under this method
     *
     * @param args
     * @param s
     */
    override fun execute(args: Array<String>, s: CommandSender): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        val ss: SetupSession? = SetupSession.getSession(p.getUniqueId())
        if (ss == null) {
            s.sendMessage("§c ▪ §7You're not in a setup session!")
            return true
        }
        if (args.size == 0) {
            p.sendMessage("§c▪ §7Usage: /" + mainCmd + " setMaxBuildHeight <int>")
        } else {
            try {
                Integer.parseInt(args[0])
            } catch (ex: Exception) {
                p.sendMessage("§c▪ §7Usage: /" + mainCmd + " setMaxBuildHeight <int>")
                return true
            }
            ss.getConfig().set("max-build-y", Integer.valueOf(args[0]))
            p.sendMessage("§6 ▪ §7Max build height Y set to §e" + args[0] + "§7!")
        }
        return true
    }

    /**
     * Manage sub-command tab complete
     */
    override fun getTabComplete(): MutableList<String>? {
        return Arrays.asList("180", "256")
    }

    override fun canSee(s: CommandSender, api: BedWars): Boolean {
        if (s is ConsoleCommandSender) {
            return false
        }
        val p = s as Player
        if (!SetupSession.isInSetupSession(p.getUniqueId())) {
            return false
        }
        return hasPermission(s)
    }

    init {
        setArenaSetupCommand(true)
        setPermission(Permissions.PERMISSION_SETUP_ARENA)
    }
}
