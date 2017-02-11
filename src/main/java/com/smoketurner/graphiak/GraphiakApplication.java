/**
 * Copyright 2017 Smoke Turner, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smoketurner.graphiak;

import javax.annotation.Nonnull;
import com.basho.riak.client.api.RiakClient;
import com.smoketurner.dropwizard.riak.RiakBundle;
import com.smoketurner.dropwizard.riak.RiakFactory;
import com.smoketurner.graphiak.config.GraphiakConfiguration;
import com.smoketurner.graphiak.managed.MetricStoreManager;
import com.smoketurner.graphiak.resources.PingResource;
import com.smoketurner.graphiak.resources.VersionResource;
import com.smoketurner.graphiak.store.MetricStore;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class GraphiakApplication extends Application<GraphiakConfiguration> {

    public static void main(final String[] args) throws Exception {
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
        new GraphiakApplication().run(args);
    }

    @Override
    public String getName() {
        return "graphiak";
    }

    @Override
    public void initialize(final Bootstrap<GraphiakConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)));

        bootstrap.addBundle(new RiakBundle<GraphiakConfiguration>() {
            @Override
            public RiakFactory getRiakFactory(
                    GraphiakConfiguration configuration) {
                return configuration.getRiak();
            }
        });
    }

    @Override
    public void run(@Nonnull final GraphiakConfiguration configuration,
            @Nonnull final Environment environment) throws Exception {

        // get Riak client
        final RiakClient client = configuration.getRiak().build();

        final MetricStore store = new MetricStore(client);
        environment.lifecycle().manage(new MetricStoreManager(store));

        // Configure the Netty TCP server
        configuration.getNetty().build(environment, store);

        // Resources
        environment.jersey().register(new PingResource());
        environment.jersey().register(new VersionResource());
    }
}
