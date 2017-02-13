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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.timeseries.Query;
import com.basho.riak.client.api.commands.timeseries.Store;
import com.basho.riak.client.core.netty.RiakResponseException;
import com.basho.riak.client.core.query.timeseries.QueryResult;
import com.basho.riak.client.core.query.timeseries.Row;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.smoketurner.graphiak.core.GraphiteMetric;
import com.smoketurner.graphiak.core.GraphiteMetricRowConverter;
import com.smoketurner.graphiak.exceptions.MetricStoreException;
import io.dropwizard.util.Duration;

public class MetricStore {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MetricStore.class);
    private static final String TABLE_NAME = "metrics";
    private static final long NANOS_IN_MILLIS = Duration.nanoseconds(1)
            .toMilliseconds();

    private final GraphiteMetricRowConverter converter = new GraphiteMetricRowConverter();
    private final RiakClient client;

    // timers
    private final Timer queryTimer;
    private final Timer storeTimer;
    private final Timer deleteTimer;

    /**
     * Constructor
     *
     * @param client
     *            Riak client
     */
    public MetricStore(@Nonnull final RiakClient client) {
        this.client = Objects.requireNonNull(client);

        final MetricRegistry registry = SharedMetricRegistries
                .getOrCreate("default");
        this.queryTimer = registry
                .timer(MetricRegistry.name(MetricStore.class, "query"));
        this.storeTimer = registry
                .timer(MetricRegistry.name(MetricStore.class, "store"));
        this.deleteTimer = registry
                .timer(MetricRegistry.name(MetricStore.class, "delete"));
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

    /**
     * Query a single metric path between a given time range
     *
     * @param path
     *            Metric path
     * @param startTime
     *            Start time
     * @param endTime
     *            End time
     * @return List of metrics
     * @throws MetricStoreException
     *             if unable to query
     */
    public List<GraphiteMetric> fetch(@Nonnull final String path,
            @Nonnull final DateTime startTime, @Nonnull final DateTime endTime)
            throws MetricStoreException {

        Objects.requireNonNull(path);
        Objects.requireNonNull(startTime);
        Objects.requireNonNull(endTime);

        if (path.isEmpty()) {
            throw new MetricStoreException("Path not provided");
        }
        if (endTime.getMillis() <= startTime.getMillis()) {
            throw new MetricStoreException(
                    "endTime must be greater than startTime");
        }

        final String queryText = String.format(
                "SELECT path, time, value FROM %s WHERE time >= %d AND time < %d AND path = '%s'",
                TABLE_NAME, startTime.getMillis(), endTime.getMillis(), path);

        final Query query = new Query.Builder(queryText).build();

        final QueryResult result;
        try (Timer.Context context = queryTimer.time()) {
            result = client.execute(query);

            final long duration = context.stop();
            LOGGER.debug("Query returned {} rows in {}ms",
                    result.getRowsCount(), duration / NANOS_IN_MILLIS);

        } catch (ExecutionException e) {
            LOGGER.error("Unable to execute query: " + queryText, e);
            throw new MetricStoreException(e);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted executing query: " + queryText, e);
            Thread.currentThread().interrupt();
            throw new MetricStoreException(e);
        }

        final List<GraphiteMetric> metrics = result.getRowsCopy()
                .parallelStream().map(row -> converter.reverse().convert(row))
                .sorted().collect(Collectors.toList());
        return metrics;
    }

    /**
     * Asynchronously stores one or more metrics in Riak TS
     *
     * @param metrics
     *            list of metrics to store
     */
    public void store(@Nullable final List<GraphiteMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return;
        }

        final List<Row> rows = metrics.stream().sorted().map(converter::convert)
                .collect(Collectors.toList());

        final Store store = new Store.Builder(TABLE_NAME).withRows(rows)
                .build();

        LOGGER.debug("Storing {} metrics (async)", metrics.size());

        try (Timer.Context context = storeTimer.time()) {
            client.executeAsync(store);
        }
    }
}
