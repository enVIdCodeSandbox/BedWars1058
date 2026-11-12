package com.andrei1058.bedwars.arena.stats

import com.andrei1058.bedwars.BedWars
import com.andrei1058.bedwars.api.arena.GameState
import com.andrei1058.bedwars.api.arena.IArena
import com.andrei1058.bedwars.api.arena.stats.*
import com.andrei1058.bedwars.arena.stats.defaults.PlayerGameStatsContainer
import java.util.*
import java.util.stream.Collectors
import org.bukkit.entity.Player
import com.andrei1058.bedwars.api.util.unchecked

open class GameStatsManager(private val arena: IArena) : GameStatsHolder {
    private val registeredStats: HashMap<String, GameStatisticProvider<*>> = HashMap()
    private val playerSessionStats: HashMap<UUID, Optional<PlayerGameStats>> = HashMap()

    override fun register(statistic: GameStatisticProvider<*>) {
        if (statistic.getIdentifier().isBlank()) {
            throw RuntimeException("Identifier cannot be blank: " + statistic.javaClass.getName())
        }
        if (statistic.getIdentifier().trim() != statistic.getIdentifier()) {
            throw RuntimeException("Identifier should not start/end with white spaces: " + statistic.javaClass.getName())
        }
        if (null != registeredStats.getOrDefault(statistic.getIdentifier(), null)) {
            throw RuntimeException("Statistic already registered: " + statistic.getIdentifier())
        }
        registeredStats.put(statistic.getIdentifier(), statistic)
        BedWars.debug("Registered new game statistic: " + statistic.getIdentifier())
    }

    override fun getArena(): IArena {
        return arena
    }

    override fun init(player: Player): PlayerGameStats {
        if (playerSessionStats.containsKey(player.getUniqueId())) {
            throw RuntimeException(player.getName() + " is already registered for game stats!")
        }
        val stats: PlayerGameStats = PlayerGameStatsContainer(player)
        this.registeredStats.forEach({ id, provider -> stats.registerStatistic(id, provider.getDefault()) })
        playerSessionStats.put(player.getUniqueId(), Optional.of(stats))
        return stats
    }

    override fun unregisterPlayer(uuid: UUID) {
        if (getArena().getStatus() == GameState.restarting) {
            throw RuntimeException("You cannot unregister player stats during restarting phase!")
        }
        this.playerSessionStats.remove(uuid)
    }

    override fun getCreate(holder: Player): PlayerGameStats {
        val ps = playerSessionStats.getOrDefault(holder.getUniqueId(), Optional.empty())
        if (ps.isEmpty()) {
            val stats = init(holder)
            playerSessionStats.put(holder.getUniqueId(), Optional.of(stats))
            return stats
        }
        return ps.get()
    }

    override fun get(holder: UUID): Optional<PlayerGameStats> {
        return playerSessionStats.getOrDefault(holder, Optional.empty())
    }

    override fun getTrackedPlayers(): MutableCollection<Optional<PlayerGameStats>> {
        return Collections.unmodifiableCollection(playerSessionStats.values)
    }

    override fun getOrderedBy(statistic: String): MutableList<Optional<PlayerGameStats>> {
        //noinspection OptionalGetWithoutIsPresent
        val list = playerSessionStats.values.stream().filter { it.isPresent }.filter({ st -> st.get().getStatistic(statistic)!!.isPresent() }).sorted(Comparator.comparing { a -> a.get().getStatistic(statistic)!!.get() }).collect(Collectors.toList())
        Collections.reverse(list)
        return list
    }

    override fun hasStatistic(orderBy: String): Boolean {
        return registeredStats.containsKey(orderBy)
    }

    override fun getRegistered(): MutableList<String> {
        return registeredStats.keys.stream().collect(Collectors.toUnmodifiableList())
    }

    override fun getProvider(statistic: String): GameStatisticProvider<*>? {
        return registeredStats.getOrDefault(statistic, null)
    }

    init {
        for (statistic in DefaultStatistics.values()) {
            if (statistic.isIncrementable()) {
                register(object : GenericStatistic() {
                    override fun getIdentifier(): String {
                        return statistic.toString()
                    }
                })
            }
        }
    }
}
