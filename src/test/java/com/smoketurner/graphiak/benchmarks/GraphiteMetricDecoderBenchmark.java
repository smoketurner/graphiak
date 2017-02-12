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
package com.smoketurner.graphiak.benchmarks;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import com.smoketurner.graphiak.core.GraphiteMetric;
import com.smoketurner.graphiak.core.GraphiteMetricDecoder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class GraphiteMetricDecoderBenchmark {

    @Benchmark
    public GraphiteMetric testDecode() {
        return GraphiteMetricDecoder.decode("test 4 1486870573");
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder()
                .include(GraphiteMetricDecoderBenchmark.class.getSimpleName())
                .forks(1).warmupIterations(5).measurementIterations(5).build())
                        .run();
    }
}
