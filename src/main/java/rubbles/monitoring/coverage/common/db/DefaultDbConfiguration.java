package rubbles.monitoring.coverage.common.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Configuration
public class DefaultDbConfiguration {
    /*@Bean
    @ConditionalOnMissingBean(DbService.class)
    @Qualifier("cdmDbService")
    public DbService cdmService(DataSource dataSource,
                                      ObjectMapper objectMapper) {
        return new DbService(dataSource, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(DbService.class)
    @Qualifier("intDbService")
    public DbService intService(DataSource dataSource,
                                      ObjectMapper objectMapper) {
        return new DbService(dataSource, objectMapper);
    }*/

    @Bean("DbObjectMapper")
    public ObjectMapper objectMapper(@Value("${db.date-format:yyyy-MM-dd HH:mm:ss.SSS}") String dateFormat,
                                     @Value("${db.date-timezone:Europe/Moscow}") String timezone) {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(TimeZone.getTimeZone(timezone));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(format);
        return objectMapper;
    }
}
