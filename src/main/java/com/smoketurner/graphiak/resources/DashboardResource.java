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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.JSONP;

@Path("/dashboard")
public class DashboardResource {

    private static final String TEXT_JAVASCRIPT = "text/javascript";

    @GET
    @JSONP
    @Path("/find")
    @Produces({ MediaType.APPLICATION_JSON, TEXT_JAVASCRIPT })
    public Response find() {
        final Map<String, List<String>> response = new HashMap<>();
        response.put("dashboards", Collections.emptyList());
        return Response.ok(response).build();
    }

    @POST
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findPost() {
        return find();
    }

    @GET
    @JSONP
    @Path("/load/{name}")
    @Produces({ MediaType.APPLICATION_JSON, TEXT_JAVASCRIPT })
    public Response load(@PathParam("name") String name) {
        final Map<String, String> response = new HashMap<>();
        response.put("error",
                String.format("Dashboard '%s' does not exist", name));
        return Response.status(Response.Status.NOT_FOUND).entity(response)
                .build();
    }

    @POST
    @Path("/load/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadPost(@PathParam("name") String name) {
        return load(name);
    }
}
