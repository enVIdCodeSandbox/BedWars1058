package com.andrei1058.bedwars.api.configuration

/**
 * Marks a [ConfigPath] constant that an arena configuration may override.
 * The annotation is read from the constant's JVM field (see ArenaConfig), so it targets fields.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class GameMainOverridable
