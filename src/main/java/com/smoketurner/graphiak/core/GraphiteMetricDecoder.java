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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.ByteProcessor;

public class GraphiteMetricDecoder extends LineBasedFrameDecoder {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GraphiteMetricDecoder.class);
    private static final byte SPACE = (byte) ' ';

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

        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("decode: {}", frame.toString(StandardCharsets.UTF_8));
        }

        try {
            return decode(frame);
        } catch (Exception e) {
            LOGGER.warn(String.format("Unable to decode frame: %s",
                    frame.toString(StandardCharsets.UTF_8)), e);
        }
        return null;
    }

    @Nullable
    private static GraphiteMetric decode(@Nonnull final ByteBuf buf) {
        final int firstSpace = buf.indexOf(buf.readerIndex(), buf.writerIndex(),
                SPACE);
        if (firstSpace == -1) {
            return null;
        }

        final int valueStart = buf.forEachByte(firstSpace,
                buf.writerIndex() - firstSpace,
                ByteProcessor.FIND_NON_LINEAR_WHITESPACE);
        if (valueStart == -1) {
            return null;
        }

        final int secondSpace = buf.indexOf(valueStart, buf.writerIndex(),
                SPACE);
        if (secondSpace == -1) {
            return null;
        }

        final String path = trim(buf.slice(buf.readerIndex(), firstSpace));

        final double value = Double.parseDouble(
                trim(buf.slice(valueStart, secondSpace - valueStart - 1)));

        final long timestamp = Long.parseUnsignedLong(trim(buf
                .slice(secondSpace + 1, buf.writerIndex() - secondSpace - 1)));

        return new GraphiteMetric(path, value, timestamp);
    }

    private static String trim(@Nonnull final ByteBuf buf) {
        return buf.toString(StandardCharsets.UTF_8).trim();
    }
}
