package com.example

import com.example.data.model.RadioStation
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testNormalizeStreamUrl() {
    val raw89 = "https://playerservices.streamtheworld.com/api/livestream-redirect/RADIO_89FM_ADP.aac?dist=site-89fm"
    val expected89 = "https://playerservices.streamtheworld.com/api/livestream-redirect/RADIO_89FM_SC?dist=site-89fm"
    assertEquals(expected89, RadioStation.normalizeStreamUrl(raw89))

    val rawKiss1 = "https://26593.live.streamtheworld.com/RADIO_KISSFM_ADP.aac"
    val expectedKiss1 = "https://playerservices.streamtheworld.com/api/livestream-redirect/RADIO_KISSFM_SC"
    assertEquals(expectedKiss1, RadioStation.normalizeStreamUrl(rawKiss1))

    val rawKiss2 = "https://playerservices.streamtheworld.com/api/livestream-redirect/RADIO_KISSFMAAC.aac?1655609055880"
    val expectedKiss2 = "https://playerservices.streamtheworld.com/api/livestream-redirect/RADIO_KISSFM_SC?1655609055880"
    assertEquals(expectedKiss2, RadioStation.normalizeStreamUrl(rawKiss2))

    val standardMp3 = "https://dispatcher.rndfnk.com/swr/swr3/live/mp3/128/stream.mp3"
    assertEquals(standardMp3, RadioStation.normalizeStreamUrl(standardMp3))
  }
}
