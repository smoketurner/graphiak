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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.junit.ClassRule;
import org.junit.Test;
import io.dropwizard.testing.junit.ResourceTestRule;

public class EventsResourceTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new EventsResource()).build();

    @Test
    public void testGetData() throws Exception {
        final Response response = resources.client().target("/events/get_data")
                .request().get();
        final List<String> actual = response
                .readEntity(new GenericType<List<String>>() {
                });

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual).isEqualTo(Collections.emptyList());
    }

    @Test
    public void testPostData() throws Exception {
        final Response response = resources.client().target("/events/get_data")
                .request().post(Entity.json(String.class));
        final List<String> actual = response
                .readEntity(new GenericType<List<String>>() {
                });

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(actual).isEqualTo(Collections.emptyList());
    }
}
