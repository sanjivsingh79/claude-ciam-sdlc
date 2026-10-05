package com.example.ciam.api;

import com.example.ciam.service.CreateCustomerCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName) {

    /** Strips surrounding whitespace so that validation applies to the effective values. */
    public CreateCustomerRequest {
        email = strip(email);
        givenName = strip(givenName);
        familyName = strip(familyName);
    }

    private static String strip(String value) {
        return value == null ? null : value.strip();
    }

    CreateCustomerCommand toCommand() {
        return new CreateCustomerCommand(email, givenName, familyName);
    }

    @Override
    public String toString() {
        return "CreateCustomerRequest[<redacted>]";
    }
}
