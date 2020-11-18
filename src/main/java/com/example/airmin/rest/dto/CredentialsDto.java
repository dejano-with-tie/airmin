package com.example.airmin.rest.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CredentialsDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
