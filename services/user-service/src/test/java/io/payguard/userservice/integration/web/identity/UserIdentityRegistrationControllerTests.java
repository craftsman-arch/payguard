package io.payguard.userservice.integration.web.identity;

import io.payguard.userservice.application.identity.registration.RegisterUserIdentityCommand;
import io.payguard.userservice.application.identity.registration.RegisterUserIdentityService;
import io.payguard.userservice.application.identity.registration.ResendUserVerificationEmailCommand;
import io.payguard.userservice.application.identity.registration.ResendUserVerificationEmailService;
import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.integration.identity.RealmRoleAuthoritiesConverter;
import io.payguard.userservice.integration.identity.exception.KeycloakException;
import io.payguard.userservice.integration.identity.exception.KeycloakPasswordPolicyException;
import io.payguard.userservice.integration.identity.exception.KeycloakUnavailableException;
import io.payguard.userservice.integration.identity.exception.KeycloakUserConflictException;
import io.payguard.userservice.integration.identity.exception.KeycloakUserCreationException;
import io.payguard.userservice.integration.web.common.GlobalExceptionHandler;
import io.payguard.userservice.integration.web.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserIdentityRegistrationController.class)
@Import({GlobalExceptionHandler.class, SecurityConfiguration.class})
class UserIdentityRegistrationControllerTests {

    private static final Instant NOW = Instant.parse("2026-08-11T08:00:00Z");
    private static final String PASSWORD = "PayGuard-12345!";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterUserIdentityService registrationService;

    @MockitoBean
    private ResendUserVerificationEmailService resendVerificationEmailService;

    @MockitoBean
    private TimeProvider timeProvider;

    @MockitoBean
    private RealmRoleAuthoritiesConverter authoritiesConverter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void registersUserWithoutAuthentication() throws Exception {

        String requestBody = validRegistrationBody();

        mockMvc.perform(post("/api/v1/user-registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"))
                .andExpect(content().string(not(containsString(PASSWORD))));

        ArgumentCaptor<RegisterUserIdentityCommand> commandCaptor = ArgumentCaptor.forClass(RegisterUserIdentityCommand.class);

        verify(registrationService).execute(commandCaptor.capture());

        RegisterUserIdentityCommand command = commandCaptor.getValue();
        assertThat(command.email().getValue()).isEqualTo("user@test.com");
        assertThat(command.password()).isEqualTo(PASSWORD);
        assertThat(command.toString()).doesNotContain(PASSWORD);
    }

    @Test
    void rejectsInvalidRegistrationRequest() throws Exception {

        givenCurrentTime();

        String requestBody = """
                {
                  "email": "invalid",
                  "password": ""
                }
        """;

        mockMvc.perform(post("/api/v1/user-registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").value(NOW.toString()))
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.path").value("/api/v1/user-registrations"))
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @ParameterizedTest
    @MethodSource("identityProviderFailures")
    void mapsIdentityProviderFailures(KeycloakException exception, int expectedStatus, String expectedMessage) throws Exception {

        givenCurrentTime();
        doThrow(exception).when(registrationService).execute(any());
        String requestBody = validRegistrationBody();

        mockMvc.perform(post("/api/v1/user-registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.timestamp").value(NOW.toString()))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value("/api/v1/user-registrations"))
                .andExpect(content().string(not(containsString(PASSWORD))));
    }

    @Test
    void resendsVerificationEmailWithoutAuthentication() throws Exception {

        String requestBody = """
                {
                  "email": "user@test.com"
                }
        """;

        mockMvc.perform(post("/api/v1/user-registrations/verification-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value(
                        "VERIFICATION_EMAIL_REQUESTED"
                ));

        ArgumentCaptor<ResendUserVerificationEmailCommand> commandCaptor = ArgumentCaptor.forClass(ResendUserVerificationEmailCommand.class);

        verify(resendVerificationEmailService).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().email().getValue()).isEqualTo("user@test.com");
    }

    @Test
    void doesNotExposeNeighbouringRegistrationPath() throws Exception {

        String requestBody = "{}";

        mockMvc.perform(post("/api/v1/user-registrations/internal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    private void givenCurrentTime() {
        org.mockito.BDDMockito.given(timeProvider.now()).willReturn(NOW);
    }

    private static String validRegistrationBody() {
        return """
                {
                  "email": "user@test.com",
                  "password": "PayGuard-12345!"
                }
        """;
    }

    private static Stream<Arguments> identityProviderFailures() {
        RuntimeException cause = new RuntimeException("provider detail");

        return Stream.of(
                Arguments.of(
                        new KeycloakUserConflictException("conflict", cause),
                        409,
                        "User identity already exists."
                ),
                Arguments.of(
                        new KeycloakPasswordPolicyException("policy", cause),
                        422,
                        "Password does not satisfy the security policy."
                ),
                Arguments.of(
                        new KeycloakUnavailableException("unavailable", cause),
                        503,
                        "Identity provider is temporarily unavailable."
                ),
                Arguments.of(
                        new KeycloakUserCreationException("unexpected"),
                        502,
                        "Identity provider failed to process the request."
                )
        );
    }
}
