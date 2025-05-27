package rubbles.monitoring.commcoverage.common.core;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class RawMessage {
    @JsonAnySetter
    private Map<String, Object> data = new LinkedHashMap<>();

    public RawMessage(Map<String, Object> data) {
        this.data = data != null ? new LinkedHashMap<>(data) : null;
    }

    public RawMessage(RawMessage message) {
        this.data = new LinkedHashMap<>(message.data);
    }

    @JsonIgnore
    public Map<String, Object> getData() {
        return data;
    }

    @JsonAnyGetter
    private Map<String, Object> getAny() {
        return Collections.unmodifiableMap(data);
    }
}
