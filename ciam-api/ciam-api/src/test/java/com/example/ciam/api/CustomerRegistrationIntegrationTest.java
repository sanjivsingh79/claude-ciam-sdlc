package com.example.ciam.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ciam.domain.Customer;
import com.example.ciam.domain.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerRegistrationIntegrationTest {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void newCustomerIsStoredWithNormalizedEmail() {
        MvcTestResult result = post("""
                {"email":"  New.Customer@Example.COM ","givenName":" Jane ","familyName":" Doe "}
                """);

        assertThat(result).hasStatus(HttpStatus.ACCEPTED);
        Customer stored = customerRepository.findByEmail("new.customer@example.com").orElseThrow();
        assertThat(stored.givenName()).isEqualTo("Jane");
        assertThat(stored.familyName()).isEqualTo("Doe");
    }

    @Test
    void duplicateEmailGetsResponseIdenticalToNewEmail() {
        MvcTestResult first = post("""
                {"email":"dup@example.com","givenName":"Jane","familyName":"Doe"}
                """);
        MvcTestResult duplicate = post("""
                {"email":"DUP@example.com","givenName":"Mallory","familyName":"Other"}
                """);

        assertThat(first).hasStatus(HttpStatus.ACCEPTED);
        assertThat(duplicate).hasStatus(HttpStatus.ACCEPTED);
        assertSameResponse(first.getMvcResult(), duplicate.getMvcResult());

        Customer stored = customerRepository.findByEmail("dup@example.com").orElseThrow();
        assertThat(stored.givenName()).as("original customer must not be overwritten").isEqualTo("Jane");
    }

    private static void assertSameResponse(MvcResult a, MvcResult b) {
        assertThat(b.getResponse().getStatus()).isEqualTo(a.getResponse().getStatus());
        assertThat(b.getResponse().getHeaderNames()).containsExactlyInAnyOrderElementsOf(a.getResponse().getHeaderNames());
        for (String header : a.getResponse().getHeaderNames()) {
            assertThat(b.getResponse().getHeaders(header)).isEqualTo(a.getResponse().getHeaders(header));
        }
        assertThat(b.getResponse().getContentAsByteArray()).isEqualTo(a.getResponse().getContentAsByteArray());
    }

    private MvcTestResult post(String body) {
        return mvc.post().uri("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();
    }
}
