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

import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class GraphiteMetric {

    private final String path;
    private final double value;
    private final long timestamp;

    /**
     * Constructor
     *
     * @param path
     *            Metric path
     * @param value
     *            Metric value
     * @param timestamp
     *            Metric timestamp in epoch seconds
     */
    public GraphiteMetric(final String path, final double value,
            final long timestamp) {
        this.path = Objects.requireNonNull(path);
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public double getValue() {
        return value;
    }

    public long timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        final GraphiteMetric other = (GraphiteMetric) obj;
        return Objects.equals(path, other.path)
                && Objects.equals(value, other.value)
                && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, value, timestamp);
    }

    @Override
    public String toString() {
        return String.format("%s %f %d", path, value, timestamp);
    }
}
