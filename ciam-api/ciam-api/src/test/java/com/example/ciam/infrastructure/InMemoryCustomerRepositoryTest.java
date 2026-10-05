package com.example.ciam.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ciam.domain.Customer;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class InMemoryCustomerRepositoryTest {

    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();

    @Test
    void storesCustomerWhenEmailIsUnique() {
        Customer customer = customer("jane@example.com");

        assertThat(repository.saveIfEmailUnique(customer)).isTrue();
        assertThat(repository.findByEmail("jane@example.com")).contains(customer);
    }

    @Test
    void rejectsSecondCustomerWithSameEmail() {
        Customer first = customer("jane@example.com");
        repository.saveIfEmailUnique(first);

        assertThat(repository.saveIfEmailUnique(customer("jane@example.com"))).isFalse();
        assertThat(repository.findByEmail("jane@example.com")).contains(first);
    }

    @Test
    void onlyOneOfManyConcurrentSavesForSameEmailSucceeds() {
        long stored = IntStream.range(0, 200).parallel()
                .mapToObj(i -> repository.saveIfEmailUnique(customer("race@example.com")))
                .filter(Boolean::booleanValue)
                .count();

        assertThat(stored).isEqualTo(1);
    }

    @Test
    void findByEmailReturnsEmptyForUnknownEmail() {
        assertThat(repository.findByEmail("unknown@example.com")).isEmpty();
    }

    private static Customer customer(String email) {
        return new Customer(UUID.randomUUID(), email, "Jane", "Doe", Instant.EPOCH);
    }
}
