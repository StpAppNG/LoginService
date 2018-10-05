package de.ddkfm.plan4ba.models

data class DatabaseConfig(
        var host : String,
        var port : Int,
        var database : String,
        var username : String,
        var password : String
)
data class Config(
        var database : DatabaseConfig
)

data class Result(
        var forename : String = "",
        var surename : String = "",
        var hash : String = "",
        var group : String = "",
        var course : String ="",
        var university : String = ""
) {
    fun toJson() : String {
        return Result::class.java.declaredFields
                .map{ member ->
                    "\"${member.name}\" : \"${member.get(this)}\""
                }
                .joinToString(separator = ",", prefix = "{", postfix = "}")
    }
    fun isValide() : Boolean = !(hash.isEmpty() || group.isEmpty() || university.isEmpty())
}