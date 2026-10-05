package com.example.ciam.infrastructure;

import com.example.ciam.domain.Customer;
import com.example.ciam.domain.CustomerRepository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

/**
 * Non-durable store keyed by normalized email; data is lost on restart.
 */
@Repository
public class InMemoryCustomerRepository implements CustomerRepository {

    private final ConcurrentMap<String, Customer> customersByEmail = new ConcurrentHashMap<>();

    @Override
    public boolean saveIfEmailUnique(Customer customer) {
        return customersByEmail.putIfAbsent(customer.email(), customer) == null;
    }

    @Override
    public Optional<Customer> findByEmail(String normalizedEmail) {
        return Optional.ofNullable(customersByEmail.get(normalizedEmail));
    }
}
