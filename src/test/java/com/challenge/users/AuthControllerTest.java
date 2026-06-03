package com.challenge.users;

import com.challenge.users.dto.AuthRequest;
import com.challenge.users.entity.User;
import com.challenge.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User();
        user.setFirstName("Jean");
        user.setLastName("Dupont");
        user.setBirthDate(LocalDate.of(1990, 5, 15));
        user.setCity("Paris");
        user.setCountry("FR");
        user.setAvatar("https://randomuser.me/api/portraits/men/1.jpg");
        user.setCompany("ACME");
        user.setJobPosition("Developer");
        user.setMobile("0600000000");
        user.setUsername("jean.dupont");
        user.setEmail("jean.dupont@example.com");
        user.setPassword(passwordEncoder.encode("Secret1"));
        user.setRole("user");
        userRepository.save(user);
    }

    @Test
    void authenticate_withUsername_shouldReturnAccessToken() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("jean.dupont");
        req.setPassword("Secret1");

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void authenticate_withEmail_shouldReturnAccessToken() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("jean.dupont@example.com");
        req.setPassword("Secret1");

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void authenticate_withWrongPassword_shouldReturn401() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("jean.dupont");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticate_withUnknownUser_shouldReturn401() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("nobody");
        req.setPassword("pass");

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
