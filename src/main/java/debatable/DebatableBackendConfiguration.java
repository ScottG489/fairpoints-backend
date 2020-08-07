package debatable;

import io.dropwizard.Configuration;
import lombok.Data;

@Data
public class DebatableBackendConfiguration extends Configuration {
    private String twilioAccountSid;
    private String twilioApiKey;
    private String twilioApiSecret;
    private String twilioChatServiceSid;
    private String dynamodbTable;
}
