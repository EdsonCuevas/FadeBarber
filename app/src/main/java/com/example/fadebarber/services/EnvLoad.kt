package com.example.fadebarber.services

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv

fun EnvLoad(): Dotenv {
    val dotenv = dotenv {
        directory = "./"   // ruta al archivo .env
        filename = ".env"  // nombre del archivo
    }
    return dotenv
}
