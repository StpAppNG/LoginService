package de.ddkfm.stpapp

import org.json.JSONObject
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import spark.kotlin.port
import spark.kotlin.post
import java.net.URL
import java.util.regex.Pattern


fun main(args : Array<String>) {
    port(8080)

    post("/login") {
        response.type("application/json")
        try {
            var jsonObject = JSONObject(request.body())
            if (jsonObject.has("username") && jsonObject.has("password")) {
                var hash = login(username = jsonObject.getString("username"),
                        password = jsonObject.getString("password"))
                if (!hash.isEmpty()) {
                    "{ \"hash\" : \"$hash\"}"
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


fun login(username: String, password: String): String {
    var hash = ""
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

        val pattern = Pattern.compile("[0-9a-f]{32}")
        val matcher = pattern.matcher(content)

        while (matcher.find()) {
            hash = matcher.group()
            break
        }
        driver.quit()
    } catch (e : Exception) {
        e.printStackTrace()
    }
    return hash
}