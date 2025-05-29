package rubbles.monitoring.coverage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CascadeCountQueryResult {
    private String messageDesc;
    private Long gzCount;
    private Long aptekaCount;
}
