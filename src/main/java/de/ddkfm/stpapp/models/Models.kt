package de.ddkfm.stpapp.models

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