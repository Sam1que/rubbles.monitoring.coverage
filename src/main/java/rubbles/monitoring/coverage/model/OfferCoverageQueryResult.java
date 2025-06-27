package rubbles.monitoring.coverage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OfferCoverageQueryResult {
    private String brand;
    private Long offerCount;
    private Long uniqueClientCount;
    private Long clientBase;
    private String coverage;
}
