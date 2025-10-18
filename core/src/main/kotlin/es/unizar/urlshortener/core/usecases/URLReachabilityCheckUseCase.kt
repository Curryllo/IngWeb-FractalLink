package es.unizar.urlshortener.core.usecases

import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import java.time.Instant


/**
 * Use case that checks if a URI is reachable (returns a 2xx or 3xx HTTP status).
 */
interface URLReachabilityCheckUseCase {
    fun check(url: String): ReachableResult
}

/**
 * Result of a URL reachability check.
 */
data class ReachableResult (
    val reachable: Boolean,
    val statusCode: Int? = null,
    val responseTime: String? = null,
    val errorType: String? = null,
    val contentType: String? = null,
    val validatedAt: Instant = Instant.now()
)

/**
 * Implementation of [URLReachabilityCheckUseCase] using `HttpURLConnection` to check if the URL is reachable or not.
 *
 * @return A status code indicating if the URL is reachable or not.
 */
class URLReachabilityCheckUseCaseImpl : URLReachabilityCheckUseCase {
    override fun check(url: String): ReachableResult {
        val startTime = System.currentTimeMillis()
        val connect = URL(url).openConnection() as HttpURLConnection

        connect.setConnectTimeout(5000) // 5 seconds timeout to connect
        connect.setReadTimeout(5000)    // 5 seconds timeout to read data
        connect.requestMethod = "HEAD"
        
        return try {
            val responseCode = connect.responseCode
            val endTime = System.currentTimeMillis()
            val responseTime = "${endTime - startTime} ms"

            // If response code is 200 (OK), mark as reachable
            if (responseCode == HttpURLConnection.HTTP_OK) {
                ReachableResult(
                    reachable = true,
                    statusCode = responseCode,
                    responseTime = responseTime,
                    contentType = connect.contentType,
                    validatedAt = Instant.now()
                )
            } else {
                ReachableResult(
                    reachable = false,
                    statusCode = responseCode,
                    responseTime = responseTime,
                    errorType = when (responseCode) {
                        in 300..399 -> "Redirection"
                        in 400..499 -> "Client Error"
                        in 500..599 -> "Server Error"
                        else -> "Other Error"
                    },
                    validatedAt = Instant.now()
                )
            }
        } catch (e: java.net.SocketTimeoutException) {
            val endTime = System.currentTimeMillis()
            val responseTime = "${endTime - startTime} ms"
            ReachableResult(
                reachable = false,
                responseTime = responseTime,
                errorType = "TIMEOUT",
                validatedAt = Instant.now()
            )
        } catch (e: Exception) {    
            val endTime = System.currentTimeMillis()
            val responseTime = "${endTime - startTime} ms"
            ReachableResult(
                reachable = false,
                responseTime = responseTime,
                errorType = "NETWORK_ERROR",
                validatedAt = Instant.now()
            )
        } finally {
            connect.disconnect()
        }
    }
}
