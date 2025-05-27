package rubbles.monitoring.commcoverage.common.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface Processor<T extends Message, R extends Message> {
    List<R> apply(T message);

    default List<R> enrich(T message) {
        List<R> result = apply(message);
        for (R item : result) {
            Map<String, Object> data = new LinkedHashMap<>(message.getData());
            data.putAll(item.getData());
            item.setData(data);
        }
        return result;
    }
}
