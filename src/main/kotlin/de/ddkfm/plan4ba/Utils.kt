package de.ddkfm.plan4ba

fun getEnvOrDefault(key : String, default : String) : String {
    return System.getenv(key) ?: default
}