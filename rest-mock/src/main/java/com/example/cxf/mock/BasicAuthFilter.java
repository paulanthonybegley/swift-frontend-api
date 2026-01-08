package com.example.cxf.mock;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Base64;

@Provider
public class BasicAuthFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"Tracker Realm\"")
                    .build());
            return;
        }

        try {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            final String[] values = credentials.split(":", 2);

            if (values.length != 2 || !"admin".equals(values[0]) || !"password".equals(values[1])) {
                abort(requestContext);
            }
        } catch (Exception e) {
            abort(requestContext);
        }
    }

    private void abort(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header("WWW-Authenticate", "Basic realm=\"Tracker Realm\"")
                .build());
    }
}
