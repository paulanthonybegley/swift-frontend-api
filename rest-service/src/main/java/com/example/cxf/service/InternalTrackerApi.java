package com.example.cxf.service;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/internal")
public interface InternalTrackerApi {
    @GET
    @Path("/active-uetrs")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getActiveUetrs();
}
