package rubbles.monitoring.coverage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CommunicationCoverageQueryResult {
    private String brand;
    private String type;
    private String channel;
    private Long clientCount;
    private Long uniqueClientCount;
    private BigDecimal communicationsPerClient;
    private String coverage;
}
