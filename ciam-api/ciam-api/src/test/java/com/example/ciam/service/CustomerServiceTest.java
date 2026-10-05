package com.example.ciam.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ciam.domain.Customer;
import com.example.ciam.infrastructure.InMemoryCustomerRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-02T10:15:30Z");

    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
    private final CustomerService service = new CustomerService(repository, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void registersCustomerWithNormalizedValuesAndServerGeneratedFields() {
        service.register(new CreateCustomerCommand("  Jane.Doe@Example.COM ", " Jane ", " Doe "));

        Customer customer = repository.findByEmail("jane.doe@example.com").orElseThrow();
        assertThat(customer.id()).isNotNull();
        assertThat(customer.email()).isEqualTo("jane.doe@example.com");
        assertThat(customer.givenName()).isEqualTo("Jane");
        assertThat(customer.familyName()).isEqualTo("Doe");
        assertThat(customer.createdAt()).isEqualTo(NOW);
    }

    @Test
    void duplicateEmailIsIgnoredWithoutOverwritingExistingCustomer() {
        service.register(new CreateCustomerCommand("jane@example.com", "Jane", "Doe"));
        Customer original = repository.findByEmail("jane@example.com").orElseThrow();

        service.register(new CreateCustomerCommand("JANE@example.com", "Other", "Person"));

        assertThat(repository.findByEmail("jane@example.com")).contains(original);
    }

    @Test
    void customerToStringDoesNotExposePii() {
        service.register(new CreateCustomerCommand("jane@example.com", "Jane", "Doe"));
        Customer customer = repository.findByEmail("jane@example.com").orElseThrow();

        assertThat(customer.toString())
                .contains(customer.id().toString())
                .doesNotContain("jane@example.com", "Jane", "Doe");
        assertThat(new CreateCustomerCommand("jane@example.com", "Jane", "Doe").toString())
                .doesNotContain("jane@example.com", "Jane", "Doe");
    }
}
