package es.unizar.urlshortener.infrastructure.delivery

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Service that limits the number of redirections per key within a configurable time window.
 *
 * @property maxRedirects Maximum number of allowed redirects within the window
 * @property windowSeconds Duration of the window in seconds
 */
@Service
class RedirectionLimiterService(

    @param:Value("\${app.redirection.max:5}")
    private val maxRedirects: Int,

    @param:Value("\${app.redirection.windowSeconds:3600}")
    private val windowSeconds: Long

) {

    /**
     * Holds the counter and the timestamp of the first redirection in the current window
     */
    private data class CounterInfo(
        val counter: AtomicInteger,
        val firstTimestamp: Long
    )

    /**
     * Map to store redirection counts and timestamps per key
     */
    private val counts = ConcurrentHashMap<String, CounterInfo>()

    /**
     * Checks if the redirection is allowed for the given key.
     * If the window expired, the counter resets.
     *
     * @param key The key which redirection allowance is being checked
     * @return `true` if redirection is allowed, `false` if the limit has been reached
     */
    fun isAllowed(key: String): Boolean {
        val now = Instant.now().epochSecond

        val info = counts.compute(key) { _, old ->

            // If no previous record or time window has passed, reset
            if (old == null || now - old.firstTimestamp > windowSeconds) {
                CounterInfo(AtomicInteger(0), now)
            } else {
                old
            }
        }!!

        val current = info.counter.get()

        return if (current < maxRedirects) {
            info.counter.incrementAndGet()
            true
        } else {
            false
        }

        //return info.counter.get() <= maxRedirects
    }

    /**
     * Returns the number of redirects already used for the given key.
     *
     * @param key The key which current redirect count is being retrieved
     * @return Number of redirects used; `0` if no redirects have been made
     */
    fun currentRedirects(key: String): Int =
        counts[key]?.counter?.get() ?: 0

    /**
     * Returns the timestamp when the current redirection limit window will reset.
     *
     * @param key The key which reset time is calculated
     * @return [Instant] representing the reset time, or `null` if the key has no record
     */
    fun resetAt(key: String): Instant? =
        counts[key]?.let { Instant.ofEpochSecond(it.firstTimestamp + windowSeconds) }

    /**
     * Returns the maximum allowed redirects within a window.
     *
     * @return Maximum number of redirects
     */    
    fun getMaxRedirects(): Int = maxRedirects

    /**
     * Returns the duration of the redirection limit window in seconds.
     *
     * @return Duration of the window in seconds
     */
    fun getWindowSeconds(): Long = windowSeconds
}
