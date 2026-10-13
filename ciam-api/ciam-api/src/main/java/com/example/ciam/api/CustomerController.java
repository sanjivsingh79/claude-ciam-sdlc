package com.example.ciam.api;

import com.example.ciam.domain.Customer;
import com.example.ciam.domain.CustomerRepository;
import com.example.ciam.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public CustomerController(
            CustomerService customerService,
            CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@Valid @RequestBody CreateCustomerRequest request) {
        System.out.println("Registering customer email: " + request.email());
        customerService.register(request.toCommand());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> findById(@PathVariable UUID customerId) {
        return customerRepository.findById(customerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // TEST ONLY: deliberately exposes the domain object at the API boundary.
    @GetMapping("/test-exposure/{customerId}")
    public ResponseEntity<Customer> testExposure(@PathVariable UUID customerId) {
        return customerRepository.findById(customerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
