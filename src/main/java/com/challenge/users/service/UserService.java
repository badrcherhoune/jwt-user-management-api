package com.challenge.users.service;

import com.challenge.users.dto.BatchImportResult;
import com.challenge.users.dto.UserDto;
import com.challenge.users.entity.User;
import com.challenge.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public List<UserDto> generateUsers(int count) {
        Faker faker = new Faker(Locale.ENGLISH);
        List<UserDto> users = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            UserDto dto = new UserDto();

            String firstName = faker.name().firstName();
            String lastName  = faker.name().lastName();
            dto.setFirstName(firstName);
            dto.setLastName(lastName);

            dto.setBirthDate(faker.date().birthday(18, 65)
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

            dto.setCity(faker.address().city());
            dto.setCountry(faker.address().countryCode());

            String gender = faker.random().nextBoolean() ? "men" : "women";
            int avatarId = faker.random().nextInt(1, 99);
            dto.setAvatar("https://randomuser.me/api/portraits/" + gender + "/" + avatarId + ".jpg");

            dto.setCompany(faker.company().name());
            dto.setJobPosition(faker.job().title());
            dto.setMobile(faker.phoneNumber().cellPhone());

            String username = (firstName + "." + lastName + faker.random().nextInt(10, 999))
                    .toLowerCase().replaceAll("[^a-z0-9.]", "");
            dto.setUsername(username);
            dto.setEmail(username + "@" + faker.internet().domainName());

            dto.setPassword(faker.internet().password(6, 10, true, false));
            dto.setRole(faker.random().nextBoolean() ? "admin" : "user");

            users.add(dto);
        }
        return users;
    }

    public BatchImportResult batchImport(MultipartFile file) throws Exception {
        List<UserDto> dtos = Arrays.asList(objectMapper.readValue(file.getInputStream(), UserDto[].class));

        int total    = dtos.size();
        int imported = 0;
        int skipped  = 0;

        for (UserDto dto : dtos) {
            if (userRepository.existsByEmail(dto.getEmail())
                    || userRepository.existsByUsername(dto.getUsername())) {
                skipped++;
                continue;
            }

            User user = new User();
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setBirthDate(dto.getBirthDate());
            user.setCity(dto.getCity());
            user.setCountry(dto.getCountry());
            user.setAvatar(dto.getAvatar());
            user.setCompany(dto.getCompany());
            user.setJobPosition(dto.getJobPosition());
            user.setMobile(dto.getMobile());
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setRole(dto.getRole());

            userRepository.save(user);
            imported++;
        }

        return new BatchImportResult(total, imported, skipped);
    }

    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + username));
        return toDto(user);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setBirthDate(user.getBirthDate());
        dto.setCity(user.getCity());
        dto.setCountry(user.getCountry());
        dto.setAvatar(user.getAvatar());
        dto.setCompany(user.getCompany());
        dto.setJobPosition(user.getJobPosition());
        dto.setMobile(user.getMobile());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
