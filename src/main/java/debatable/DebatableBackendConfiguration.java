package debatable;

import io.dropwizard.Configuration;
import lombok.Data;

@Data
public class DebatableBackendConfiguration extends Configuration {
    private String twilioAccountSid;
    private String twilioApiKey;
    private String twilioApiSecret;
    private String twilioChatServiceSid;
    private AwsConfiguration aws;

    @Data
    public static class AwsConfiguration {
        private String region;
        private String accessKeyId;
        private String secretAccessKey;
        private String dynamoDbTable;
    }
}
