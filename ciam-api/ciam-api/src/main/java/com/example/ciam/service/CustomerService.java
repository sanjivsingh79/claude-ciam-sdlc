package com.example.ciam.service;

import com.example.ciam.domain.Customer;
import com.example.ciam.domain.CustomerRepository;
import java.time.Clock;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final Clock clock;

    public CustomerService(CustomerRepository customerRepository, Clock clock) {
        this.customerRepository = customerRepository;
        this.clock = clock;
    }

    /**
     * Registers a customer. If the email is already registered the request is ignored and
     * nothing is reported to the caller, so callers cannot tell whether an account exists.
     */
    public void register(CreateCustomerCommand command) {
        Customer customer = new Customer(
                UUID.randomUUID(),
                normalizeEmail(command.email()),
                command.givenName().strip(),
                command.familyName().strip(),
                clock.instant());

        if (customerRepository.saveIfEmailUnique(customer)) {
            log.info("Customer registered: id={}", customer.id());
        } else {
            log.info("Customer registration ignored: email already registered");
        }
    }

    static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
