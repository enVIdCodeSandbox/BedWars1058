/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.api.language

import com.andrei1058.bedwars.api.configuration.ConfigPath

object Messages {
    @JvmField var PREFIX: String = "prefix"
    /**
     * next event related
     */
    @JvmField var NEXT_EVENT_DIAMOND_UPGRADE_II: String = "next-event-diamondII"
    @JvmField var NEXT_EVENT_DIAMOND_UPGRADE_III: String = "next-event-diamondIII"
    @JvmField var NEXT_EVENT_EMERALD_UPGRADE_II: String = "next-event-emeraldII"
    @JvmField var NEXT_EVENT_EMERALD_UPGRADE_III: String = "next-event-emeraldIII"
    @JvmField var NEXT_EVENT_BEDS_DESTROY: String = "next-event-beds-destroy"
    @JvmField var NEXT_EVENT_DRAGON_SPAWN: String = "next-event-dragon-spawn"
    @JvmField var NEXT_EVENT_GAME_END: String = "next-event-game-end"
    @JvmField var NEXT_EVENT_TITLE_ANNOUNCE_BEDS_DESTROYED: String = "next-event-beds-destroy-title"
    @JvmField var NEXT_EVENT_SUBTITLE_ANNOUNCE_BEDS_DESTROYED: String = "next-event-beds-destroy-sub-title"
    @JvmField var NEXT_EVENT_CHAT_ANNOUNCE_BEDS_DESTROYED: String = "next-event-beds-destroy-chat"
    @JvmField var NEXT_EVENT_TITLE_ANNOUNCE_SUDDEN_DEATH: String = "next-event-sudden-death-title"
    @JvmField var NEXT_EVENT_SUBTITLE_ANNOUNCE_SUDDEN_DEATH: String = "next-event-sudden-death-sub-title"
    @JvmField var NEXT_EVENT_CHAT_ANNOUNCE_SUDDEN_DEATH: String = "next-event-sudden-death-chat"
    /**
     * General commands reply
     */
    @JvmField var COMMAND_MAIN: String = "cmd-main-list"
    @JvmField var COMMAND_LANG_LIST_HEADER: String = "cmd-lang-list-header"
    @JvmField var COMMAND_LANG_LIST_FORMAT: String = "cmd-lang-list-format"
    @JvmField var COMMAND_LANG_USAGE: String = "cmd-lang-usage"
    @JvmField var COMMAND_LANG_SELECTED_NOT_EXIST: String = "cmd-lang-not-exist"
    @JvmField var COMMAND_LANG_SELECTED_SUCCESSFULLY: String = "cmd-lang-set"
    @JvmField var COMMAND_LANG_USAGE_DENIED: String = "cmd-lang-not-set"
    @JvmField var COMMAND_JOIN_USAGE: String = "cmd-join-usage"
    @JvmField var COMMAND_JOIN_GROUP_OR_ARENA_NOT_FOUND: String = "cmd-join-not-found"
    @JvmField var COMMAND_JOIN_DENIED_IS_FULL: String = "cmd-join-arena-full"
    @JvmField var COMMAND_JOIN_NO_EMPTY_FOUND: String = "cmd-join-arenas-full"
    @JvmField var COMMAND_JOIN_DENIED_IS_FULL_OF_VIPS: String = "cmd-join-arena-full-vips"
    @JvmField var COMMAND_JOIN_DENIED_PARTY_TOO_BIG: String = "cmd-join-party-big"
    @JvmField var COMMAND_JOIN_DENIED_NOT_PARTY_LEADER: String = "cmd-join-not-leader"
    @JvmField var COMMAND_JOIN_PLAYER_JOIN_MSG: String = "cmd-join-success"
    @JvmField var COMMAND_JOIN_SPECTATOR_MSG: String = "cmd-join-spectate"
    @JvmField var COMMAND_JOIN_SPECTATOR_DENIED_MSG: String = "cmd-join-spectate-denied"
    @JvmField var COMMAND_TP_PLAYER_NOT_FOUND: String = "cmd-tp-player-not-found"
    @JvmField var COMMAND_TP_NOT_IN_ARENA: String = "cmd-tp-not-in-arena"
    @JvmField var COMMAND_TP_NOT_STARTED: String = "cmd-tp-not-started"
    @JvmField var COMMAND_TP_USAGE: String = "cmd-tp-usage"
    @JvmField var REJOIN_NO_ARENA: String = "cmd-rejoin-no-arena"
    @JvmField var REJOIN_DENIED: String = "cmd-rejoin-denied"
    @JvmField var REJOIN_ALLOWED: String = "cmd-rejoin-allowed"
    @JvmField var COMMAND_REJOIN_PLAYER_RECONNECTED: String = "cmd-rejoin-player-reconnected"
    @JvmField var COMMAND_LEAVE_MSG: String = "cmd-leave"
    @JvmField var COMMAND_NOT_ALLOWED_IN_GAME: String = "cmd-blocked-in-game"
    @JvmField var COMMAND_LEAVE_DENIED_NOT_IN_ARENA: String = "cmd-not-in-arena"
    @JvmField var COMMAND_PARTY_HELP: String = "cmd-party-help"
    @JvmField var COMMAND_PARTY_INVITE_USAGE: String = "cmd-party-invite-usage"
    @JvmField var COMMAND_PARTY_INVITE_SENT: String = "cmd-party-invite"
    @JvmField var COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE: String = "cmd-party-offline"
    @JvmField var COMMAND_PARTY_INVITE_SENT_TARGET_RECEIVE_MSG: String = "cmd-party-invite-received"
    @JvmField var COMMAND_PARTY_INVITE_DENIED_CANNOT_INVITE_YOURSELF: String = "cmd-party-invite-self"
    @JvmField var COMMAND_PARTY_ACCEPT_DENIED_NO_INVITE: String = "cmd-party-no-invite"
    @JvmField var COMMAND_PARTY_ACCEPT_DENIED_ALREADY_IN_PARTY: String = "cmd-party-already-in"
    @JvmField var COMMAND_PARTY_INSUFFICIENT_PERMISSIONS: String = "cmd-party-no-perm"
    @JvmField var COMMAND_PARTY_ACCEPT_USAGE: String = "cmd-party-accept-usage"
    @JvmField var COMMAND_PARTY_ACCEPT_SUCCESS: String = "cmd-party-join"
    @JvmField var COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY: String = "cmd-party-not-in"
    @JvmField var COMMAND_PARTY_LEAVE_DENIED_IS_OWNER_NEEDS_DISBAND: String = "cmd-party-cant-leave"
    @JvmField var COMMAND_PARTY_LEAVE_SUCCESS: String = "cmd-party-leave"
    @JvmField var COMMAND_PARTY_DISBAND_SUCCESS: String = "cmd-party-disband"
    @JvmField var COMMAND_PARTY_REMOVE_USAGE: String = "cmd-party-remove-usage"
    @JvmField var COMMAND_PARTY_REMOVE_SUCCESS: String = "cmd-party-remove"
    @JvmField var COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER: String = "cmd-party-remove-not-in"
    @JvmField var COMMAND_PARTY_PROMOTE_SUCCESS: String = "cmd-party-promote-owner"
    @JvmField var COMMAND_PARTY_PROMOTE_OWNER: String = "cmd-party-owner"
    @JvmField var COMMAND_PARTY_PROMOTE_NEW_OWNER: String = "cmd-party-owner-changed"
    @JvmField var COMMAND_PARTY_INFO_OWNER: String = "cmd-party-info-owner"
    @JvmField var COMMAND_PARTY_INFO_PLAYERS: String = "cmd-party-info-players"
    @JvmField var COMMAND_PARTY_INFO_PLAYER: String = "cmd-party-info-player"
    @JvmField var COMMAND_NOT_FOUND_OR_INSUFF_PERMS: String = "cmd-not-found"
    @JvmField var COMMAND_FORCESTART_NOT_IN_GAME: String = "cmd-start-no-game"
    @JvmField var COMMAND_FORCESTART_SUCCESS: String = "cmd-start"
    @JvmField var COMMAND_FORCESTART_NO_PERM: String = "cmd-start-no-perm"
    @JvmField var COMMAND_COOLDOWN: String = "cmd-cooldown"
    /**
     * Arena join/ leave related
     */
    @JvmField var ARENA_JOIN_VIP_KICK: String = "arena-kicked-by-vip"
    @JvmField var ARENA_START_COUNTDOWN_STOPPED_INSUFF_PLAYERS_CHAT: String = "arena-countdown-stopped"
    //public static String ARENA_PLAYER_QUIT = "player.quit";
    @JvmField var ARENA_RESTART_PLAYER_KICK: String = "arena-restart-kick"
    @JvmField var ARENA_JOIN_DENIED_SELECTOR: String = "arena-join-denied-selector"
    @JvmField var ARENA_JOIN_DENIED_NO_PROXY: String = "arena-join-denied-no-bwp"
    @JvmField var ARENA_SPECTATE_DENIED_SELECTOR: String = "arena-spectate-denied-selector"
    @JvmField var ARENA_LEAVE_PARTY_DISBANDED: String = "arena-leave-party-disbanded"
    /**
     * Arena status/ status change related
     */
    @JvmField var ARENA_STATUS_WAITING_NAME: String = "arena-status-waiting"
    @JvmField var ARENA_STATUS_STARTING_NAME: String = "arena-status-starting"
    @JvmField var ARENA_STATUS_PLAYING_NAME: String = "arena-status-playing"
    @JvmField var ARENA_STATUS_RESTARTING_NAME: String = "arena-status-restarting"
    @JvmField var ARENA_STATUS_START_PLAYER_TITLE: String = "arena-start-title"
    @JvmField var ARENA_STATUS_START_PLAYER_TUTORIAL: String = "arena-start-tutorial"
    @JvmField var ARENA_STATUS_START_COUNTDOWN_CHAT: String = "arena-start-countdown"
    @JvmField var ARENA_STATUS_START_COUNTDOWN_TITLE: String = "arena-start-countdown-title"
    @JvmField var ARENA_STATUS_START_COUNTDOWN_SUB_TITLE: String = "arena-start-countdown-subtitle"
    @JvmField var ARENA_STATUS_START_COUNTDOWN_CANCELLED_TITLE: String = "arena-countdown-stopped-subtitle"
    @JvmField var ARENA_STATUS_START_COUNTDOWN_CANCELLED_SUB_TITLE: String = "arena-countdown-stopped-subtitle"
    @JvmField var ARENA_DISPLAY_GROUP_PATH: String = "display-group-"
    /**
     * Arena GUI related
     */
    @JvmField var ARENA_GUI_INV_NAME: String = "arena-selector-gui-name"
    @JvmField var ARENA_GUI_ARENA_CONTENT_NAME: String = "arena-selector-content-name"
    @JvmField var ARENA_GUI_ARENA_CONTENT_LORE: String = "arena-selector-content-lore"
    @JvmField var ARENA_GUI_SKIPPED_ITEM_NAME: String = "arena-selector-skipped-item-name"
    @JvmField var ARENA_GUI_SKIPPED_ITEM_LORE: String = "arena-selector-skipped-item-lore"
    /**
     * Spectator related
     */
    @JvmField var ARENA_SPECTATOR_TELEPORTER_GUI_NAME: String = "spectator-tp-gui-name"
    //{player} - returns display name, {prefix} - returns the player rank
    @JvmField var ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_NAME: String = "spectator-tp-gui-head-name"
    //{health}, {food}
    @JvmField var ARENA_SPECTATOR_TELEPORTER_GUI_HEAD_LORE: String = "spectator-tp-gui-head-lore"
    @JvmField var ARENA_SPECTATOR_LEAVE_ITEM_NAME: String = "spectator-tp-name"
    @JvmField var ARENA_SPECTATOR_LEAVE_ITEM_LORE: String = "spectator-tp-lore"
    @JvmField var ARENA_SPECTATOR_FIRST_PERSON_ENTER_TITLE: String = "spectator-first-person-enter-title"
    @JvmField var ARENA_SPECTATOR_FIRST_PERSON_ENTER_SUBTITLE: String = "spectator-first-person-enter-subtitle"
    @JvmField var ARENA_SPECTATOR_FIRST_PERSON_LEAVE_TITLE: String = "spectator-first-person-quit-title"
    @JvmField var ARENA_SPECTATOR_FIRST_PERSON_LEAVE_SUBTITLE: String = "spectator-first-person-quit-subtitle"
    /**
     * Stats related
     */
    @JvmField var PLAYER_STATS_GUI_PATH: String = "stats"
    @JvmField var PLAYER_STATS_GUI_INV_NAME: String = PLAYER_STATS_GUI_PATH + "-inv-name"
    /**
     * Arena generators related
     */
    @JvmField var GENERATOR_HOLOGRAM_TIER: String = "generator-tier"
    @JvmField var GENERATOR_HOLOGRAM_TYPE_DIAMOND: String = "generator-diamond"
    @JvmField var GENERATOR_HOLOGRAM_TYPE_EMERALD: String = "generator-emerald"
    @JvmField var GENERATOR_HOLOGRAM_TIMER: String = "generator-timer"
    @JvmField var GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT: String = "generator-upgrade-announce"
    /**
     * General formatting
     */
    @JvmField var FORMATTING_CHAT_LOBBY: String = "format-chat-lobby"
    @JvmField var FORMATTING_CHAT_WAITING: String = "format-chat-waiting"
    @JvmField var FORMATTING_CHAT_SHOUT: String = "format-chat-global"
    @JvmField var FORMATTING_CHAT_TEAM: String = "format-chat-team"
    @JvmField var FORMATTING_CHAT_SPECTATOR: String = "format-chat-spectator"
    @JvmField var FORMATTING_SCOREBOARD_DATE: String = "format-sb-date"
    @JvmField var FORMATTING_SCOREBOARD_TEAM_GENERIC: String = "format-sb-team-generic"
    @JvmField var FORMATTING_SCOREBOARD_HEALTH: String = "format-sb-health"
    // TAB
    @JvmField var FORMATTING_SB_TAB_BASE: String = "format-tab"
    // TAB LOBBY
    @JvmField var FORMATTING_SB_TAB_LOBBY_HEADER: String = FORMATTING_SB_TAB_BASE + ".lobby.header"
    @JvmField var FORMATTING_SB_TAB_LOBBY_FOOTER: String = FORMATTING_SB_TAB_BASE + ".lobby.footer"
    @JvmField var FORMATTING_SB_TAB_LOBBY_PREFIX: String = FORMATTING_SB_TAB_BASE + ".lobby.prefix"
    @JvmField var FORMATTING_SB_TAB_LOBBY_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".lobby.suffix"
    // TAB WAITING
    @JvmField var FORMATTING_SB_TAB_WAITING_HEADER: String = FORMATTING_SB_TAB_BASE + ".waiting.player.header"
    @JvmField var FORMATTING_SB_TAB_WAITING_FOOTER: String = FORMATTING_SB_TAB_BASE + ".waiting.player.footer"
    @JvmField var FORMATTING_SB_TAB_WAITING_PREFIX: String = FORMATTING_SB_TAB_BASE + ".waiting.player.prefix"
    @JvmField var FORMATTING_SB_TAB_WAITING_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".waiting.player.suffix"
    // TAB WAITING FOR SPECTATORS
    @JvmField var FORMATTING_SB_TAB_WAITING_HEADER_SPEC: String = FORMATTING_SB_TAB_BASE + ".waiting.spectator.header"
    @JvmField var FORMATTING_SB_TAB_WAITING_FOOTER_SPEC: String = FORMATTING_SB_TAB_BASE + ".waiting.spectator.footer"
    @JvmField var FORMATTING_SB_TAB_WAITING_PREFIX_SPEC: String = FORMATTING_SB_TAB_BASE + ".waiting.spectator.prefix"
    @JvmField var FORMATTING_SB_TAB_WAITING_SUFFIX_SPEC: String = FORMATTING_SB_TAB_BASE + ".waiting.spectator.suffix"
    // TAB STARTING
    @JvmField var FORMATTING_SB_TAB_STARTING_HEADER: String = FORMATTING_SB_TAB_BASE + ".starting.player.header"
    @JvmField var FORMATTING_SB_TAB_STARTING_FOOTER: String = FORMATTING_SB_TAB_BASE + ".starting.player.footer"
    @JvmField var FORMATTING_SB_TAB_STARTING_PREFIX: String = FORMATTING_SB_TAB_BASE + ".starting.player.prefix"
    @JvmField var FORMATTING_SB_TAB_STARTING_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".starting.player.suffix"
    // TAB STARTING FOR SPECTATORS
    @JvmField var FORMATTING_SB_TAB_STARTING_HEADER_SPEC: String = FORMATTING_SB_TAB_BASE + ".starting.spectator.header"
    @JvmField var FORMATTING_SB_TAB_STARTING_FOOTER_SPEC: String = FORMATTING_SB_TAB_BASE + ".starting.spectator.footer"
    @JvmField var FORMATTING_SB_TAB_STARTING_PREFIX_SPEC: String = FORMATTING_SB_TAB_BASE + ".starting.player.prefix"
    @JvmField var FORMATTING_SB_TAB_STARTING_SUFFIX_SPEC: String = FORMATTING_SB_TAB_BASE + ".starting.player.suffix"
    // TAB PLAYING
    @JvmField var FORMATTING_SB_TAB_PLAYING_HEADER: String = FORMATTING_SB_TAB_BASE + ".playing.alive.header"
    @JvmField var FORMATTING_SB_TAB_PLAYING_FOOTER: String = FORMATTING_SB_TAB_BASE + ".playing.alive.footer"
    @JvmField var FORMATTING_SB_TAB_PLAYING_PREFIX: String = FORMATTING_SB_TAB_BASE + ".playing.alive.prefix"
    @JvmField var FORMATTING_SB_TAB_PLAYING_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".playing.alive.suffix"
    // TAB PLAYING-ELIMINATED
    @JvmField var FORMATTING_SB_TAB_PLAYING_ELM_HEADER: String = FORMATTING_SB_TAB_BASE + ".playing.eliminated.header"
    @JvmField var FORMATTING_SB_TAB_PLAYING_ELM_FOOTER: String = FORMATTING_SB_TAB_BASE + ".playing.eliminated.footer"
    @JvmField var FORMATTING_SB_TAB_PLAYING_ELM_PREFIX: String = FORMATTING_SB_TAB_BASE + ".playing.eliminated.prefix"
    @JvmField var FORMATTING_SB_TAB_PLAYING_ELM_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".playing.eliminated.suffix"
    // TAB PLAYING FOR SPECTATORS
    @JvmField var FORMATTING_SB_TAB_PLAYING_SPEC_HEADER: String = FORMATTING_SB_TAB_BASE + ".playing.spectator.header"
    @JvmField var FORMATTING_SB_TAB_PLAYING_SPEC_FOOTER: String = FORMATTING_SB_TAB_BASE + ".playing.spectator.footer"
    @JvmField var FORMATTING_SB_TAB_PLAYING_SPEC_PREFIX: String = FORMATTING_SB_TAB_BASE + ".playing.spectator.prefix"
    @JvmField var FORMATTING_SB_TAB_PLAYING_SPEC_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".playing.spectator.suffix"
    // TAB RESTARTING FOR WINNERS ALIVE
    @JvmField var FORMATTING_SB_TAB_RESTARTING_WIN1_HEADER: String = FORMATTING_SB_TAB_BASE + ".restarting.winner-alive.header"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_WIN1_FOOTER: String = FORMATTING_SB_TAB_BASE + ".restarting.winner-alive.footer"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_WIN1_PREFIX: String = FORMATTING_SB_TAB_BASE + ".restarting.winner-alive.prefix"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_WIN1_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".restarting.winner-alive.suffix"
    // TAB RESTARTING FOR WINNERS DEAD
    @JvmField var FORMATTING_SB_TAB_RESTARTING_WIN2_HEADER: String = FORMATTING_SB_TAB_BASE + ".restarting.winner-dead.header"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_WIN2_FOOTER: String = FORMATTING_SB_TAB_BASE + ".restarting.winner-dead.footer"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_WIN2_PREFIX: String = FORMATTING_SB_TAB_BASE + ".restarting.winner-dead.prefix"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_WIN2_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".restarting.winner-dead.suffix"
    // TAB RESTARTING FOR LOSERS
    @JvmField var FORMATTING_SB_TAB_RESTARTING_ELM_HEADER: String = FORMATTING_SB_TAB_BASE + ".restarting.loser.header"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_ELM_FOOTER: String = FORMATTING_SB_TAB_BASE + ".restarting.loser.footer"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_ELM_PREFIX: String = FORMATTING_SB_TAB_BASE + ".restarting.loser.prefix"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_ELM_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".restarting.loser.suffix"
    // TAB RESTARTING FOR SPECTATORS
    @JvmField var FORMATTING_SB_TAB_RESTARTING_SPEC_HEADER: String = FORMATTING_SB_TAB_BASE + ".restarting.spectator.header"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_SPEC_FOOTER: String = FORMATTING_SB_TAB_BASE + ".restarting.spectator.footer"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_SPEC_PREFIX: String = FORMATTING_SB_TAB_BASE + ".restarting.spectator.prefix"
    @JvmField var FORMATTING_SB_TAB_RESTARTING_SPEC_SUFFIX: String = FORMATTING_SB_TAB_BASE + ".restarting.spectator.suffix"
    @JvmField var FORMATTING_SCOREBOARD_TEAM_ELIMINATED: String = "format-sb-team-eliminated"
    @JvmField var FORMATTING_SCOREBOARD_BED_DESTROYED: String = "format-sb-bed-destroyed"
    @JvmField var FORMATTING_SCOREBOARD_TEAM_ALIVE: String = "format-sb-team-alive"
    @JvmField var FORMATTING_SCOREBOARD_NEXEVENT_TIMER: String = "format-sb-generator-timer"
    @JvmField var FORMATTING_SCOREBOARD_YOUR_TEAM: String = "format-sb-you"
    @JvmField var FORMATTING_ACTION_BAR_TRACKING: String = "format-action-tracking"
    @JvmField var FORMATTING_TEAM_WINNER_FORMAT: String = "format-winner-team"
    @JvmField var FORMATTING_SOLO_WINNER_FORMAT: String = "format-winner-solo"
    //public static String FORMATTING_TAB_LIST = "format.tablist";
    @JvmField var FORMATTING_GENERATOR_TIER1: String = "format-tier1"
    @JvmField var FORMATTING_GENERATOR_TIER2: String = "format-tier2"
    @JvmField var FORMATTING_GENERATOR_TIER3: String = "format-tier3"
    @JvmField var FORMATTING_DESPAWNABLE_UTILITY_NPC_HEALTH: String = "format-despawnable-health"
    @JvmField var FORMATTING_STATS_DATE_FORMAT: String = "format-stats-time"
    @JvmField var FORMAT_PAPI_PLAYER_TEAM_TEAM: String = "format-papi-player-team"
    @JvmField var FORMAT_PAPI_PLAYER_TEAM_SHOUT: String = "format-papi-player-shout"
    @JvmField var FORMAT_PAPI_PLAYER_TEAM_SPECTATOR: String = "format-papi-player-spectator"
    @JvmField var FORMAT_UPGRADE_TIER_LOCKED: String = "format-tier-color-locked"
    @JvmField var FORMAT_UPGRADE_TIER_UNLOCKED: String = "format-tier-color-unlocked"
    @JvmField var FORMAT_UPGRADE_COLOR_CANT_AFFORD: String = "format-upgrade-color-cant-afford"
    @JvmField var FORMAT_UPGRADE_COLOR_CAN_AFFORD: String = "format-upgrade-color-can-afford"
    @JvmField var FORMAT_UPGRADE_COLOR_UNLOCKED: String = "format-upgrade-color-unlocked"
    @JvmField var FORMAT_UPGRADE_TRAP_COST: String = "format-upgrade-trap-cost"
    @JvmField var FORMAT_SPECTATOR_TARGET: String = "format-spectator-target"
    /**
     * Meaning/ Translations
     */
    @JvmField var MEANING_FULL: String = "meaning-full"
    @JvmField var MEANING_SHOUT: String = "meaning-shout"
    @JvmField var MEANING_NEVER: String = "meaning-never"
    @JvmField var MEANING_NOBODY: String = "meaning-nobody"
    @JvmField var MEANING_IRON_SINGULAR: String = "meaning-iron-singular"
    @JvmField var MEANING_IRON_PLURAL: String = "meaning-iron-plural"
    @JvmField var MEANING_GOLD_SINGULAR: String = "meaning-gold-singular"
    @JvmField var MEANING_GOLD_PLURAL: String = "meaning-gold-plural"
    @JvmField var MEANING_EMERALD_SINGULAR: String = "meaning-emerald-singular"
    @JvmField var MEANING_EMERALD_PLURAL: String = "meaning-emerald-plural"
    @JvmField var MEANING_DIAMOND_SINGULAR: String = "meaning-diamond-singular"
    @JvmField var MEANING_DIAMOND_PLURAL: String = "meaning-diamond-plural"
    @JvmField var MEANING_VAULT_SINGULAR: String = "meaning-vault-singular"
    @JvmField var MEANING_VAULT_PLURAL: String = "meaning-vault-plural"
    @JvmField var MEANING_NO_TRAP: String = "meaning-no-trap"
    /**
     * Scoreboard related
     */
    @JvmField var SCOREBOARD_LOBBY: String = "sidebar.lobby"
    @JvmField var SCOREBOARD_DEFAULT_WAITING: String = "sidebar.Default.waiting.player"
    @JvmField var SCOREBOARD_DEFAULT_WAITING_SPEC: String = "sidebar.Default.waiting.spectator"
    @JvmField var SCOREBOARD_DEFAULT_STARTING: String = "sidebar.Default.starting.player"
    @JvmField var SCOREBOARD_DEFAULT_STARTING_SPEC: String = "sidebar.Default.starting.spectator"
    @JvmField var SCOREBOARD_DEFAULT_PLAYING: String = "sidebar.Default.playing.alive"
    @JvmField var SCOREBOARD_DEFAULT_PLAYING_SPEC: String = "sidebar.Default.playing.spectator"
    @JvmField var SCOREBOARD_DEFAULT_PLAYING_SPEC_ELIMINATED: String = "sidebar.Default.playing.eliminated"
    @JvmField var SCOREBOARD_DEFAULT_RESTARTING_SPEC: String = "sidebar.Default.restarting.spectator"
    @JvmField var SCOREBOARD_DEFAULT_RESTARTING_WIN1: String = "sidebar.Default.restarting.winner-alive"
    @JvmField var SCOREBOARD_DEFAULT_RESTARTING_WIN2: String = "sidebar.Default.restarting.winner-eliminated"
    @JvmField var SCOREBOARD_DEFAULT_RESTARTING_LOSER: String = "sidebar.Default.restarting.loser"
    /**
     * Player interact related
     */
    @JvmField var INTERACT_CANNOT_PLACE_BLOCK: String = "interact-cant-place"
    @JvmField var INTERACT_CANNOT_BREAK_BLOCK: String = "interact-cant-break"
    @JvmField var INTERACT_CANNOT_BREAK_OWN_BED: String = "interact-cant-destroy-bed"
    @JvmField var INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT: String = "interact-bed-destroy-chat"
    @JvmField var INTERACT_BED_DESTROY_TITLE_ANNOUNCEMENT: String = "interact-bed-destroy-title"
    @JvmField var INTERACT_BED_DESTROY_SUBTITLE_ANNOUNCEMENT: String = "interact-bed-destroy-subtitle"
    @JvmField var INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT_TO_VICTIM: String = "interact-bed-destroy-team"
    @JvmField var INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED: String = "interact-cant-open-chest"
    @JvmField var INTERACT_INVISIBILITY_REMOVED_DAMGE_TAKEN: String = "interact-invisibility-removed-damaged"
    /**
     * PvP related
     */
    @JvmField var PLAYER_DIE_RESPAWN_TITLE: String = "player-respawn-title"
    @JvmField var PLAYER_DIE_RESPAWN_SUBTITLE: String = "player-respawn-subtitle"
    @JvmField var PLAYER_DIE_RESPAWN_CHAT: String = "player-respawn-timer-chat"
    @JvmField var PLAYER_DIE_RESPAWNED_TITLE: String = "player-respawned-title"
    @JvmField var PLAYER_DIE_ELIMINATED_CHAT: String = "player-eliminated-chat"
    @JvmField var PLAYER_DIE_VOID_FALL_REGULAR_KILL: String = "player-die-void-regular"
    @JvmField var PLAYER_DIE_VOID_FALL_FINAL_KILL: String = "player-die-void-final"
    @JvmField var PLAYER_DIE_KNOCKED_IN_VOID_REGULAR_KILL: String = "player-die-knocked-void-regular"
    @JvmField var PLAYER_DIE_KNOCKED_IN_VOID_FINAL_KILL: String = "player-die-knocked-void-final"
    @JvmField var PLAYER_DIE_KNOCKED_BY_REGULAR_KILL: String = "player-die-knocked-fall-regular"
    @JvmField var PLAYER_DIE_KNOCKED_BY_FINAL_KILL: String = "player-die-knocked-fall-final"
    @JvmField var PLAYER_DIE_EXPLOSION_WITH_SOURCE_REGULAR_KILL: String = "player-die-bomb-regular"
    @JvmField var PLAYER_DIE_EXPLOSION_WITH_SOURCE_FINAL_KILL: String = "player-die-bomb-final"
    @JvmField var PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_REGULAR: String = "player-die-bomb2-regular"
    @JvmField var PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_FINAL_KILL: String = "player-die-bomb2-final"
    @JvmField var PLAYER_DIE_PVP_REGULAR_KILL: String = "player-die-attack-regular"
    @JvmField var PLAYER_DIE_PVP_FINAL_KILL: String = "player-die-attack-final"
    @JvmField var PLAYER_DIE_UNKNOWN_REASON_REGULAR: String = "player-die-unknown-regular"
    @JvmField var PLAYER_DIE_UNKNOWN_REASON_FINAL_KILL: String = "player-die-unknown-final"
    @JvmField var PLAYER_DIE_SHOOT_REGULAR: String = "player-die-shoot-regular"
    @JvmField var PLAYER_DIE_SHOOT_FINAL_KILL: String = "player-die-shoot-final"
    @JvmField var PLAYER_DIE_DEBUG_REGULAR: String = "player-die-bedbug-regular"
    @JvmField var PLAYER_DIE_DEBUG_FINAL_KILL: String = "player-die-bedbug-final"
    @JvmField var PLAYER_DIE_IRON_GOLEM_REGULAR: String = "player-die-golem-regular"
    @JvmField var PLAYER_DIE_IRON_GOLEM_FINAL_KILL: String = "player-die-golem-final"
    @JvmField var PLAYER_DIE_PVP_LOG_OUT_REGULAR: String = "player-die-pvp-log-out-regular"
    @JvmField var PLAYER_DIE_PVP_LOG_OUT_FINAL: String = "player-die-pvp-log-out-final"
    @JvmField var PLAYER_DIE_REWARD_DIAMOND: String = "player-loot-diamond"
    @JvmField var PLAYER_DIE_REWARD_IRON: String = "player-loot-iron"
    @JvmField var PLAYER_DIE_REWARD_GOLD: String = "player-loot-gold"
    @JvmField var PLAYER_DIE_REWARD_EMERALD: String = "player-loot-emerald"
    @JvmField var PLAYER_HIT_BOW: String = "player-hit-bow"
    /**
     * Misc
     */
    @JvmField var GAME_END_GAME_OVER_PLAYER_TITLE: String = "game-end-game-over-title"
    @JvmField var GAME_END_VICTORY_PLAYER_TITLE: String = "game-end-victory-title"
    @JvmField var GAME_END_TOP_PLAYER_CHAT: String = "game-end-top-chat"
    @JvmField var GAME_END_TEAM_WON_CHAT: String = "game-end-winner-team"
    @JvmField var XP_REWARD_WIN: String = "xp-reward-game-win"
    @JvmField var XP_REWARD_PER_TEAMMATE: String = "xp-reward-per-teammate"
    @JvmField var XP_REWARD_PER_MINUTE: String = "xp-reward-per-minute"
    @JvmField var XP_REWARD_BED_DESTROY: String = "xp-reward-bed-destroyed"
    @JvmField var XP_REWARD_REGULAR_KILL: String = "xp-reward-regular-kill"
    @JvmField var XP_REWARD_FINAL_KILL: String = "xp-reward-final-kill"
    @JvmField var MONEY_REWARD_WIN: String = "money-reward-game-win"
    @JvmField var MONEY_REWARD_PER_MINUTE: String = "money-reward-per-minute"
    @JvmField var MONEY_REWARD_PER_TEAMMATE: String = "money-reward-per-teammate"
    @JvmField var MONEY_REWARD_BED_DESTROYED: String = "money-reward-bed-destroyed"
    @JvmField var MONEY_REWARD_FINAL_KILL: String = "money-reward-final-kill"
    @JvmField var MONEY_REWARD_REGULAR_KILL: String = "money-reward-regular-kill"
    @JvmField var BED_HOLOGRAM_DEFEND: String = "bed-hologram-defend"
    @JvmField var BED_HOLOGRAM_DESTROYED: String = "bed-hologram-destroyed"
    @JvmField var TEAM_ELIMINATED_CHAT: String = "team-eliminated"
    /**
     * Upgrades/ Shop
     */
    @JvmField var NPC_NAME_TEAM_UPGRADES: String = "npc-team-upgrades"
    @JvmField var NPC_NAME_TEAM_SHOP: String = "npc-team-shop"
    @JvmField var NPC_NAME_SOLO_UPGRADES: String = "npc-solo-upgrades"
    @JvmField var NPC_NAME_SOLO_SHOP: String = "npc-solo-shop"
    @JvmField var UPGRADES_MENU_GUI_NAME_PATH: String = "upgrades-menu-gui-name-"
    @JvmField var UPGRADES_CATEGORY_GUI_NAME_PATH: String = "upgrades-category-gui-name-"
    @JvmField var UPGRADES_CATEGORY_ITEM_NAME_PATH: String = "upgrades-category-item-name-"
    @JvmField var UPGRADES_CATEGORY_ITEM_LORE_PATH: String = "upgrades-category-item-lore-"
    @JvmField var UPGRADES_SEPARATOR_ITEM_NAME_PATH: String = "upgrades-separator-item-name-"
    @JvmField var UPGRADES_SEPARATOR_ITEM_LORE_PATH: String = "upgrades-separator-item-lore-"
    @JvmField var UPGRADES_TRAP_SLOT_ITEM_NAME_PATH: String = "upgrades-trap-slot-item-name-"
    @JvmField var UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH: String = "upgrades-trap-slot-item-lore1-"
    @JvmField var UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH: String = "upgrades-trap-slot-item-lore2-"
    @JvmField var UPGRADES_UPGRADE_TIER_ITEM_NAME: String = "upgrades-upgrade-name-{name}-{tier}"
    @JvmField var UPGRADES_UPGRADE_TIER_ITEM_LORE: String = "upgrades-upgrade-lore-{name}"
    @JvmField var UPGRADES_BASE_TRAP_ITEM_NAME_PATH: String = "upgrades-base-trap-name-"
    @JvmField var UPGRADES_BASE_TRAP_ITEM_LORE_PATH: String = "upgrades-base-trap-lore-"
    @JvmField var UPGRADES_TRAP_CUSTOM_TITLE: String = "upgrades-base-trap-title-"
    @JvmField var UPGRADES_TRAP_CUSTOM_SUBTITLE: String = "upgrades-base-trap-subtitle-"
    @JvmField var UPGRADES_TRAP_CUSTOM_MSG: String = "upgrades-base-trap-msg-"
    @JvmField var UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_MONEY: String = "upgrades-lore-insuff-money"
    @JvmField var UPGRADES_LORE_REPLACEMENT_CLICK_TO_BUY: String = "upgrades-lore-click-buy"
    @JvmField var UPGRADES_LORE_REPLACEMENT_UNLOCKED: String = "upgrades-lore-unlocked"
    @JvmField var UPGRADES_LORE_REPLACEMENT_LOCKED: String = "upgrades-lore-locked"
    @JvmField var UPGRADES_UPGRADE_BOUGHT_CHAT: String = "upgrades-new-purchase"
    @JvmField var UPGRADES_TRAP_QUEUE_LIMIT: String = "upgrades-trap-queue-full"
    @JvmField var UPGRADES_TRAP_DEFAULT_TITLE: String = "upgrades-trap-default-title"
    @JvmField var UPGRADES_TRAP_DEFAULT_SUBTITLE: String = "upgrades-trap-default-subtitle"
    @JvmField var UPGRADES_TRAP_DEFAULT_MSG: String = "upgrades-trap-default-msg"
    @JvmField var SHOP_NEW_PURCHASE: String = "shop-new-purchase"
    @JvmField var SHOP_UTILITY_NPC_SILVERFISH_NAME: String = "shop-utility-silverfish"
    @JvmField var SHOP_UTILITY_NPC_IRON_GOLEM_NAME: String = "shop-utility-iron-golem"
    @JvmField var SHOP_INSUFFICIENT_MONEY: String = "shop-insuff-money"
    @JvmField var SHOP_ALREADY_BOUGHT: String = "shop-already-bought"
    const val SHOP_PATH: String = "shop-items-messages"
    const val SHOP_LORE_STATUS_CANT_AFFORD: String = "shop-lore-status-cant-afford"
    const val SHOP_LORE_STATUS_CAN_BUY: String = "shop-lore-status-can-buy"
    const val SHOP_LORE_STATUS_MAXED: String = "shop-lore-status-tier-maxed"
    const val SHOP_LORE_STATUS_ARMOR: String = "shop-lore-status-armor"
    const val SHOP_LORE_QUICK_ADD: String = "shop-lore-quick-add"
    const val SHOP_LORE_QUICK_REMOVE: String = "shop-lore-quick-remove"
    @JvmField val SHOP_INDEX_NAME: String = SHOP_PATH + ".inventory-name"
    @JvmField val SHOP_QUICK_ADD_NAME: String = SHOP_PATH + ".quick-buy-add-inventory-name"
    @JvmField val SHOP_SEPARATOR_NAME: String = SHOP_PATH + ".separator-item-name"
    @JvmField val SHOP_SEPARATOR_LORE: String = SHOP_PATH + ".separator-item-lore"
    @JvmField val SHOP_QUICK_BUY_NAME: String = SHOP_PATH + ".quick-buy-item-name"
    @JvmField val SHOP_QUICK_BUY_LORE: String = SHOP_PATH + ".quick-buy-item-lore"
    @JvmField val SHOP_QUICK_EMPTY_NAME: String = SHOP_PATH + ".quick-buy-empty-item-name"
    @JvmField val SHOP_QUICK_EMPTY_LORE: String = SHOP_PATH + ".quick-buy-empty-item-lore"
    @JvmField val SHOP_CATEGORY_INVENTORY_NAME: String = SHOP_PATH + ".%category%.inventory-name"
    @JvmField val SHOP_CATEGORY_ITEM_NAME: String = SHOP_PATH + ".%category%.category-item-name"
    @JvmField val SHOP_CATEGORY_ITEM_LORE: String = SHOP_PATH + ".%category%.category-item-lore"
    @JvmField val SHOP_CONTENT_TIER_ITEM_NAME: String = SHOP_PATH + ".%category%.content-item-%content%-name"
    @JvmField val SHOP_CONTENT_TIER_ITEM_LORE: String = SHOP_PATH + ".%category%.content-item-%content%-lore"
    @JvmField val SHOP_CAN_BUY_COLOR: String = SHOP_PATH + ".can-buy-color"
    @JvmField val SHOP_CANT_BUY_COLOR: String = SHOP_PATH + ".cant-buy-color"
    /* MultiArena Lobby Item Messages */
    @JvmField val GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME: String = ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH + "-%path%-name"
    @JvmField val GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE: String = ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH + "-%path%-lore"
    /* Spectator Items Messages */
    @JvmField val GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME: String = ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH + "-%path%-name"
    @JvmField val GENERAL_CONFIGURATION_SPECTATOR_ITEMS_LORE: String = ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH + "-%path%-lore"
    /* Arena waiting Items Messages */
    @JvmField val GENERAL_CONFIGURATION_WAITING_ITEMS_NAME: String = ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH + "-%path%-name"
    @JvmField val GENERAL_CONFIGURATION_WAITING_ITEMS_LORE: String = ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH + "-%path%-lore"
    // LOBBY TAB
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_HEADER_LOBBY: String = FORMATTING_SB_TAB_LOBBY_HEADER
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_FOOTER_LOBBY: String = FORMATTING_SB_TAB_LOBBY_FOOTER
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_PREFIX_LOBBY: String = FORMATTING_SB_TAB_LOBBY_PREFIX
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_SUFFIX_LOBBY: String = FORMATTING_SB_TAB_LOBBY_SUFFIX
    // WAITING TAB
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_HEADER_WAITING: String = FORMATTING_SB_TAB_WAITING_HEADER
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_FOOTER_WAITING: String = FORMATTING_SB_TAB_WAITING_FOOTER
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_PREFIX_WAITING: String = FORMATTING_SB_TAB_WAITING_PREFIX
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_SUFFIX_WAITING: String = FORMATTING_SB_TAB_WAITING_SUFFIX
    // STARTING TAB
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_HEADER_STARTING: String = FORMATTING_SB_TAB_STARTING_HEADER
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_FOOTER_STARTING: String = FORMATTING_SB_TAB_STARTING_FOOTER
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_PREFIX_STARTING: String = FORMATTING_SB_TAB_STARTING_PREFIX
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_SUFFIX_STARTING: String = FORMATTING_SB_TAB_STARTING_SUFFIX
    // PLAYING TAB
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_HEADER_PLAYING: String = FORMATTING_SB_TAB_PLAYING_HEADER
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_FOOTER_PLAYING: String = FORMATTING_SB_TAB_PLAYING_FOOTER
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_PREFIX_PLAYING: String = FORMATTING_SB_TAB_PLAYING_PREFIX
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_SUFFIX_PLAYING: String = FORMATTING_SB_TAB_PLAYING_SUFFIX
    // SPECTATING TAB
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_HEADER_SPECTATOR: String = FORMATTING_SB_TAB_PLAYING_SPEC_HEADER
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_FOOTER_SPECTATOR: String = FORMATTING_SB_TAB_PLAYING_SPEC_FOOTER
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_PREFIX_SPECTATOR: String = FORMATTING_SB_TAB_PLAYING_SPEC_PREFIX
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_SUFFIX_SPECTATOR: String = FORMATTING_SB_TAB_PLAYING_SPEC_SUFFIX
    // RESTARTING TAB
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_PREFIX_PRESTARTING: String = FORMATTING_SB_TAB_RESTARTING_WIN2_PREFIX
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_PREFIX_RESTARTING: String = FORMATTING_SB_TAB_RESTARTING_WIN2_PREFIX
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_SUFFIX_PRESTARTING: String = FORMATTING_SB_TAB_RESTARTING_WIN2_SUFFIX
    @Deprecated("")
    @JvmField var FORMATTING_SCOREBOARD_TAB_SUFFIX_RESTARTING: String = FORMATTING_SB_TAB_RESTARTING_WIN2_SUFFIX
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_HEADER_RESTARTING: String = FORMATTING_SB_TAB_RESTARTING_ELM_HEADER
    @Deprecated("")
    @JvmField var FORMATTING_SIDEBAR_TAB_FOOTER_RESTARTING: String = FORMATTING_SB_TAB_PLAYING_SPEC_HEADER
    @Deprecated("")
    @JvmField var FORMATTING_SPECTATOR_TEAM: String = "format-spectator-team"
    @Deprecated("")
    @JvmField var FORMATTING_SPECTATOR_COLOR: String = FORMATTING_SPECTATOR_TEAM
}
