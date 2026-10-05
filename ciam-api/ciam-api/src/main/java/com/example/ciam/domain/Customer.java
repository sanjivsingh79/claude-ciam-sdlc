package com.example.ciam.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A registered customer. {@link #toString()} exposes only the ID so that PII cannot leak
 * into logs by accident.
 */
public record Customer(UUID id, String email, String givenName, String familyName, Instant createdAt) {

    public Customer {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(givenName, "givenName");
        Objects.requireNonNull(familyName, "familyName");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    @Override
    public String toString() {
        return "Customer[id=" + id + "]";
    }
}
