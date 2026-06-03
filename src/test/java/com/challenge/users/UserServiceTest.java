package com.challenge.users;

import com.challenge.users.dto.BatchImportResult;
import com.challenge.users.dto.UserDto;
import com.challenge.users.entity.User;
import com.challenge.users.repository.UserRepository;
import com.challenge.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, objectMapper);
    }

    // ── generateUsers ──────────────────────────────────────────────────────────

    @Test
    void generateUsers_shouldReturnCorrectCount() {
        List<UserDto> users = userService.generateUsers(10);
        assertThat(users).hasSize(10);
    }

    @Test
    void generateUsers_shouldHaveNonBlankMandatoryFields() {
        List<UserDto> users = userService.generateUsers(5);
        for (UserDto u : users) {
            assertThat(u.getFirstName()).isNotBlank();
            assertThat(u.getLastName()).isNotBlank();
            assertThat(u.getUsername()).isNotBlank();
            assertThat(u.getEmail()).contains("@");
            assertThat(u.getCompany()).isNotBlank();
            assertThat(u.getJobPosition()).isNotBlank();
            assertThat(u.getMobile()).isNotBlank();
            assertThat(u.getCity()).isNotBlank();
            assertThat(u.getBirthDate()).isNotNull();
        }
    }

    @Test
    void generateUsers_shouldHaveValidCountryCode() {
        userService.generateUsers(20).forEach(u ->
                assertThat(u.getCountry()).hasSize(2)
        );
    }

    @Test
    void generateUsers_shouldHaveValidAvatarUrl() {
        userService.generateUsers(5).forEach(u ->
                assertThat(u.getAvatar()).startsWith("https://randomuser.me/api/portraits/")
        );
    }

    @Test
    void generateUsers_shouldHavePasswordBetween6And10Chars() {
        userService.generateUsers(20).forEach(u ->
                assertThat(u.getPassword().length()).isBetween(6, 10)
        );
    }

    @Test
    void generateUsers_shouldHaveRoleAdminOrUser() {
        userService.generateUsers(20).forEach(u ->
                assertThat(u.getRole()).isIn("admin", "user")
        );
    }

    // ── batchImport ────────────────────────────────────────────────────────────

    @Test
    void batchImport_shouldImportNewUser() throws Exception {
        UserDto dto = buildDto("john", "john@example.com", "pass123");
        MockMultipartFile file = toFile(List.of(dto));

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(false);

        BatchImportResult result = userService.batchImport(file);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getImported()).isEqualTo(1);
        assertThat(result.getSkipped()).isEqualTo(0);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void batchImport_shouldSkipDuplicateEmail() throws Exception {
        UserDto dto = buildDto("jane", "jane@example.com", "pass123");
        MockMultipartFile file = toFile(List.of(dto));

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        BatchImportResult result = userService.batchImport(file);

        assertThat(result.getImported()).isEqualTo(0);
        assertThat(result.getSkipped()).isEqualTo(1);
        verify(userRepository, never()).save(any());
    }

    @Test
    void batchImport_shouldSkipDuplicateUsername() throws Exception {
        UserDto dto = buildDto("bob", "bob@example.com", "pass123");
        MockMultipartFile file = toFile(List.of(dto));

        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        BatchImportResult result = userService.batchImport(file);

        assertThat(result.getSkipped()).isEqualTo(1);
        verify(userRepository, never()).save(any());
    }

    @Test
    void batchImport_shouldEncodePassword() throws Exception {
        UserDto dto = buildDto("alice", "alice@example.com", "mySecret");
        MockMultipartFile file = toFile(List.of(dto));

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        userService.batchImport(file);
        verify(userRepository).save(captor.capture());

        String stored = captor.getValue().getPassword();
        assertThat(stored).isNotEqualTo("mySecret");
        assertThat(passwordEncoder.matches("mySecret", stored)).isTrue();
    }

    @Test
    void batchImport_shouldReturnCorrectCountsForMixedInput() throws Exception {
        UserDto u1 = buildDto("user1", "u1@example.com", "pass1");
        UserDto u2 = buildDto("user2", "u2@example.com", "pass2");
        UserDto u3 = buildDto("user3", "u3@example.com", "pass3");
        MockMultipartFile file = toFile(List.of(u1, u2, u3));

        when(userRepository.existsByEmail("u1@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("u2@example.com")).thenReturn(true);  // doublon
        when(userRepository.existsByEmail("u3@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user3")).thenReturn(false);

        BatchImportResult result = userService.batchImport(file);

        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getImported()).isEqualTo(2);
        assertThat(result.getSkipped()).isEqualTo(1);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private UserDto buildDto(String username, String email, String password) {
        UserDto dto = new UserDto();
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setCity("Paris");
        dto.setCountry("FR");
        dto.setAvatar("https://randomuser.me/api/portraits/men/1.jpg");
        dto.setCompany("ACME Corp");
        dto.setJobPosition("Developer");
        dto.setMobile("0600000000");
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setRole("user");
        return dto;
    }

    private MockMultipartFile toFile(List<UserDto> dtos) throws Exception {
        byte[] json = objectMapper.writeValueAsBytes(dtos);
        return new MockMultipartFile("file", "users.json", "application/json", json);
    }
}
