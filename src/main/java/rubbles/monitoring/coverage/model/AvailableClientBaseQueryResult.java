package rubbles.monitoring.coverage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableClientBaseQueryResult {
    private String brand;
    private Long totalClientsCount;
    private Long availableClientCount;
    private Long EmailCount;
    private Long SmsCount;
    private Long EmailAndSmsCount;
    private String emailPercentage;
    private String smsPercentage;
    private String emailAndSmsPercentage;
}
