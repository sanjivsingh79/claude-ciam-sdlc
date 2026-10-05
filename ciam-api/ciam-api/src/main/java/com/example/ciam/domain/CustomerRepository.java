package com.example.ciam.domain;

import java.util.Optional;

public interface CustomerRepository {

    /**
     * Stores the customer unless another customer already uses the same normalized email.
     * The check and the insert are atomic.
     *
     * @return {@code true} if the customer was stored, {@code false} if the email is taken
     */
    boolean saveIfEmailUnique(Customer customer);

    Optional<Customer> findByEmail(String normalizedEmail);
}
