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

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class GraphiteMetricDecoderTest {

    @Test
    public void testDecode() {
        final GraphiteMetric expected = new GraphiteMetric("test", 4,
                1486870573L);

        final ByteBuf buf = Unpooled.buffer();
        buf.writeCharSequence("test 4 1486870573", StandardCharsets.UTF_8);

        final EmbeddedChannel channel = new EmbeddedChannel(
                new GraphiteMetricDecoder(100));
        assertThat(channel.writeInbound(buf)).isTrue();
        assertThat(channel.finish()).isTrue();

        final GraphiteMetric actual = channel.readInbound();
        assertThat(actual).isEqualTo(expected);
    }
}
