package com.lian.shop

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class LianShopApplication

fun main(args: Array<String>) {
    // Load .env file
    val dotenv = dotenv { ignoreIfMissing = true }

    // Set environment variables from .env
    dotenv.entries().forEach { entry -> System.setProperty(entry.key, entry.value) }

    runApplication<LianShopApplication>(*args)
}
