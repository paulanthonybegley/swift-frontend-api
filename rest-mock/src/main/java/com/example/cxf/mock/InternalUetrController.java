package com.example.cxf.mock;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/internal")
public class InternalUetrController {
    private final TransactionStateStore store;

    public InternalUetrController(TransactionStateStore store) {
        this.store = store;
    }

    @GET
    @Path("/active-uetrs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getActiveUetrs() {
        return store.getActiveUetrs();
    }
}
