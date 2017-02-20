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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.junit.ClassRule;
import org.junit.Test;
import io.dropwizard.testing.junit.ResourceTestRule;

public class DashboardResourceTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new DashboardResource()).build();

    @Test
    public void testGetFind() throws Exception {
        final Response response = resources.client().target("/dashboard/find")
                .request().get();
        final Map<String, List<String>> actual = response
                .readEntity(new GenericType<Map<String, List<String>>>() {
                });

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual.get("dashboards")).isEqualTo(Collections.emptyList());
    }

    @Test
    public void testGetFindJSONP() throws Exception {
        final Response response = resources.client().target("/dashboard/find")
                .request().accept("text/javascript").get();
        final String actual = response.readEntity(String.class);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual).isEqualTo("callback({\"dashboards\":[]})");
    }

    @Test
    public void testPostFind() throws Exception {
        final Response response = resources.client().target("/dashboard/find")
                .request().post(Entity.json(String.class));
        final Map<String, List<String>> actual = response
                .readEntity(new GenericType<Map<String, List<String>>>() {
                });

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual.get("dashboards")).isEqualTo(Collections.emptyList());
    }

    @Test
    public void testGetLoad() throws Exception {
        final Response response = resources.client()
                .target("/dashboard/load/test").request().get();
        final Map<String, String> actual = response
                .readEntity(new GenericType<Map<String, String>>() {
                });

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(actual.get("error"))
                .isEqualTo("Dashboard 'test' does not exist");
    }

    @Test
    public void testGetLoadJSONP() throws Exception {
        final Response response = resources.client()
                .target("/dashboard/load/test").request()
                .accept("text/javascript").get();
        final String actual = response.readEntity(String.class);

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(actual).isEqualTo(
                "callback({\"error\":\"Dashboard 'test' does not exist\"})");
    }

    @Test
    public void testPostLoad() throws Exception {
        final Response response = resources.client()
                .target("/dashboard/load/test").request()
                .post(Entity.json(String.class));
        final Map<String, String> actual = response
                .readEntity(new GenericType<Map<String, String>>() {
                });

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(actual.get("error"))
                .isEqualTo("Dashboard 'test' does not exist");
    }
}
