package com.example.airmin.rest.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class SignupDto {
    @NotBlank
    @Length(max = 255)
    private String firstName;
    @NotBlank
    @Length(max = 255)
    private String lastName;
    @NotBlank
    @Length(min = 3, max = 255)
    private String username;
    @NotBlank
    @Length(max = 255)
    // Maybe define pass complexity and validator
    private String password;
}
