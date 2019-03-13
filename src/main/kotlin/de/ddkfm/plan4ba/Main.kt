package de.ddkfm.plan4ba

import de.ddkfm.plan4ba.models.Result
import kong.unirest.Unirest
import org.apache.http.client.HttpClient
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.jsoup.Jsoup
import spark.Spark.get
import spark.Spark.halt
import spark.kotlin.port
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*


fun main(args : Array<String>) {
    val httpPort = getEnvOrDefault("HTTP_PORT", "8080")
    port(8080)

    get("/login") { req, resp ->
        resp.type("application/json")
        try {
            val auth = req.headers("Authorization")
            if (auth == null || !auth.startsWith("Basic ")) {
                resp.header("WWW-Authenticate", "Basic realm=\"Anmeldung wird benötigt\"")
                halt(401, "Unauthorized")
            } else {
                val encoded = String(Base64.getDecoder().decode(auth.replace("Basic", "").trim().toByteArray()))
                if (encoded.contains(":")) {
                    val username = encoded.split(":")[0]
                    val password = encoded.split(":")[1]
                    var result = Result()
                    if(getEnvOrDefault("ENABLE_DUMMY_MODE", "false").toBoolean())
                        result = loginDummy(username, password)
                    else {
                        result = loginWithUnirest(username, password)
                    }
                    if (result.isValide()) {
                        result.toJson()
                    } else {
                        val result = mapOf(
                            "status" to 400,
                            "message" to "username or password are not given"
                        )
                        halt(400, result.toJson())
                    }
                } else {
                    halt(401, mapOf("status" to 401, "message" to "unauthorized").toJson())
                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
            halt(401, mapOf("status" to 401, "message" to "unauthorized").toJson())
        }
    }
}

fun loginDummy(username: String, password: String) : Result {
    return Result(
        forename = "Maximilian",
        surename = "Schädlich",
        university = "Staatliche Studienakademie Leipzig",
        group = "",
        course = "",
        hash = ""
    )
}

fun loginWithUnirest(username: String, password: String) : Result {
    val instance = Unirest.primaryInstance()
    instance
        .config()
        .httpClient(makeHttpClient())
    val result = Result()
    val firstLogin = instance.get("https://erp.campus-dual.de/sap/bc/webdynpro/sap/zba_initss?sap-client=100&sap-language=de&uri=https://selfservice.campus-dual.de/index/login")
            .header("Connection", "keep-alive")
            .header("Cache-Control", "max-age=0")
            .header("Upgrade-Insecure-Requests", "1")
            .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7,de-AT;q=0.6")

    val resp = firstLogin.asString()
    val respDoc = Jsoup.parse(resp.body)
    val xsrfToken = respDoc.getElementsByAttributeValue("name", "sap-login-XSRF").attr("value")
    val cookies = resp.headers["set-cookie"]
    val secondRequest = instance.post("https://erp.campus-dual.de/sap/bc/webdynpro/sap/zba_initss?sap-client=100&sap-language=de&uri=https://selfservice.campus-dual.de/index/login")
            .header("Connection", "keep-alive")
            .header("Cache-Control", "max-age=0")
            .header("Origin", "https://erp.campus-dual.de")
            .header("Upgrade-Insecure-Requests", "1")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
            .header("Referer", "https://erp.campus-dual.de/sap/bc/webdynpro/sap/zba_initss?sap-client=100&sap-language=de&uri=https://selfservice.campus-dual.de/index/login")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7,de-AT;q=0.6")
            .header("Cookie", cookies?.joinToString(separator = "; ") ?: "")
            .field("FOCUS_ID", "sap-user")
            .field("sap-system-login-oninputprocessing", "onLogin")
            .field("sap-urlscheme", "")
            .field("sap-system-login", "onLogin")
            .field("sap-system-login-basic_auth", "")
            .field("sap-client", "100")
            .field("sap-language", "DE")
            .field("sap-accessibility", "")
            .field("sap-login-XSRF", "$xsrfToken")
            .field("sap-system-login-ookie_disabled", "")
            .field("sap-user", "$username")
            .field("sap-password", "$password")
            .field("SAPEVENTQUEUE", "Form_Submit%7EE002Id%7EE004SL__FORM%7EE003%7EE002ClientAction%7EE004submit%7EE005ActionUrl%7EE004%7EE005ResponseData%7EE004full%7EE005PrepareScript%7EE004%7EE003%7EE002%7EE003")
    val secondResp = secondRequest.asString()

    val thirdLogin = instance.get("https://selfservice.campus-dual.de/index/login")
            .header("Connection", "keep-alive")
            .header("Cache-Control", "max-age=0")
            .header("Upgrade-Insecure-Requests", "1")
            .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
            .header("Referer", "https://selfservice.campus-dual.de/index/logout")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7,de-AT;q=0.6")
            .header("Cookie", secondResp.headers["set-cookie"]?.joinToString(separator = "; ") ?: "")

    val thirdResp = thirdLogin.asString()

    val content = thirdResp.body

    result.hash = "[0-9a-f]{32}"
            .toRegex()
            .find(content)
            ?.value ?: ""

    val doc = Jsoup.parse(content)
    val studInfoDiv = doc.select("#studinfo td").first()
    var studInfo = studInfoDiv.html()
    studInfo = studInfo.replace("<\\/?strong>|\\(\\d+\\)|(Name|Seminargruppe)\\:".toRegex(), "")
    var params = studInfo
            .split("(<br>|,)".toRegex())
            .map { it.trim() }

    result.surename = params[0]
    result.forename = params[1]
    result.group = params[2]
    result.course = params[3]
    result.university = doc.select("a[href=\"/dash/index\"]").first().html()
    return result
}

fun makeHttpClient() : HttpClient? {
    val builder = SSLContextBuilder()
    var httpclient: CloseableHttpClient? = null
    try {
        // builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        builder.loadTrustMaterial(null, object : TrustStrategy {
            @Throws(CertificateException::class)
            override fun isTrusted(chain: Array<X509Certificate>, authType: String): Boolean {
                return true
            }
        })
        val sslsf = SSLConnectionSocketFactory(
            builder.build())
        httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build()
        println("custom httpclient called")
        System.out.println(httpclient)

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: KeyStoreException) {
        e.printStackTrace()
    } catch (e: KeyManagementException) {
        e.printStackTrace()
    }


    return httpclient
}