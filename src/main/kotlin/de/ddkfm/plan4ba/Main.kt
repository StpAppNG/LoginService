package de.ddkfm.plan4ba

import de.ddkfm.plan4ba.models.Result
import org.json.JSONObject
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import spark.kotlin.port
import spark.kotlin.post
import java.net.URL


fun main(args : Array<String>) {
    port(8080)

    post("/login") {
        response.type("application/json")
        try {
            var jsonObject = JSONObject(request.body())
            if (jsonObject.has("username") && jsonObject.has("password")) {
                val result = login(username = jsonObject.getString("username"),
                        password = jsonObject.getString("password"))
                if (result.isValide()) {
                    result.toJson()
                } else {
                    "{ \"status\" : 400, \"message\" : \"username or password are not given\"}"
                }
            } else {
                "{ \"status\" : 401, \"message\" : \"unauthorized\"}"
            }
        } catch (e : Exception) {
            e.printStackTrace()
            "{ \"status\" : 400 , \"message\" : \"Error by processing the request: ${e.message}\"}"
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