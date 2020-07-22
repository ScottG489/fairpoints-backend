package debatable;

import debatable.health.VersionCheck;
import debatable.resources.BuildResource;
import debatable.resources.filter.EveryResponseFilter;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
    }

    @Override
    public void run(DebatableBackendConfiguration configuration, Environment environment) {
        environment.jersey().register(new EveryResponseFilter());

        environment.jersey().register(new BuildResource());

        environment.healthChecks().register("version", new VersionCheck());
    }
}
