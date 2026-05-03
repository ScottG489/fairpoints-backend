package debatable;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import debatable.core.DynamoDbChannelDeterminer;
import debatable.health.VersionCheck;
import debatable.resources.ChannelResource;
import debatable.resources.TokenResource;
import debatable.resources.filter.EveryResponseFilter;
import debatable.twilio.TwilioConversationService;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DebatableBackendApplication extends Application<DebatableBackendConfiguration> {
    public static void main(String[] args) throws Exception {
        new DebatableBackendApplication().run(args);
    }

    @Override
    public String getName() {
        return "debatable-backend";
    }

    @Override
    public void initialize(Bootstrap<DebatableBackendConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor())
        );
    }

    @Override
    public void run(DebatableBackendConfiguration configuration, Environment environment) {
        environment.jersey().register(new EveryResponseFilter());

        environment.jersey().register(new TokenResource(
                configuration.getTwilioAccountSid(),
                configuration.getTwilioApiKey(),
                configuration.getTwilioApiSecret(),
                configuration.getTwilioChatServiceSid()));

        TwilioConversationService twilioConversationService = new TwilioConversationService(
                configuration.getTwilioAccountSid(),
                configuration.getTwilioApiKey(),
                configuration.getTwilioApiSecret(),
                configuration.getTwilioChatServiceSid());

//        environment.jersey().register(new ChannelResource(new ChannelDeterminer(), getInMemoryChannelsStore(), twilioConversationService));
        environment.jersey().register(
                new ChannelResource(
                        new DynamoDbChannelDeterminer(),
                        getChannelsTable(configuration.getAws()),
                        twilioConversationService));

        environment.healthChecks().register("version", new VersionCheck());
    }

    private Table getChannelsTable(DebatableBackendConfiguration.AwsConfiguration awsConfig) {
        BasicAWSCredentials awsCreds =
                new BasicAWSCredentials(awsConfig.getAccessKeyId(), awsConfig.getSecretAccessKey());
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(awsConfig.getRegion())
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
        DynamoDB db = new DynamoDB(client);

        return db.getTable(awsConfig.getDynamoDbTable());
    }

    private Map<String, Map<String, LinkedList<String>>> getInMemoryChannelsStore() {
        return new HashMap<>();
    }
}
