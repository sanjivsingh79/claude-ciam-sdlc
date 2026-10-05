package com.example.ciam.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.ciam.service.CreateCustomerCommand;
import com.example.ciam.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    private static final String VALID_BODY = """
            {"email":"jane@example.com","givenName":"Jane","familyName":"Doe"}
            """;

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void validRequestReturns202WithNoBodyAndNoLocation() {
        MvcTestResult result = post(VALID_BODY);

        assertThat(result).hasStatus(HttpStatus.ACCEPTED);
        assertThat(result).doesNotContainHeader("Location");
        assertThat(result).bodyText().isEmpty();

        ArgumentCaptor<CreateCustomerCommand> captor = ArgumentCaptor.forClass(CreateCustomerCommand.class);
        verify(customerService).register(captor.capture());
        assertThat(captor.getValue())
                .isEqualTo(new CreateCustomerCommand("jane@example.com", "Jane", "Doe"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            missing email           | {"givenName":"Jane","familyName":"Doe"}                                  | email
            blank email             | {"email":"  ","givenName":"Jane","familyName":"Doe"}                      | email
            malformed email         | {"email":"not-an-email","givenName":"Jane","familyName":"Doe"}            | email
            missing givenName       | {"email":"jane@example.com","familyName":"Doe"}                           | givenName
            blank givenName         | {"email":"jane@example.com","givenName":" ","familyName":"Doe"}           | givenName
            missing familyName      | {"email":"jane@example.com","givenName":"Jane"}                           | familyName
            blank familyName        | {"email":"jane@example.com","givenName":"Jane","familyName":""}           | familyName
            """)
    void invalidFieldReturns400ProblemDetail(String description, String body, String field) {
        assertValidationError(post(body), field);
    }

    @Test
    void emailLongerThan254CharactersIsRejected() {
        String email = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(61) + ".com";
        assertThat(email).hasSizeGreaterThan(254);

        assertValidationError(post(body(email, "Jane", "Doe")), "email");
    }

    @Test
    void givenNameLongerThan100CharactersIsRejected() {
        assertValidationError(post(body("jane@example.com", "x".repeat(101), "Doe")), "givenName");
    }

    @Test
    void familyNameLongerThan100CharactersIsRejected() {
        assertValidationError(post(body("jane@example.com", "Jane", "x".repeat(101))), "familyName");
    }

    @Test
    void namesOfExactly100CharactersAreAccepted() {
        assertThat(post(body("jane@example.com", "x".repeat(100), "y".repeat(100)))).hasStatus(HttpStatus.ACCEPTED);
    }

    @Test
    void validationErrorDoesNotEchoRejectedValue() {
        MvcTestResult result = post(body("secret-value-not-an-email", "Jane", "Doe"));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyText().doesNotContain("secret-value-not-an-email");
    }

    @Test
    void malformedJsonReturns400ProblemDetail() {
        MvcTestResult result = post("{\"email\":");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(400);
        verifyNoInteractions(customerService);
    }

    @Test
    void missingBodyReturns400ProblemDetail() {
        MvcTestResult result = mvc.post().uri("/customers").contentType(MediaType.APPLICATION_JSON).exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void unsupportedMediaTypeReturns415ProblemDetail() {
        MvcTestResult result = mvc.post().uri("/customers")
                .contentType(MediaType.TEXT_PLAIN)
                .content(VALID_BODY)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(415);
        verifyNoInteractions(customerService);
    }

    @Test
    void unsupportedMethodReturns405ProblemDetail() {
        MvcTestResult result = mvc.get().uri("/customers").exchange();

        assertThat(result).hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void unexpectedErrorReturnsGeneric500WithoutInternals() {
        doThrow(new IllegalStateException("internal detail jane@example.com"))
                .when(customerService).register(any());

        MvcTestResult result = post(VALID_BODY);

        assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("An unexpected error occurred.");
        assertThat(result).bodyText()
                .doesNotContain("IllegalStateException")
                .doesNotContain("internal detail")
                .doesNotContain("jane@example.com");
    }

    private void assertValidationError(MvcTestResult result, String field) {
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(400);
        assertThat(result).bodyJson().extractingPath("$.title").isEqualTo("Bad Request");
        assertThat(result).bodyJson().extractingPath("$.instance").isEqualTo("/customers");
        assertThat(result).bodyJson().extractingPath("$.errors[*].field").asArray().isNotEmpty().containsOnly(field);
        assertThat(result).bodyJson().extractingPath("$.errors[0].message").asString().isNotBlank();
        verifyNoInteractions(customerService);
    }

    private MvcTestResult post(String body) {
        return mvc.post().uri("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();
    }

    private static String body(String email, String givenName, String familyName) {
        return """
                {"email":"%s","givenName":"%s","familyName":"%s"}
                """.formatted(email, givenName, familyName);
    }
}
