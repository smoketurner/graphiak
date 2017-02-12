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
package com.smoketurner.graphiak.core;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class GraphiteMetricDecoder extends LineBasedFrameDecoder {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GraphiteMetricDecoder.class);

    /**
     * Constructor
     *
     * @param maxLength
     *            Maximum line length
     */
    public GraphiteMetricDecoder(final int maxLength) {
        super(maxLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer)
            throws Exception {
        final ByteBuf frame = (ByteBuf) super.decode(ctx, buffer);
        if (frame == null) {
            return null;
        }

        final String metric = frame.toString(StandardCharsets.UTF_8);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("decode: '{}'", metric);
        }

        try {
            return decode(metric);
        } catch (NumberFormatException e) {
            LOGGER.warn(String.format("Unable to decode metric: %s", metric),
                    e);
        }
        return null;
    }

    @Nullable
    public static GraphiteMetric decode(@Nullable final String metric) {
        if (metric == null || metric.isEmpty()) {
            return null;
        }

        final List<String> parts = Splitter.on(CharMatcher.BREAKING_WHITESPACE)
                .trimResults().omitEmptyStrings().splitToList(metric);
        if (parts.isEmpty() || parts.size() != 3) {
            return null;
        }

        final double value = Double.parseDouble(parts.get(1));
        final long timestamp = Long.parseUnsignedLong(parts.get(2));

        return new GraphiteMetric(parts.get(0), value, timestamp);
    }
}
