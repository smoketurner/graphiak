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
package com.smoketurner.graphiak.store;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.timeseries.Query;
import com.basho.riak.client.api.commands.timeseries.Store;
import com.basho.riak.client.core.netty.RiakResponseException;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.smoketurner.graphiak.core.GraphiteMetric;
import com.smoketurner.graphiak.core.GraphiteMetricRowConverter;
import com.smoketurner.graphiak.exceptions.MetricStoreException;

public class MetricStore {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MetricStore.class);
    private static final String TABLE_NAME = "metrics";

    private final GraphiteMetricRowConverter converter = new GraphiteMetricRowConverter();
    private final RiakClient client;

    // timers
    private final Timer fetchTimer;
    private final Timer storeTimer;
    private final Timer deleteTimer;

    /**
     * Constructor
     *
     * @param client
     *            Riak client
     */
    public MetricStore(@Nonnull final RiakClient client) {
        final MetricRegistry registry = SharedMetricRegistries
                .getOrCreate("default");
        this.fetchTimer = registry
                .timer(MetricRegistry.name(MetricStore.class, "fetch"));
        this.storeTimer = registry
                .timer(MetricRegistry.name(MetricStore.class, "store"));
        this.deleteTimer = registry
                .timer(MetricRegistry.name(MetricStore.class, "delete"));

        this.client = Objects.requireNonNull(client);
    }

    /**
     * Internal method to create the table
     */
    public void initialize() throws MetricStoreException {
        final String queryText = String.format(
                "CREATE TABLE %s (path VARCHAR NOT NULL,"
                        + "time TIMESTAMP NOT NULL, value DOUBLE,"
                        + "PRIMARY KEY ((path, QUANTUM(time, 15, 'm')), path, time DESC))",
                TABLE_NAME);

        LOGGER.debug("Creating Riak TS table: {}", TABLE_NAME);

        final Query query = new Query.Builder(queryText).build();

        try {
            client.execute(query);
        } catch (InterruptedException e) {
            LOGGER.warn("Create table was interrrupted", e);
            Thread.currentThread().interrupt();
            throw new MetricStoreException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RiakResponseException) {
                final String message = e.getCause().getMessage();
                if (message.endsWith("already_active")) {
                    LOGGER.debug("Table \"{}\" already exists, skipping",
                            TABLE_NAME);
                    return;
                }
            }
            LOGGER.error("Unable to create table", e);
            throw new MetricStoreException(e);
        }
    }

    public void store(@Nullable final List<GraphiteMetric> metrics)
            throws MetricStoreException {
        if (metrics == null || metrics.isEmpty()) {
            return;
        }

        Collections.sort(metrics);
        final Store store = new Store.Builder(TABLE_NAME)
                .withRows(converter.convertAll(metrics)).build();

        LOGGER.debug("Storing {} metrics (async)", metrics.size());

        try (Timer.Context context = storeTimer.time()) {
            client.executeAsync(store);
        }
    }
}
