@file:JvmName("JavaInterop")

package com.andrei1058.spigot.sidebar

/*
 * These sources were converted from Java. Two helpers keep Java's behaviour around null where the original code
 * relied on it, without changing any public signature:
 *
 *  - a Java reference field is null until it is assigned       -> `var x: T = uninitialized()`
 *  - Java hands a possibly-null value on without checking it   -> `value.unchecked()`
 *
 * Both let null through unchecked, exactly like Java did. Replace them with real nullable types (`T?`) or with
 * `lateinit` / `!!` once a place is known to be safe.
 */

/** Initial value of a field that Java left unassigned (null) until its first assignment. */
@Suppress("UNCHECKED_CAST")
fun <T> uninitialized(): T = null as T

/** Passes a possibly-null value on without a null check, like Java does. */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> T?.unchecked(): T = this as T
