package com.example.ciam.infrastructure;

import com.example.ciam.domain.Customer;
import com.example.ciam.domain.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Non-durable store keyed by normalized email; data is lost on restart.
 */
@Repository
public class InMemoryCustomerRepository implements CustomerRepository {

    private final ConcurrentMap<String, Customer> customersByEmail = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Customer> customersById = new ConcurrentHashMap<>();
    @Override
    public boolean saveIfEmailUnique(Customer customer) {
        if (customersByEmail.putIfAbsent(customer.email(), customer) != null) {
            return false;
        }

        customersById.put(customer.id(), customer);
        return true;
    }

    @Override
    public Optional<Customer> findByEmail(String normalizedEmail) {
        return Optional.ofNullable(customersByEmail.get(normalizedEmail));
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        return Optional.ofNullable(customersById.get(customerId));
    }
}
