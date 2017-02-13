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

import java.util.Objects;
import javax.annotation.Nonnull;
import com.google.common.primitives.Ints;
import com.smoketurner.graphiak.core.GraphiteMetricDecoder;
import com.smoketurner.graphiak.store.MetricStore;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public final class GraphiakInitializer
        extends ChannelInitializer<SocketChannel> {

    private static final int READER_IDLE_SECONDS = 60;
    private final AuthHandler authHandler = new AuthHandler();
    private final MetricStore store;
    private final int maxLength;

    /**
     * Constructor
     *
     * @param store
     *            Metric store
     * @param maxLength
     *            maximum metric line length
     */
    public GraphiakInitializer(@Nonnull final MetricStore store,
            final long maxLength) {
        this.store = Objects.requireNonNull(store);
        this.maxLength = Ints.checkedCast(maxLength);
    }

    @Override
    public void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline p = ch.pipeline();

        // removes idle connections after READER_IDLE_SECONDS seconds
        p.addLast("idleStateHandler",
                new IdleStateHandler(READER_IDLE_SECONDS, 0, 0));

        // handle new connections and idle timeouts
        p.addLast("auth", authHandler);

        // break each data chunk by newlines and split out metrics
        p.addLast("line", new GraphiteMetricDecoder(maxLength));

        // batch up metrics and store
        p.addLast("metrics", new MetricHandler(store));
    }
}
