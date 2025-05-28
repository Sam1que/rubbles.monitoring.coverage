package rubbles.monitoring.coverage.common.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
public class DbService {
    private static final int MAX_SQL_LOG_LEN = 500;

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    public final ObjectMapper objectMapper;

    public DbService(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.objectMapper = objectMapper;
        try {
            dataSource.getConnection().close();
        } catch (Exception e) {
            log.error("error initializing pool", e);
        }
    }

    public DbService(DataSource dataSource, ObjectMapper objectMapper,
                     NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = namedParameterJdbcTemplate;
        this.objectMapper = objectMapper;
        try {
            dataSource.getConnection().close();
        } catch (Exception e) {
            log.error("error initializing pool", e);
        }
    }

    private static String truncate(String sql) {
        sql = sql.replace('\n', ' ');
        if (sql.length() > MAX_SQL_LOG_LEN) {
            sql = sql.substring(0, MAX_SQL_LOG_LEN) + "...";
        }
        return sql;
    }

    public List<Map<String, Object>> select(String sql, Map<String, Object> map) {
        return select(sql, new CustomSqlParameterSource(map));
    }

    public List<Map<String, Object>> select(String sql, MapSqlParameterSource parameter) {
        Instant startDttm = Instant.now();
        log.trace("using sql {}", truncate(sql));
        log.trace("using row {}", parameter.getValues());
        List<Map<String, Object>> result;
        try {
            result = jdbcTemplate.queryForList(sql, parameter);
            log.debug("got result {}", result);
        } catch (Exception e) {
            throw new DbException("error selecting from db", e, DbException.SELECT_ERROR);
        } finally {
            log.info("query [{}] took {} ms", truncate(sql),
                    Duration.between(startDttm, Instant.now()).toMillis());
        }
        return result;
    }

    public void simpleBatchInsert(String tableName, List<? extends Map<String, ?>> data, String... generatedColumns) {
        Instant startDttm = Instant.now();
        log.trace("using table {}", tableName);
        log.trace("using row {}", data);
        SimpleJdbcInsert statement = createInsert(tableName, generatedColumns);
        try {
            statement.executeBatch(data.toArray(new Map[0]));
        } catch (Exception e) {
            throw new DbException(e, DbException.INSERT_ERROR);
        } finally {
            log.info("batch query [{}] : {} rows took {} ms", tableName,
                    data.size(), Duration.between(startDttm, Instant.now()).toMillis());
        }
    }

    private SimpleJdbcInsert createInsert(String tableName, String[] generatedColumns) {
        SimpleJdbcInsert statement = new SimpleJdbcInsert(dataSource);
        String[] splitName = tableName.split("\\.");
        switch (splitName.length) {
            case 3:
                statement.withCatalogName(splitName[splitName.length - 3]);
            case 2:
                statement.withSchemaName(splitName[splitName.length - 2]);
            case 1:
                statement.withTableName(splitName[splitName.length - 1]);
                break;
            default:
                throw new DbException("Table name has invalid format: " + tableName, DbException.INVALID_TABLE_NAME);
        }
        return statement.usingGeneratedKeyColumns(generatedColumns);
    }

    public void execute(String sql) {
        log.info("query [{}]", truncate(sql));
        jdbcTemplate.getJdbcOperations().execute(sql);
    }

    public void batchUpdate(String sql, List<Map<String, Object>> data) {
        Instant startDttm = Instant.now();
        log.trace("using sql {}", sql);
        log.trace("using rows {}", data);
        try {
            MapSqlParameterSource[] paramsArray = data.stream()
                    .map(MapSqlParameterSource::new)
                    .toArray(MapSqlParameterSource[]::new);

            jdbcTemplate.batchUpdate(sql, paramsArray);
        } catch (Exception e) {
            throw new DbException(e, DbException.INSERT_ERROR);
        }
        log.info("query [{}] took {} ms", sql, Duration.between(startDttm, Instant.now()).toMillis());
    }

    public void update(String sql, Map<String, Object> map) {
        Instant startDttm = Instant.now();
        log.trace("using sql {}", truncate(sql));
        log.trace("using row {}", map);
        try {
            jdbcTemplate.update(sql, new CustomSqlParameterSource(map));
        } catch (Exception e) {
            throw new DbException(e, DbException.INSERT_ERROR);
        } finally {
            log.info("query [{}] took {} ms", truncate(sql), Duration.between(startDttm, Instant.now()).toMillis());
        }
    }

    private static class CustomSqlParameterSource extends MapSqlParameterSource {
        CustomSqlParameterSource(Map<String, ?> values) {
            super(values);
        }

        @Override
        public boolean hasValue(@NonNull String paramName) {
            return true;
        }
    }
}
