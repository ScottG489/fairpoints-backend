package debatable;

import debatable.core.*;
import debatable.health.VersionCheck;
import debatable.resources.ChatResource;
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
        environment.jersey().register(new ChatResource(getChannelRetriever()));

        environment.healthChecks().register("version", new VersionCheck());
    }

    private RedisChannelRetriever getRedisChannelRetriever() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        RedissonClient redisson = Redisson.create(config);
        Map<Topic, Map<Viewpoint, LinkedList<Channel>>> topics =
                redisson.getMap("topics");
        return new RedisChannelRetriever(topics);
    }

    private InMemoryChannelRetriever getChannelRetriever() {
        Map<Topic, Map<Viewpoint, LinkedList<Channel>>> topicViewpointChannelStore = new HashMap<>();
        return new InMemoryChannelRetriever(topicViewpointChannelStore);
    }
}
