package com.example.cxf.service;

import com.example.cxf.api.UsersApi;
import com.example.cxf.model.UserProfile;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import java.util.Collections;

public class UserService {
    private final UsersApi proxy;

    public UserService(String baseUrl) {
        this.proxy = JAXRSClientFactory.create(baseUrl, UsersApi.class,
                Collections.singletonList(new JacksonJsonProvider()));
    }

    public UserProfile getUser(String id) {
        try {
            return proxy.getUserById(id);
        } catch (jakarta.ws.rs.NotFoundException e) {
            return null;
        } catch (Exception e) {
            // Log error
            System.err.println("Error calling service: " + e.getMessage());
            throw e;
        }
    }
}
