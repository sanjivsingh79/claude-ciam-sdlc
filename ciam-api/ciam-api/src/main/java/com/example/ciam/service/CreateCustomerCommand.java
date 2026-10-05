package com.example.ciam.service;

public record CreateCustomerCommand(String email, String givenName, String familyName) {

    @Override
    public String toString() {
        return "CreateCustomerCommand[<redacted>]";
    }
}
