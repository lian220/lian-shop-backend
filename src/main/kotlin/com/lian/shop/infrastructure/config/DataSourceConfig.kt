package com.lian.shop.infrastructure.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.net.URI
import javax.sql.DataSource

@Configuration
class DataSourceConfig(
    private val dataSourceProperties: DataSourceProperties
) {

    @Bean
    @Primary
    fun dataSource(): DataSource {
        val hikariDataSource = dataSourceProperties.initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build()

        // PostgreSQL prepared statement 중복 문제 해결
        val originalUrl = hikariDataSource.jdbcUrl ?: ""
        
        // 필요한 파라미터들을 추가
        val params = mutableListOf<String>()
        params.add("prepareThreshold=0")  // prepared statement 캐싱 비활성화
        params.add("preparedStatementCacheQueries=0")  // 쿼리 캐시 비활성화
        params.add("preparedStatementCacheSizeMiB=0")  // 캐시 크기 0
        
        val urlWithParams = if (originalUrl.contains("?")) {
            val baseUrl = originalUrl.substringBefore("?")
            val existingParams = originalUrl.substringAfter("?")
            "$baseUrl?$existingParams&${params.joinToString("&")}"
        } else {
            "$originalUrl?${params.joinToString("&")}"
        }
        
        hikariDataSource.jdbcUrl = urlWithParams
        
        return hikariDataSource
    }
}

