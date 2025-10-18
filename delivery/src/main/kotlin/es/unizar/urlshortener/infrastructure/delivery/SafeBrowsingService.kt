package es.unizar.urlshortener.infrastructure.delivery

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import java.net.http.HttpRequest  
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody



/**
 * Service that interacts with Google Safe Browsing API to check URL safety.
 *
 * @property restTemplate RestTemplate for making HTTP requests
 */
@Service
class SafeBrowsingService() {

    //private val apiKey: String? = System.getenv("GOOGLE_API_KEY")
    private val apiKey: String? = "AIzaSyA_5zNXyOW5lOGvnT4pmZV_l0WW0lmHEBY"
    private val apiUrl = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=$apiKey"

    /**
     * Checks if a URL is safe using Google Safe Browsing API.
     * @param url The URL to be checked
     * @return `true` if the URL is safe, `false` otherwise
     */
    fun isSafe(url: String): Boolean {
      println("=== SAFE BROWSING DEBUG ===")
      println("$apiKey")
      println("URL to check: $url")

        // Format of the request body
        /*
        val body = mapOf(
          "client" to mapOf(
            "clientId" to "FractalLinkApp",
            "clientVersion" to "1.5.2"
          ),
          "threatInfo" to mapOf(
            "threatTypes" to listOf("MALWARE", "SOCIAL ENGINERING"),
            "platformTypes" to listOf("WINDOWS", "LINUX"),
            "threatEntryTpes" to listOf("URL"),
            "threatEntries" to listOf(mapOf("url" to url))
          )
        )
         */

        val body = """
            {
              "client": {
                "clientId": "FractalLinkApp",
                "clientVersion": "1.5.2"
              },
              "threatInfo": {
                "threatTypes": ["MALWARE","SOCIAL_ENGINEERING"],
                "platformTypes": ["WINDOWS", "LINUX"],
                "threatEntryTypes": ["URL"],
                "threatEntries": [{"url": "$url"}]
              }
            }
        """.trimIndent()      

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = body.toRequestBody(mediaType)

        val request = Request.Builder().url("$apiUrl").post(requestBody).build()

        val response = client.newCall(request).execute()

        val responseBody = response.body?.string()
        println("📡 Response Body: $responseBody")

        // If the response contains threat matches, the URL is unsafe
        if(response.isSuccessful){
          println("Success")
          return true
        }
        else {
          println("Unsuccess")
          return false
        }
        
    }

    fun getThreatTypes(): List<String> {
        return listOf("MALWARE","SOCIAL_ENGINEERING")
    }

    fun getPlatformTypes(): List<String> {
        return listOf("WINDOWS", "LINUX")
    }
}
