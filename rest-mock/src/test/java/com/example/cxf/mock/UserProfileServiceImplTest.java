package com.example.cxf.mock;

import com.example.cxf.model.UserProfile;
import jakarta.ws.rs.NotFoundException;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserProfileServiceImplTest {

    private final UserProfileServiceImpl service = new UserProfileServiceImpl();

    @Test
    public void testGetUserById_Success() {
        UserProfile user = service.getUserById("123");
        assertNotNull(user);
        assertEquals("john_doe", user.getUsername());
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserById_NotFound() {
        service.getUserById("unknown");
    }

    @Test(expected = RuntimeException.class)
    public void testGetUserById_Error() {
        service.getUserById("error");
    }
}
