package rubbles.monitoring.commcoverage.common.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import rubbles.monitoring.commcoverage.common.core.Message;
import rubbles.monitoring.commcoverage.common.core.Processor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class QueryProcessor implements Processor<Message, Message> {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String sql;
    private final boolean required;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Message> apply(Message message) {
        Map<String, Object> rawMap = objectMapper.convertValue(message, new TypeReference<Map<String, Object>>() {
        });
        log.trace("selecting with object {}", rawMap);
        List<Map<String, Object>> loadedData = jdbcTemplate.queryForList(sql, rawMap);
        List<Message> result = loadedData.stream()
                .map(map -> {
                    Message row = new Message(message.getId());
                    row.setData(map);
                    return row;
                })
                .collect(Collectors.toList());
        log.debug("retrieved new data from db: {}", result);

        if (result.size() == 0 && !required) {
            return Collections.singletonList(new Message(message.getId()));
        }
        return result;
    }
}
