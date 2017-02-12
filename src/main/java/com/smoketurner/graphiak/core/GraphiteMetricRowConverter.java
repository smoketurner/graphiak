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

import java.util.List;
import com.basho.riak.client.core.query.timeseries.Cell;
import com.basho.riak.client.core.query.timeseries.Row;
import com.google.common.base.Converter;

public class GraphiteMetricRowConverter extends Converter<GraphiteMetric, Row> {

    @Override
    protected Row doForward(GraphiteMetric metric) {
        return new Row(new Cell(metric.getPath()),
                Cell.newTimestamp(metric.timestamp() * 1000),
                new Cell(metric.getValue()));
    }

    @Override
    protected GraphiteMetric doBackward(Row row) {
        final List<Cell> cells = row.getCellsCopy();
        return new GraphiteMetric(cells.get(0).getVarcharAsUTF8String(),
                cells.get(1).getDouble(), cells.get(2).getTimestamp() / 1000);
    }
}
