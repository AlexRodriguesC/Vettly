package com.vettly.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import com.vettly.TestcontainersConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration.class)
class AuthFlowIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mvc;

    @Test
    void registerReturnsTokenThatAuthenticatesTheUser() throws Exception {
        String email = uniqueEmail();

        String body = register(email, PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.accessToken");

        mvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void registeringTheSameEmailTwiceIsRejected() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD).andExpect(status().isCreated());

        // Case and surrounding spaces don't make a new account.
        register("  " + email.toUpperCase() + " ", PASSWORD)
                .andExpect(status().isConflict());
    }

    @Test
    void loginSucceedsOnlyWithTheRightPassword() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD).andExpect(status().isCreated());

        login(email, "wrong-password").andExpect(status().isUnauthorized());
        login("nobody-" + email, PASSWORD).andExpect(status().isUnauthorized());

        login(email, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void loginIgnoresCaseAndSurroundingSpacesInTheEmail() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD).andExpect(status().isCreated());

        login("  " + email.toUpperCase() + " ", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void invalidRegistrationIsRejectedWithFieldErrors() throws Exception {
        register("not-an-email", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void protectedEndpointsRequireAValidToken() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        String body = register(uniqueEmail(), PASSWORD).andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.accessToken");
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        mvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions register(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(email, password)));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(email, password)));
    }

    private static String credentials(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
