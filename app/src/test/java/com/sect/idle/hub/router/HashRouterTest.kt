package com.sect.idle.hub.router

import org.junit.Assert
import org.junit.Test

class HashRouterTest {

    @Test
    fun testInitialRoute() {
        val router = HashRouter("#/dashboard")
        Assert.assertEquals("#/dashboard", router.currentRoute)
        Assert.assertEquals(1, router.getStackDepth())
        Assert.assertTrue(router.isActive("#/dashboard"))
    }

    @Test
    fun testNavigateAndBackstackPush() {
        val router = HashRouter("#/dashboard")
        router.navigate("#/settings")
        Assert.assertEquals("#/settings", router.currentRoute)
        Assert.assertEquals(2, router.getStackDepth())

        router.navigate("#/settings/audio")
        Assert.assertEquals("#/settings/audio", router.currentRoute)
        Assert.assertEquals(3, router.getStackDepth())
        Assert.assertTrue(router.isActive("#/settings"))
    }

    @Test
    fun testPopNavigation() {
        val router = HashRouter("#/dashboard")
        router.navigate("#/disciples")
        router.navigate("#/settings")

        val popped = router.pop()
        Assert.assertTrue(popped)
        Assert.assertEquals("#/disciples", router.currentRoute)

        val poppedRoot = router.pop()
        Assert.assertTrue(poppedRoot)
        Assert.assertEquals("#/dashboard", router.currentRoute)

        val cannotPopRoot = router.pop()
        Assert.assertFalse(cannotPopRoot)
        Assert.assertEquals("#/dashboard", router.currentRoute)
    }

    @Test
    fun testReplaceRoute() {
        val router = HashRouter("#/dashboard")
        router.navigate("#/settings")
        Assert.assertEquals(2, router.getStackDepth())

        router.replace("#/settings/themes")
        Assert.assertEquals("#/settings/themes", router.currentRoute)
        Assert.assertEquals(2, router.getStackDepth()) // Depth should not increase on replace
    }

    @Test
    fun testQueryParamsExtraction() {
        val router = HashRouter("#/cultivation?discipleId=disciple_99&arrayLevel=3")
        Assert.assertEquals("#/cultivation", router.getBasePath())
        Assert.assertEquals("disciple_99", router.getQueryParam("discipleId"))
        Assert.assertEquals("3", router.getQueryParam("arrayLevel"))
        Assert.assertNull(router.getQueryParam("nonExistent"))
    }
}
