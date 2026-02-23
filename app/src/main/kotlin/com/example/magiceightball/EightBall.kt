package com.example.magiceightball

/**
 * Core Magic Eight Ball logic with intelligent, context-aware responses.
 *
 * The ball analyses the user's question for keywords and adjusts the probability
 * weighting of positive, neutral, and negative response pools so the answer feels
 * relevant to the nature of the question asked.
 *
 * Uses group-normalized weighting: each sentiment group gets an equal base weight,
 * then keyword hits boost the matching group. This ensures keywords have proportional
 * impact regardless of the unequal response counts per group (10/5/5).
 */
class EightBall {

    enum class Sentiment { POSITIVE, NEUTRAL, NEGATIVE }

    data class Response(val text: String, val sentiment: Sentiment)

    companion object {
        /** Weight added per keyword hit to the matching sentiment group. */
        private const val KEYWORD_BOOST = 2.0

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

        /** Expanded keyword sets with common morphological variants. */
        val POSITIVE_KEYWORDS = setOf(
            "love", "loved", "loving", "lovely",
            "happy", "happiness", "happily",
            "good", "great",
            "succeed", "success", "successful", "successfully",
            "win", "winning", "winner",
            "achieve", "achieved", "achievement", "achieving",
            "help", "helped", "helpful", "helping",
            "better", "best",
            "improve", "improved", "improvement", "improving",
            "wonderful", "wonderfully",
            "amazing", "amazed", "amazingly",
            "excellent", "excellence",
            "fantastic",
            "perfect", "perfectly",
            "right",
            "hope", "hoping", "hopeful", "hopefully",
            "lucky", "fortunate", "fortune",
            "brilliant", "awesome", "outstanding",
            "confident", "confidence",
            "joy", "joyful",
            "positive", "optimistic",
            "beautiful", "superb", "exceptional",
            "thrive", "thriving", "prosper", "prosperity"
        )

        val NEGATIVE_KEYWORDS = setOf(
            "fail", "failed", "failing", "failure",
            "bad", "badly",
            "wrong", "wrongly",
            "lose", "losing", "loser", "lost",
            "hurt", "hurting", "hurtful",
            "afraid",
            "worried", "worry", "worrying",
            "risk", "risky",
            "danger", "dangerous", "dangerously",
            "problem", "problematic", "problems",
            "issue", "issues",
            "trouble", "troubled", "troubling",
            "difficult", "difficulty",
            "worse", "worst",
            "terrible", "terribly",
            "awful", "awfully",
            "horrible", "horribly",
            "never",
            "impossible", "impossibly",
            "hate", "hated", "hating", "hatred",
            "sad", "sadly", "sadness",
            "angry", "anger",
            "fear", "fearful", "feared",
            "regret", "regretful", "regretting",
            "disaster", "disastrous",
            "miserable", "miserably",
            "unfortunate", "unfortunately",
            "painful", "painfully", "pain",
            "destroy", "destroyed", "destruction",
            "ruin", "ruined"
        )

        val UNCERTAIN_KEYWORDS = setOf(
            "maybe", "perhaps", "possibly", "possible",
            "unsure", "uncertain", "uncertainty",
            "think", "thinking",
            "guess", "guessing",
            "wonder", "wondering",
            "doubt", "doubtful", "doubting",
            "complicated",
            "confusing", "confused", "confuse",
            "might", "could",
            "sometimes", "somehow",
            "unclear", "unknown",
            "depends", "depending",
            "questionable",
            "undecided", "indecisive",
            "probably", "likely",
            "chance", "chances"
        )
    }

    private val random = kotlin.random.Random

    /**
     * Returns a response intelligently weighted based on [question] keywords.
     *
     * Uses group-normalized weighting: each sentiment group (positive, neutral,
     * negative) gets an equal base weight of 1.0, then keyword hits boost the
     * matching group's weight by [KEYWORD_BOOST] per hit. This ensures keywords
     * have proportional impact regardless of response counts per group (10/5/5).
     *
     * If [question] is blank, all three groups have equal weight (uniform 1/3 each).
     */
    fun ask(question: String): Response {
        val words = question.lowercase().split(Regex("\\W+")).filter { it.isNotEmpty() }.toSet()

        val positiveHits = words.count { it in POSITIVE_KEYWORDS }
        val negativeHits = words.count { it in NEGATIVE_KEYWORDS }
        val uncertainHits = words.count { it in UNCERTAIN_KEYWORDS }

        // Weight each sentiment GROUP equally, then boost by keyword hits.
        val positiveWeight = 1.0 + positiveHits * KEYWORD_BOOST
        val neutralWeight = 1.0 + uncertainHits * KEYWORD_BOOST
        val negativeWeight = 1.0 + negativeHits * KEYWORD_BOOST
        val totalWeight = positiveWeight + neutralWeight + negativeWeight

        // Roll to pick a sentiment group
        val roll = random.nextDouble() * totalWeight
        val sentiment = when {
            roll < positiveWeight -> Sentiment.POSITIVE
            roll < positiveWeight + neutralWeight -> Sentiment.NEUTRAL
            else -> Sentiment.NEGATIVE
        }

        // Pick a random response from the chosen group
        val candidates = ALL_RESPONSES.filter { it.sentiment == sentiment }
        return candidates[random.nextInt(candidates.size)]
    }

    /** Returns a completely random response, ignoring question content. */
    fun shake(): Response = ALL_RESPONSES[random.nextInt(ALL_RESPONSES.size)]
}
