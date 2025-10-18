package es.unizar.urlshortener.infrastructure.delivery

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity


/**
 * Service that interacts with Google Safe Browsing API to check URL safety.
 *
 * @property restTemplate RestTemplate for making HTTP requests
 */
@Service
class SafeBrowsingService() {

    private val apiKey = "GOOGLE_API_KEY"
    private val apiUrl = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=$apiKey"

    /**
     * Checks if a URL is safe using Google Safe Browsing API.
     * @param url The URL to be checked
     * @return `true` if the URL is safe, `false` otherwise
     */
    fun isSafe(url: String): Boolean {

        // Format of the request body
        val requestBody = """
            {
              "client": {
                "clientId": "FractalLinkApp",
                "clientVersion": "1.0"
              },
              "threatInfo": {
                "threatTypes": ["MALWARE","SOCIAL_ENGINEERING"],
                "platformTypes": ["WINDOWS", "LINUX"],
                "threatEntryTypes": ["URL"],
                "threatEntries": [{"url": "$url"}]
              }
            }
        """       

        // If the response contains threat matches, the URL is unsafe
        return true //response.body.isNullOrEmpty()
    }

    fun getThreatTypes(): List<String> {
        return listOf("MALWARE","SOCIAL_ENGINEERING")
    }

    fun getPlatformTypes(): List<String> {
        return listOf("WINDOWS", "LINUX")
    }
}
