package rubbles.monitoring.coverage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OfferCoverageQueryResult {
    private String metricName;
    private Long gzCount;
    private Long aptekaCount;
}
