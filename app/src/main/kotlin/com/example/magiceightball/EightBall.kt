package com.example.magiceightball

/**
 * Core Magic Eight Ball logic with intelligent, context-aware responses.
 *
 * The ball analyses the user's question for keywords and adjusts the probability
 * weighting of positive, neutral, and negative response pools so the answer feels
 * relevant to the nature of the question asked.
 */
class EightBall {

    enum class Sentiment { POSITIVE, NEUTRAL, NEGATIVE }

    data class Response(val text: String, val sentiment: Sentiment)

    companion object {
        /** All 20 classic Magic 8-Ball responses. */
        val ALL_RESPONSES = listOf(
            // Positive (10)
            Response("It is certain", Sentiment.POSITIVE),
            Response("It is decidedly so", Sentiment.POSITIVE),
            Response("Without a doubt", Sentiment.POSITIVE),
            Response("Yes, definitely", Sentiment.POSITIVE),
            Response("You may rely on it", Sentiment.POSITIVE),
            Response("As I see it, yes", Sentiment.POSITIVE),
            Response("Most likely", Sentiment.POSITIVE),
            Response("Outlook good", Sentiment.POSITIVE),
            Response("Yes", Sentiment.POSITIVE),
            Response("Signs point to yes", Sentiment.POSITIVE),
            // Neutral (5)
            Response("Reply hazy, try again", Sentiment.NEUTRAL),
            Response("Ask again later", Sentiment.NEUTRAL),
            Response("Better not tell you now", Sentiment.NEUTRAL),
            Response("Cannot predict now", Sentiment.NEUTRAL),
            Response("Concentrate and ask again", Sentiment.NEUTRAL),
            // Negative (5)
            Response("Don't count on it", Sentiment.NEGATIVE),
            Response("My reply is no", Sentiment.NEGATIVE),
            Response("My sources say no", Sentiment.NEGATIVE),
            Response("Outlook not so good", Sentiment.NEGATIVE),
            Response("Very doubtful", Sentiment.NEGATIVE)
        )

        /** Keyword sets that nudge the sentiment weighting. */
        private val POSITIVE_KEYWORDS = setOf(
            "love", "happy", "good", "great", "succeed", "success", "win",
            "achieve", "help", "better", "improve", "best", "wonderful",
            "amazing", "excellent", "fantastic", "perfect", "right"
        )

        private val NEGATIVE_KEYWORDS = setOf(
            "fail", "bad", "wrong", "lose", "hurt", "afraid", "worried",
            "risk", "danger", "problem", "issue", "trouble", "difficult",
            "worse", "terrible", "awful", "horrible", "never", "impossible"
        )

        private val UNCERTAIN_KEYWORDS = setOf(
            "maybe", "perhaps", "possibly", "unsure", "uncertain", "think",
            "guess", "wonder", "doubt", "complicated", "confusing"
        )
    }

    private val random = kotlin.random.Random

    /**
     * Returns a response intelligently weighted based on [question] keywords.
     * If [question] is blank, responses are selected with uniform probability.
     */
    fun ask(question: String): Response {
        val lower = question.lowercase()
        val words = lower.split(Regex("\\W+")).toSet()

        val positiveHits = words.count { it in POSITIVE_KEYWORDS }
        val negativeHits = words.count { it in NEGATIVE_KEYWORDS }
        val uncertainHits = words.count { it in UNCERTAIN_KEYWORDS }

        // Build a weighted pool: each response's weight defaults to 1;
        // sentiment-matching responses are boosted by keyword hits.
        val pool = mutableListOf<Response>()
        for (response in ALL_RESPONSES) {
            val weight = when (response.sentiment) {
                Sentiment.POSITIVE -> 1 + positiveHits * 2
                Sentiment.NEGATIVE -> 1 + negativeHits * 2
                Sentiment.NEUTRAL  -> 1 + uncertainHits * 2
            }
            repeat(weight) { pool.add(response) }
        }

        return pool[random.nextInt(pool.size)]
    }

    /** Returns a completely random response, ignoring question content. */
    fun shake(): Response = ALL_RESPONSES[random.nextInt(ALL_RESPONSES.size)]
}
