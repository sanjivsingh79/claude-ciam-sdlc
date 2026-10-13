package com.example.ciam.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ciam.domain.Customer;
import com.example.ciam.infrastructure.InMemoryCustomerRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Registration tests using a synthetic identity from the ciam-test-data MCP server
 * (customer TEST-10002, scenario mfa_enrolment, ACTIVE, MFA disabled). Only the email is
 * used; MFA enrolment itself is not implemented by the application.
 */
class McpAssistedCustomerRegistrationTest {

    private static final Instant NOW = Instant.parse("2026-10-02T10:15:30Z");
    private static final String SYNTHETIC_EMAIL = "mfa-enrolment-01@example.test";

    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
    private final CustomerService service = new CustomerService(repository, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void registeredCustomerCanBeFoundByIdThroughService() {
        service.register(new CreateCustomerCommand(SYNTHETIC_EMAIL, "Test", "Customer"));
        Customer stored = repository.findByEmail(SYNTHETIC_EMAIL).orElseThrow();

        assertThat(service.findById(stored.id())).contains(stored);
        assertThat(service.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void customersWithDifferentEmailsAreStoredIndependently() {
        service.register(new CreateCustomerCommand(SYNTHETIC_EMAIL, "Test", "Customer"));
        service.register(new CreateCustomerCommand("other-" + SYNTHETIC_EMAIL, "Other", "Customer"));

        Customer first = repository.findByEmail(SYNTHETIC_EMAIL).orElseThrow();
        Customer second = repository.findByEmail("other-" + SYNTHETIC_EMAIL).orElseThrow();
        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(service.findById(first.id())).contains(first);
        assertThat(service.findById(second.id())).contains(second);
    }
}
