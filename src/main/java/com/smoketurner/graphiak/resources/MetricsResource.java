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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.common.collect.ImmutableMap;
import com.smoketurner.graphiak.store.MetricStore;
import io.dropwizard.jersey.params.IntParam;
import jersey.repackaged.com.google.common.collect.Lists;

@Path("/metrics")
public class MetricsResource {

    private static final List<String> VALID_FORMATS = Lists
            .newArrayList("treejson", "completer", "nodelist");
    private final MetricStore store;

    /**
     * Constructor
     *
     * @param store
     *            Metric store
     */
    public MetricsResource(@Nonnull final MetricStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response root(
            @QueryParam("wildcards") @DefaultValue("0") IntParam wildcards,
            @QueryParam("from") @DefaultValue("-1") IntParam from,
            @QueryParam("until") @DefaultValue("-1") IntParam until,
            @QueryParam("position") @DefaultValue("-1") IntParam position,
            @QueryParam("format") @DefaultValue("treejson") String format,
            @QueryParam("query") String query) {
        return find(wildcards, from, until, position, format, query);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response rootPost(
            @QueryParam("wildcards") @DefaultValue("0") IntParam wildcards,
            @QueryParam("from") @DefaultValue("-1") IntParam from,
            @QueryParam("until") @DefaultValue("-1") IntParam until,
            @QueryParam("position") @DefaultValue("-1") IntParam position,
            @QueryParam("format") @DefaultValue("treejson") String format,
            @QueryParam("query") String query) {
        return find(wildcards, from, until, position, format, query);
    }

    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(
            @QueryParam("wildcards") @DefaultValue("0") IntParam wildcardsParam,
            @QueryParam("from") @DefaultValue("-1") IntParam fromParam,
            @QueryParam("until") @DefaultValue("-1") IntParam untilParam,
            @QueryParam("position") @DefaultValue("-1") IntParam positionParam,
            @QueryParam("format") @DefaultValue("treejson") String format,
            @QueryParam("query") String query) {

        final boolean wildcards = (wildcardsParam.get().equals(1));
        final int fromTime = fromParam.get();
        final int untilTime = untilParam.get();
        final int nodePosition = positionParam.get();

        final Map<String, String> errors = new HashMap<>();
        if (!VALID_FORMATS.contains(format)) {
            errors.put("format",
                    String.format("unrecognized format: \"%s\".", format));
        }
        if (query == null || query.isEmpty()) {
            errors.put("query", "this parameter is required.");
        }

        if (!errors.isEmpty()) {
            final Response response = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ImmutableMap.of("errors", errors))
                    .type(MediaType.APPLICATION_JSON).build();
            throw new WebApplicationException(response);
        }

        return Response.noContent().build();
    }

    @POST
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findPost(
            @QueryParam("wildcards") @DefaultValue("0") IntParam wildcards,
            @QueryParam("from") @DefaultValue("-1") IntParam from,
            @QueryParam("until") @DefaultValue("-1") IntParam until,
            @QueryParam("position") @DefaultValue("-1") IntParam position,
            @QueryParam("format") @DefaultValue("treejson") String format,
            @QueryParam("query") String query) {
        return find(wildcards, from, until, position, format, query);
    }

    @GET
    @Path("/expand")
    public Response expand() {
        return Response.noContent().build();
    }

    @GET
    @Path("/index.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response index() {
        return Response.noContent().build();
    }
}
