package de.ddkfm.plan4ba

import de.ddkfm.plan4ba.models.Result
import org.json.JSONObject
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import spark.Spark.*
import spark.kotlin.port
import java.net.URL
import java.util.*


fun main(args : Array<String>) {
    port(8082)

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
                    val result = login(username, password)
                    if (result.isValide()) {
                        result.toJson()
                    } else {
                        "{ \"status\" : 400, \"message\" : \"username or password are not given\"}"
                    }
                } else {
                    "{ \"status\" : 401, \"message\" : \"unauthorized\"}"
                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
            "{ \"status\" : 401, \"message\" : \"unauthorized\"}"
        }
    }
}


fun login(username: String, password: String): Result {
    var result = Result()
    try {
        val capabilities = DesiredCapabilities.chrome()
        var options = ChromeOptions()
        options.addArguments("--headless")
        options.addArguments("--no-sandbox")
        capabilities.setCapability(ChromeOptions.CAPABILITY, options)
        val driver = RemoteWebDriver(URL("http://localhost:9515"), capabilities)

        driver.get("https://selfservice.campus-dual.de/index/login")

        val userField = driver.findElement(By.id("sap-user"))
        userField.sendKeys(username)

        val passwordField = driver.findElement(By.id("sap-password"))
        passwordField.sendKeys(password)

        userField.submit()

        driver.get("https://selfservice.campus-dual.de/index/login")

        val content = driver.pageSource

        val pattern = "[0-9a-f]{32}".toPattern()
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

        var universityAnchor = doc.select("a[href=\"/dash/index\"]").first().html()
        result.university = universityAnchor

        driver.quit()
    } catch (e : Exception) {
        e.printStackTrace()
    }
    return result
}