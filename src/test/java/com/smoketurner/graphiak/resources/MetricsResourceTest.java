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
package com.smoketurner.graphiak.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import java.util.Map;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import com.smoketurner.graphiak.store.MetricStore;
import io.dropwizard.testing.junit.ResourceTestRule;

public class MetricsResourceTest {

    private static final MetricStore store = mock(MetricStore.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new MetricsResource(store)).build();

    @After
    public void tearDown() {
        reset(store);
    }

    @Test
    public void testGetRootNoQuery() {
        final Response response = resources.client().target("/metrics")
                .request().get();
        final Map<String, Map<String, String>> actual = response.readEntity(
                new GenericType<Map<String, Map<String, String>>>() {
                });

        assertThat(response.getStatus()).isEqualTo(400);
        final Map<String, String> errors = actual.get("errors");
        assertThat(errors.get("query"))
                .isEqualTo("this parameter is required.");
    }

    @Test
    public void testGetRootEmptyQuery() {
        final Response response = resources.client().target("/metrics")
                .queryParam("query", "").request().get();
        final Map<String, Map<String, String>> actual = response.readEntity(
                new GenericType<Map<String, Map<String, String>>>() {
                });

        assertThat(response.getStatus()).isEqualTo(400);
        final Map<String, String> errors = actual.get("errors");
        assertThat(errors.get("query"))
                .isEqualTo("this parameter is required.");
    }

    @Test
    public void testGetFindNoQuery() {
        final Response response = resources.client().target("/metrics/find")
                .request().get();
        final Map<String, Map<String, String>> actual = response.readEntity(
                new GenericType<Map<String, Map<String, String>>>() {
                });

        assertThat(response.getStatus()).isEqualTo(400);
        final Map<String, String> errors = actual.get("errors");
        assertThat(errors.get("query"))
                .isEqualTo("this parameter is required.");
    }

    @Test
    public void testGetFindEmptyQuery() {
        final Response response = resources.client().target("/metrics/find")
                .queryParam("query", "").request().get();
        final Map<String, Map<String, String>> actual = response.readEntity(
                new GenericType<Map<String, Map<String, String>>>() {
                });

        assertThat(response.getStatus()).isEqualTo(400);
        final Map<String, String> errors = actual.get("errors");
        assertThat(errors.get("query"))
                .isEqualTo("this parameter is required.");
    }
}
