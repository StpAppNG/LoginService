package de.ddkfm.plan4ba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun getEnvOrDefault(key : String, default : String) : String {
    return System.getenv(key) ?: default
}

val objectMapper = jacksonObjectMapper()

fun Any.toJson() : String {
    return objectMapper.writeValueAsString(this)
}