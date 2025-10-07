package es.unizar.urlshortener.infrastructure.delivery

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/*
 * Service to limit the number of redirects per key within a time window.
 * If the limit is exceeded, further redirects are blocked until the time window passes.
*/
@Service
class RedirectionLimiterService {

    private val MAX_REDIRECTIONS = 10        // maximum redirects allowed
    private val TIME_WINDOW = 10L        // time window in seconds

    // Map: key -> Pair(counter, timestamp of the first redirect in the current window)
    private val counts = ConcurrentHashMap<String, Pair<AtomicInteger, Long>>()

    fun isAllowed(key: String): Boolean {
        val now = System.currentTimeMillis() / 1000
        // Update the redirection count for the key
        val pair = counts.compute(key) { _, old ->
            // old.first is the counter, old.second is the timestamp

            // If no previous record or time window has passed, reset   
            if (old == null || now - old.second > TIME_WINDOW) {
                Pair(AtomicInteger(1), now)
            }
            // Otherwise, increment the counter
            else {
                Pair(old.first.apply { incrementAndGet() }, old.second) // keep the old timestamp
            }
        }!!

        return pair.first.get() <= MAX_REDIRECTIONS
    }
}