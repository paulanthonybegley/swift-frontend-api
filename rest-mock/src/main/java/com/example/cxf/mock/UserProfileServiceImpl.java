package com.example.cxf.mock;

import com.example.cxf.api.UsersApi; // generated interface
import com.example.cxf.model.UserProfile; // generated POJO
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class UserProfileServiceImpl implements UsersApi {

    private final Map<String, UserProfile> userDb = new HashMap<>();

    public UserProfileServiceImpl() {
        // Seed data
        UserProfile user = new UserProfile();
        user.setId("123");
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setBio("Hello world");
        userDb.put("123", user);
    }

    @Override
    public UserProfile getUserById(String userId) {
        if ("error".equals(userId)) {
            throw new RuntimeException("Simulated 500 Error");
        }
        if ("unknown".equals(userId) || !userDb.containsKey(userId)) {
            // In CXF JAX-RS, returning null often results in 204 or can be mapped to 404
            // via ExceptionMapper.
            // But the interface return type is UserProfile.
            // To return 404, we usually throw NotFoundException (JAX-RS) or
            // WebApplicationException.
            throw new jakarta.ws.rs.NotFoundException("User not found");
        }
        return userDb.get(userId);
    }

    @Override
    public UserProfile updateUser(String userId, UserProfile userProfile) {
        // In a real app we might check if userId matches userProfile.getId()
        userDb.put(userId, userProfile);
        return userProfile;
    }
}
