package rubbles.monitoring.coverage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OfferCoverageQueryResult {
    private String brand;
    private Long offerCount;
    private Long uniqueClientsWithOffer;
    private Long offersWithComm;
    private Long uniqueClientsWithOfferAndComm;
    private Long availableBase;
    private String offerCoverage;
    private String offerWithCommCoverage;
}
