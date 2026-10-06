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
    private  CustomerRepository customerRepository;
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Always answers 202 Accepted with no body, whether or not the email was already
     * registered, so the response does not reveal account existence.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@Valid @RequestBody CreateCustomerRequest request) {
        customerService.register(request.toCommand());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> findById(@PathVariable UUID customerId) {
        return customerService.findById(customerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
