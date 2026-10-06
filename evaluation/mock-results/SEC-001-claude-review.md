# Claude PR Review

## Overall assessment

The Pull Request introduces a CIAM security concern because customer email
addresses are written directly to application logs.

## Findings by severity

### [HIGH] Customer PII logged

- **File:** `src/main/java/com/example/ciam/service/CustomerService.java`
- **Line:** 42
- **Evidence:** `customer.getEmail()` is written directly to the application log.
- **Expected:** Customer email addresses must not be written directly to logs.
- **Recommendation:** Remove the email address from the log message or use an approved non-sensitive identifier.

## Positive observations

The change does not expose credentials or authentication tokens.

## Recommended actions

Remove the customer email address from the log statement.