package debatable;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import debatable.core.ChannelDeterminer;
import debatable.health.VersionCheck;
import debatable.resources.ChannelResource;
import debatable.resources.TokenResource;
import debatable.resources.filter.EveryResponseFilter;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

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
        environment.jersey().register(new ChannelResource(new ChannelDeterminer(), getInMemoryChannelsStore()));

        environment.healthChecks().register("version", new VersionCheck());
    }

    private Map<String, Map<String, LinkedList<String>>> getInMemoryChannelsStore() {
        return new HashMap<>();
    }

    private Table getChannelsTable() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB db = new DynamoDB(client);

        return db.getTable("Channels");
    }

//    private RedisChannelRetriever getRedisChannelRetriever() {
//        Map<String, Map<String, LinkedList<String>>> topics = getRedisTopics();
//        return new RedisChannelRetriever();
//    }

    private Map<String, Map<String, LinkedList<String>>> getRedisChannelsStore() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson.getMap("topics");
    }
}
