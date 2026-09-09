package com.ssajudn.bareuang.data.error

import com.ssajudn.bareuang.domain.error.AppException
import java.io.IOException
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorParserTest {
    @Test
    fun `maps io failure to network exception`() {
        assertTrue(ApiErrorParser.fromThrowable(IOException("offline")) is AppException.NetworkException)
    }

    @Test
    fun `maps invalid state to data exception`() {
        assertTrue(ApiErrorParser.fromThrowable(IllegalStateException("bad data")) is AppException.DataException)
    }

    @Test
    fun `preserves existing app exception`() {
        val original = AppException.DataException("known")
        assertTrue(ApiErrorParser.fromThrowable(original) is AppException.DataException)
    }
}
