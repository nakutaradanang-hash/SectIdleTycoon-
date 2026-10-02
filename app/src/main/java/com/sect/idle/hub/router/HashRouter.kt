package com.sect.idle.hub.router

import androidx.compose.runtime.*
import java.util.ArrayDeque

/**
 * HashRouter - Declarative, State-Driven Hash Route Navigation Engine for Jetpack Compose.
 *
 * Supports URL-like hash routing patterns (e.g. `#/dashboard`, `#/settings`, `#/settings/audio`, `#/cultivation?discipleId=3`).
 * Maintains a reliable internal navigation backstack and provides route history management.
 */
class HashRouter(initialRoute: String = "#/dashboard") {

    private val backStack = ArrayDeque<String>()

    var currentRoute by mutableStateOf(initialRoute)
        private set

    init {
        backStack.push(initialRoute)
    }

    /**
     * Navigates to a new hash route and pushes it to the backstack.
     */
    fun navigate(route: String) {
        val cleanRoute = sanitizeRoute(route)
        if (cleanRoute != currentRoute) {
            backStack.push(cleanRoute)
            currentRoute = cleanRoute
        }
    }

    /**
     * Replaces the current top route without growing the backstack.
     */
    fun replace(route: String) {
        val cleanRoute = sanitizeRoute(route)
        if (backStack.isNotEmpty()) {
            backStack.pop()
        }
        backStack.push(cleanRoute)
        currentRoute = cleanRoute
    }

    /**
     * Navigates back in history. Returns true if navigation succeeded, false if at root.
     */
    fun pop(): Boolean {
        if (backStack.size > 1) {
            backStack.pop()
            currentRoute = backStack.peek() ?: "#/dashboard"
            return true
        }
        return false
    }

    /**
     * Checks whether the current route matches or starts with the given path prefix.
     */
    fun isActive(pathPrefix: String): Boolean {
        val baseCurrent = getBasePath(currentRoute)
        val targetBase = getBasePath(sanitizeRoute(pathPrefix))
        return baseCurrent.startsWith(targetBase)
    }

    /**
     * Extracts query parameter value from current route (e.g., `#/settings?tab=audio`).
     */
    fun getQueryParam(paramName: String): String? {
        val queryIndex = currentRoute.indexOf('?')
        if (queryIndex < 0) return null
        val queryString = currentRoute.substring(queryIndex + 1)
        val pairs = queryString.split("&")
        for (pair in pairs) {
            val parts = pair.split("=")
            if (parts.size == 2 && parts[0] == paramName) {
                return parts[1]
            }
        }
        return null
    }

    fun getBasePath(route: String = currentRoute): String {
        val clean = sanitizeRoute(route)
        val queryIndex = clean.indexOf('?')
        return if (queryIndex >= 0) clean.substring(0, queryIndex) else clean
    }

    fun getStackDepth(): Int = backStack.size

    private fun sanitizeRoute(route: String): String {
        var r = route.trim()
        if (!r.startsWith("#")) {
            r = if (r.startsWith("/")) "#$r" else "#/$r"
        }
        return r
    }
}

val LocalHashRouter = compositionLocalOf<HashRouter> {
    error("No HashRouter provided in composition")
}
