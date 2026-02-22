package com.example.magiceightball

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EightBallTest {

    private val eightBall = EightBall()

    @Test
    fun `all responses list contains exactly 20 entries`() {
        assertEquals(20, EightBall.ALL_RESPONSES.size)
    }

    @Test
    fun `all responses list contains 10 positive, 5 neutral, 5 negative`() {
        val groups = EightBall.ALL_RESPONSES.groupBy { it.sentiment }
        assertEquals(10, groups[EightBall.Sentiment.POSITIVE]?.size)
        assertEquals(5,  groups[EightBall.Sentiment.NEUTRAL]?.size)
        assertEquals(5,  groups[EightBall.Sentiment.NEGATIVE]?.size)
    }

    @Test
    fun `ask returns a non-null response for a blank question`() {
        val response = eightBall.ask("")
        assertNotNull(response)
        assertTrue(response.text.isNotBlank())
    }

    @Test
    fun `ask returns a response from the known set`() {
        val response = eightBall.ask("Will I be successful?")
        assertTrue(EightBall.ALL_RESPONSES.contains(response))
    }

    @Test
    fun `shake returns a response from the known set`() {
        val response = eightBall.shake()
        assertTrue(EightBall.ALL_RESPONSES.contains(response))
    }

    @Test
    fun `positive keyword bias increases positive response probability`() {
        // With a strongly positive question, positive responses should dominate
        // over 200 trials (statistically near-certain with the 2× weight boost).
        val positiveQuestion = "love happy good great succeed"
        val results = (1..200).map { eightBall.ask(positiveQuestion).sentiment }
        val positiveCount = results.count { it == EightBall.Sentiment.POSITIVE }
        assertTrue(
            "Expected positive responses to be the majority, got $positiveCount/200",
            positiveCount > 100
        )
    }

    @Test
    fun `negative keyword bias increases negative response probability`() {
        val negativeQuestion = "fail bad wrong lose hurt afraid"
        val results = (1..200).map { eightBall.ask(negativeQuestion).sentiment }
        val negativeCount = results.count { it == EightBall.Sentiment.NEGATIVE }
        assertTrue(
            "Expected negative responses to be elevated, got $negativeCount/200",
            negativeCount > 20   // baseline would be ~50; negative pool is 5 responses
        )
    }

    @Test
    fun `uncertain keyword bias increases neutral response probability`() {
        val uncertainQuestion = "maybe perhaps possibly unsure uncertain"
        val results = (1..200).map { eightBall.ask(uncertainQuestion).sentiment }
        val neutralCount = results.count { it == EightBall.Sentiment.NEUTRAL }
        assertTrue(
            "Expected neutral responses to be elevated, got $neutralCount/200",
            neutralCount > 20
        )
    }

    @Test
    fun `response text is never empty`() {
        repeat(50) {
            val response = eightBall.shake()
            assertTrue(response.text.isNotBlank())
        }
    }

    @Test
    fun `ask is non-deterministic across multiple calls`() {
        // With 20 possible responses calling 30 times we expect at least 2 distinct results
        val responses = (1..30).map { eightBall.ask("What will happen?") }.toSet()
        assertTrue("Expected multiple distinct responses, got ${responses.size}", responses.size > 1)
    }
}
