package com.example.magiceightball

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EightBallTest {

    private val eightBall = EightBall()

    // ── Response pool ────────────────────────────────────────────────

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

    // ── Basic ask / shake contract ───────────────────────────────────

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
    fun `response text is never empty`() {
        repeat(50) {
            val response = eightBall.shake()
            assertTrue(response.text.isNotBlank())
        }
    }

    @Test
    fun `ask is non-deterministic across multiple calls`() {
        val responses = (1..30).map { eightBall.ask("What will happen?") }.toSet()
        assertTrue("Expected multiple distinct responses, got ${responses.size}", responses.size > 1)
    }

    // ── Keyword bias (statistical) ──────────────────────────────────

    @Test
    fun `positive keyword bias increases positive response probability`() {
        val positiveQuestion = "love happy good great succeed"
        val results = (1..200).map { eightBall.ask(positiveQuestion).sentiment }
        val positiveCount = results.count { it == EightBall.Sentiment.POSITIVE }
        assertTrue(
            "Expected positive responses to dominate, got $positiveCount/200",
            positiveCount > 100
        )
    }

    @Test
    fun `negative keyword bias increases negative response probability`() {
        val negativeQuestion = "fail bad wrong lose hurt afraid"
        val results = (1..200).map { eightBall.ask(negativeQuestion).sentiment }
        val negativeCount = results.count { it == EightBall.Sentiment.NEGATIVE }
        assertTrue(
            "Expected negative responses to dominate, got $negativeCount/200",
            negativeCount > 100
        )
    }

    @Test
    fun `uncertain keyword bias increases neutral response probability`() {
        val uncertainQuestion = "maybe perhaps possibly unsure uncertain"
        val results = (1..200).map { eightBall.ask(uncertainQuestion).sentiment }
        val neutralCount = results.count { it == EightBall.Sentiment.NEUTRAL }
        assertTrue(
            "Expected neutral responses to dominate, got $neutralCount/200",
            neutralCount > 100
        )
    }

    // ── Normalized weighting ─────────────────────────────────────────

    @Test
    fun `negative keywords are as effective as positive keywords`() {
        // The old per-response pool algorithm gave positive keywords ~75% hit rate
        // but negative only ~50% because there are 10 positive vs 5 negative responses.
        // The group-normalized algorithm should give both about the same rate.
        val posResults = (1..500).map { eightBall.ask("love happy good").sentiment }
        val negResults = (1..500).map { eightBall.ask("fail bad wrong").sentiment }

        val posRate = posResults.count { it == EightBall.Sentiment.POSITIVE } / 500.0
        val negRate = negResults.count { it == EightBall.Sentiment.NEGATIVE } / 500.0

        // Both should be roughly equal (within 15 percentage points)
        assertTrue(
            "Positive rate ($posRate) and negative rate ($negRate) should be similar",
            kotlin.math.abs(posRate - negRate) < 0.15
        )
    }

    @Test
    fun `blank question gives roughly uniform sentiment distribution`() {
        val results = (1..600).map { eightBall.ask("").sentiment }
        val posCount = results.count { it == EightBall.Sentiment.POSITIVE }
        val neuCount = results.count { it == EightBall.Sentiment.NEUTRAL }
        val negCount = results.count { it == EightBall.Sentiment.NEGATIVE }

        // Each sentiment should get roughly 1/3 (200 out of 600) — allow wide margin
        assertTrue("Positive count $posCount too low", posCount > 120)
        assertTrue("Positive count $posCount too high", posCount < 280)
        assertTrue("Neutral count $neuCount too low", neuCount > 120)
        assertTrue("Neutral count $neuCount too high", neuCount < 280)
        assertTrue("Negative count $negCount too low", negCount > 120)
        assertTrue("Negative count $negCount too high", negCount < 280)
    }

    // ── Expanded keyword vocabulary ──────────────────────────────────

    @Test
    fun `morphological variants are recognised as keywords`() {
        // "successful" should now trigger positive bias (was not matched before)
        val results = (1..200).map { eightBall.ask("Will I be successful?").sentiment }
        val positiveCount = results.count { it == EightBall.Sentiment.POSITIVE }
        assertTrue(
            "Expected 'successful' to boost positive, got $positiveCount/200",
            positiveCount > 80
        )
    }

    @Test
    fun `negative morphological variants are recognised`() {
        // "failure" and "worried" should both be matched
        val results = (1..200).map { eightBall.ask("I am worried about failure").sentiment }
        val negativeCount = results.count { it == EightBall.Sentiment.NEGATIVE }
        assertTrue(
            "Expected 'worried' and 'failure' to boost negative, got $negativeCount/200",
            negativeCount > 100
        )
    }

    @Test
    fun `uncertain variants are recognised`() {
        // "doubtful" and "confused" should be matched
        val results = (1..200).map { eightBall.ask("I am doubtful and confused").sentiment }
        val neutralCount = results.count { it == EightBall.Sentiment.NEUTRAL }
        assertTrue(
            "Expected 'doubtful' and 'confused' to boost neutral, got $neutralCount/200",
            neutralCount > 100
        )
    }

    // ── Keyword set sanity ───────────────────────────────────────────

    @Test
    fun `keyword sets have no overlap`() {
        val posNeg = EightBall.POSITIVE_KEYWORDS intersect EightBall.NEGATIVE_KEYWORDS
        val posUnc = EightBall.POSITIVE_KEYWORDS intersect EightBall.UNCERTAIN_KEYWORDS
        val negUnc = EightBall.NEGATIVE_KEYWORDS intersect EightBall.UNCERTAIN_KEYWORDS

        assertTrue("Positive and negative overlap: $posNeg", posNeg.isEmpty())
        assertTrue("Positive and uncertain overlap: $posUnc", posUnc.isEmpty())
        assertTrue("Negative and uncertain overlap: $negUnc", negUnc.isEmpty())
    }

    @Test
    fun `all keywords are lowercase`() {
        val allKeywords = EightBall.POSITIVE_KEYWORDS +
            EightBall.NEGATIVE_KEYWORDS +
            EightBall.UNCERTAIN_KEYWORDS
        allKeywords.forEach { keyword ->
            assertEquals("Keyword '$keyword' should be lowercase", keyword.lowercase(), keyword)
        }
    }

    // ── HistoryEntry ─────────────────────────────────────────────────

    @Test
    fun `history entry stores question and response`() {
        val response = EightBall.Response("Yes", EightBall.Sentiment.POSITIVE)
        val entry = HistoryEntry("Will I win?", response)
        assertEquals("Will I win?", entry.question)
        assertEquals(response, entry.response)
        assertTrue(entry.timestamp > 0)
    }
}
