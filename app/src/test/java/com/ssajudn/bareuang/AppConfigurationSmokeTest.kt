package com.ssajudn.bareuang

import org.junit.Test

import com.ssajudn.bareuang.domain.AppConfig
import org.junit.Assert.assertFalse

/**
 * Application smoke test for stable domain configuration.
 */
class AppConfigurationSmokeTest {
    @Test
    fun defaultWalletConfiguration_isPresent() {
        assertFalse(AppConfig.DEFAULT_WALLET_NAME.isBlank())
    }
}
