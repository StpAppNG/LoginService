package de.ddkfm.plan4ba.models

import com.fasterxml.jackson.annotation.JsonIgnore

data class DatabaseConfig(
        var host : String,
        var port : Int,
        var database : String,
        var username : String,
        var password : String
)

data class Result(
        var forename : String = "",
        var surename : String = "",
        var hash : String = "",
        var group : String = "",
        var course : String ="",
        var university : String = ""
) {
    @JsonIgnore
    fun isValide() : Boolean = !(hash.isEmpty() || group.isEmpty() || university.isEmpty())
}