package com.example.bankcards.dto;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import com.example.bankcards.entity.Role;
public record UserDTO(
        Long id,

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        Role role
) {}