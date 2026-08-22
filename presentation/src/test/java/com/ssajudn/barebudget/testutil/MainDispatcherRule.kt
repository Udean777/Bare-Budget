package com.ssajudn.barebudget.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit [TestWatcher] that swaps [Dispatchers.Main] for a [TestDispatcher] so that
 * code using [Dispatchers.Main] (e.g. `viewModelScope`) can be driven
 * deterministically from unit tests.
 *
 * Usage:
 * ```
 * @get:Rule val mainDispatcherRule = MainDispatcherRule()
 * ```
 *
 * Tests run inside [runTest]; the virtual time advances on the same dispatcher
 * returned by [dispatcher], so call `mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()`
 * (or simply rely on `runTest`'s auto-advance) to flush pending coroutines.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
