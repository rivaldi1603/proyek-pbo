package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthContextTests {

    @Test
    @DisplayName("Membuat instance kelas AuthContext dengan benar")
    void testAuthContextMethods() {
        AuthContext authContext = new AuthContext();

        // Awalnya null
        assertNull(authContext.getAuthUser());
        assertFalse(authContext.isAuthenticated());

        // Set user
        User user = new User("Abdullah Ubaid", "test@example.com", "123456");
        authContext.setAuthUser(user);

        assertEquals(user, authContext.getAuthUser());
        assertTrue(authContext.isAuthenticated());

        // Set null lagi
        authContext.setAuthUser(null);
        assertNull(authContext.getAuthUser());
        assertFalse(authContext.isAuthenticated());
    }
}
