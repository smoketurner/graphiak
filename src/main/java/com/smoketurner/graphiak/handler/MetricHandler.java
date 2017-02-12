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
package com.smoketurner.graphiak.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.smoketurner.graphiak.core.GraphiteMetric;
import com.smoketurner.graphiak.store.MetricStore;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public final class MetricHandler
        extends SimpleChannelInboundHandler<GraphiteMetric> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MetricHandler.class);
    private static final int MAX_METRICS = 100;

    private final List<GraphiteMetric> metrics = new ArrayList<>(MAX_METRICS);
    private final MetricStore store;
    private final Meter metricMeter;

    /**
     * Constructor
     *
     * @param store
     *            Metric store
     */
    public MetricHandler(@Nonnull final MetricStore store) {
        this.store = Objects.requireNonNull(store);

        final MetricRegistry registry = SharedMetricRegistries
                .getOrCreate("default");
        this.metricMeter = registry
                .meter(MetricRegistry.name(MetricHandler.class, "metric-rate"));
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, GraphiteMetric metric)
            throws Exception {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("channelRead0: {}", metric);
        }

        metricMeter.mark();

        if (metric == null) {
            LOGGER.trace("received null metric");
            return;
        }

        metrics.add(metric);

        if (metrics.size() >= MAX_METRICS) {
            LOGGER.debug("Storing {} metrics", metrics.size());
            store.store(metrics);
            metrics.clear();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.trace("channelInactive");

        if (!metrics.isEmpty()) {
            LOGGER.debug("Storing remaining {} metrics", metrics.size());
            store.store(metrics);
            metrics.clear();
        }
    }
}
