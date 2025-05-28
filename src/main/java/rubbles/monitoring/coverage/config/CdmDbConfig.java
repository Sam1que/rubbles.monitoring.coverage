package rubbles.monitoring.coverage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import rubbles.monitoring.coverage.common.db.DbService;

import javax.sql.DataSource;

@Slf4j
@Lazy
@Configuration
public class CdmDbConfig {
    static JdbcTemplate jdbcTemplate(DataSource dataSource, JdbcProperties properties) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcProperties.Template template = properties.getTemplate();
        jdbcTemplate.setFetchSize(template.getFetchSize());
        jdbcTemplate.setMaxRows(template.getMaxRows());
        if (template.getQueryTimeout() != null) {
            jdbcTemplate.setQueryTimeout((int) template.getQueryTimeout().getSeconds());
        }
        return jdbcTemplate;
    }

    @Bean
    @ConfigurationProperties("db.postgres.cdm")
    public DataSourceProperties cdmDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "db.postgres.cdm.hikari")
    public DataSource cdmDataSource(@Qualifier("cdmDataSourceProperties") DataSourceProperties cdmDataSourceProperties) {
        try {
//            cdmDataSourceProperties.setPassword(Encryptor.decrypt(cdmDataSourceProperties.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cdmDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean
    public JdbcTemplate cdmJdbcTemplate(@Qualifier("cdmDataSource") DataSource cdmDataSource, JdbcProperties jdbcProperties) {
        return jdbcTemplate(cdmDataSource, jdbcProperties);
    }

    @Bean
    public DbService cdmDbService(@Qualifier("cdmDataSource") DataSource cdmDataSource,
                                       @Qualifier("cdmJdbcTemplate") JdbcTemplate cdmJdbcTemplate, @Qualifier("DbObjectMapper")ObjectMapper objectMapper) {
        return new DbService(cdmDataSource, objectMapper, new NamedParameterJdbcTemplate(cdmJdbcTemplate));
    }
}