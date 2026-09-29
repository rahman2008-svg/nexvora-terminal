package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NexVora Terminal", appName)
  }

  @Test
  fun `test json formatting and base64 dev utilities`() {
    val json = "{\"key\":\"value\"}"
    val (ok, formatted) = com.example.developer.DevTools.formatJson(json)
    assertEquals(true, ok)
    org.junit.Assert.assertTrue(formatted.contains("\"key\": \"value\""))

    val b64 = com.example.developer.DevTools.encodeBase64("NexVora")
    val (decOk, decoded) = com.example.developer.DevTools.decodeBase64(b64)
    assertEquals(true, decOk)
    assertEquals("NexVora", decoded)
  }
}
