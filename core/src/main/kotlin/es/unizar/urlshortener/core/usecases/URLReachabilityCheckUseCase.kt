package es.unizar.urlshortener.core.usecases

import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64


/**
 * Use case that checks if a URI is reachable (returns a 2xx or 3xx HTTP status).
 */
interface URLReachabilityCheckUseCase {
    fun check(url: String): Boolean
}

/**
 * Implementation of [URLReachabilityCheckUseCase] using `HttpURLConnection` to check if the URL is reachable or not.
 *
 * @return A status code indicating if the URL is reachable or not.
 */
class URLReachabilityCheckUseCaseImpl : URLReachabilityCheckUseCase {
    override fun check(url: String): Boolean {
        val connect = URL(url).openConnection() as HttpURLConnection
        connect.setConnectTimeout(5000); // 5 seconds timeout to connect
        connect.setReadTimeout(5000);    // 5 seconds timeout to read data
        connect.requestMethod = "HEAD"
        return try {
            val responseCode = connect.responseCode
            responseCode in 200..399
        } catch (e: Exception) {
            false
        } finally {
            connect.disconnect()
        }
    }
}
