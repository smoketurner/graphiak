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
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import org.junit.Test;

public class GraphiteMetricDecoderTest {

    @Test
    public void testDecode() {
        final GraphiteMetric expected = new GraphiteMetric("test", 4.0,
                1486870573L);

        final GraphiteMetric actual = GraphiteMetricDecoder
                .decode("test 4 1486870573");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testDecodeNull() {
        final GraphiteMetric actual = GraphiteMetricDecoder.decode(null);
        assertThat(actual).isNull();
    }

    @Test
    public void testDecodeEmpty() {
        final GraphiteMetric actual = GraphiteMetricDecoder.decode("");
        assertThat(actual).isNull();
    }

    @Test
    public void testDecodeMissingValueAndTimestamp() {
        final GraphiteMetric actual = GraphiteMetricDecoder.decode("test");
        assertThat(actual).isNull();
    }

    @Test
    public void testDecodeMissingTimestamp() {
        final GraphiteMetric actual = GraphiteMetricDecoder.decode("test 4");
        assertThat(actual).isNull();
    }

    @Test
    public void testDecodeInvalidValue() {
        final GraphiteMetric actual = GraphiteMetricDecoder.decode("test test");
        assertThat(actual).isNull();
    }

    @Test
    public void testDecodeInvalidTimestamp() {
        try {
            GraphiteMetricDecoder.decode("test 4 test");
            failBecauseExceptionWasNotThrown(NumberFormatException.class);
        } catch (NumberFormatException e) {
        }
    }

    @Test
    public void testDecodeInvalidValueAndTimestamp() {
        try {
            GraphiteMetricDecoder.decode("test test test");
            failBecauseExceptionWasNotThrown(NumberFormatException.class);
        } catch (NumberFormatException e) {
        }
    }

    @Test
    public void testDecodeExtraWhitespace() {
        final GraphiteMetric expected = new GraphiteMetric("test", 4.0,
                1486870573L);

        final GraphiteMetric actual = GraphiteMetricDecoder
                .decode("    test     4     1486870573     ");
        assertThat(actual).isEqualTo(expected);
    }
}
