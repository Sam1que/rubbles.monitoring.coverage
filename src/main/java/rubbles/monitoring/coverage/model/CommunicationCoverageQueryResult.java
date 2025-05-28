package rubbles.monitoring.coverage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommunicationCoverageQueryResult {
    private Long gzSmsCount;
    private Long gzEmailCount;
    private Long aptekaSmsCount;
    private Long aptekaEmailCount;
    private Long kfSmsCount;
    private Long kfEmailCount;
}
