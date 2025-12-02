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
        // prepareThreshold=0으로 설정하여 prepared statement 비활성화
        val originalUrl = hikariDataSource.jdbcUrl
        
        val urlWithParams = if (originalUrl.contains("?")) {
            // 이미 파라미터가 있는 경우
            if (!originalUrl.contains("prepareThreshold")) {
                "$originalUrl&prepareThreshold=0"
            } else {
                originalUrl.replace("prepareThreshold=\\d+".toRegex(), "prepareThreshold=0")
            }
        } else {
            // 파라미터가 없는 경우
            "$originalUrl?prepareThreshold=0"
        }
        
        hikariDataSource.jdbcUrl = urlWithParams
        
        // Spring 트랜잭션 관리와 호환되도록 autoCommit 비활성화
        hikariDataSource.isAutoCommit = false
        
        return hikariDataSource
    }
}

