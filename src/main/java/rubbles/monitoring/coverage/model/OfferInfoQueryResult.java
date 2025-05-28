package rubbles.monitoring.coverage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OfferInfoQueryResult {
    private String metricName;
    private Long gzSms;
    private Long gzEmail;
    private Long aptekaSms;
    private Long aptekaEmail;
    private Long kfSms;
    private Long kfEmail;
}
