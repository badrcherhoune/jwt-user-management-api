package com.challenge.users.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private String firstName;
    private String lastName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String city;
    private String country;
    private String avatar;
    private String company;
    private String jobPosition;
    private String mobile;
    private String username;
    private String email;
    private String password;
    private String role;
}
