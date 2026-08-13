package io.payguard.userservice.integration.web.user;

import io.payguard.userservice.application.time.TimeProvider;
import io.payguard.userservice.application.user.profile.CreateUserProfileResult;
import io.payguard.userservice.application.user.profile.CreateUserProfileService;
import io.payguard.userservice.application.user.query.CurrentSettlementAccountResult;
import io.payguard.userservice.application.user.query.CurrentUserQueryService;
import io.payguard.userservice.application.user.query.CurrentUserResult;
import io.payguard.userservice.domain.settlement.SettlementAccountHolderType;
import io.payguard.userservice.domain.settlement.SettlementAccountRequiredAction;
import io.payguard.userservice.domain.settlement.SettlementAccountStatus;
import io.payguard.userservice.domain.settlement.SettlementProvider;
import io.payguard.userservice.domain.user.UserStatus;
import io.payguard.userservice.integration.identity.RealmRoleAuthoritiesConverter;
import io.payguard.userservice.integration.web.common.GlobalExceptionHandler;
import io.payguard.userservice.integration.web.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfiguration.class,
        UserWebMapperImpl.class
})
class UserControllerTests {

    private static final UUID USER_ID = UUID.fromString(
            "7f895b10-d59f-43e5-96e7-0ae9658f90b0"
    );
    private static final UUID SETTLEMENT_ACCOUNT_ID = UUID.fromString(
            "3cf089b0-6222-46fa-b779-93d3ecdfb899"
    );
    private static final Instant NOW = Instant.parse("2026-08-13T08:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateUserProfileService createUserProfileService;

    @MockitoBean
    private CurrentUserQueryService currentUserQueryService;

    @MockitoBean
    private TimeProvider timeProvider;

    @MockitoBean
    private RealmRoleAuthoritiesConverter authoritiesConverter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createsProfileForAuthenticatedUser() throws Exception {
        given(createUserProfileService.execute(any()))
                .willReturn(new CreateUserProfileResult(
                        USER_ID,
                        UserStatus.ACTIVE,
                        true
                ));

        String requestBody = validProfileRequest();

        mockMvc.perform(post("/api/v1/users")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void returnsExistingProfileWithOk() throws Exception {
        given(createUserProfileService.execute(any()))
                .willReturn(new CreateUserProfileResult(
                        USER_ID,
                        UserStatus.ACTIVE,
                        false
                ));

        mockMvc.perform(post("/api/v1/users")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProfileRequest()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsCurrentUserProfile() throws Exception {
        given(currentUserQueryService.execute(any()))
                .willReturn(currentUserResult());

        mockMvc.perform(get("/api/v1/users/me").with(userJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Alex"))
                .andExpect(jsonPath("$.marketCountry").value("GB"))
                .andExpect(jsonPath("$.settlementAccount.provider")
                        .value("STRIPE"))
                .andExpect(jsonPath("$.settlementAccount.providerAccountId")
                        .doesNotExist())
                .andExpect(jsonPath("$.settlementAccount.requiredAction")
                        .value("CONTINUE_ONBOARDING"))
                .andExpect(jsonPath("$.settlementAccount.canReceiveTransfers")
                        .value(false));
    }

    @Test
    void rejectsMissingAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMissingUserRole() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void validatesProfileRequest() throws Exception {
        given(timeProvider.now()).willReturn(NOW);
        String requestBody = "{}";

        mockMvc.perform(post("/api/v1/users")
                        .with(userJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.errors.length()").value(4));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor userJwt() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private static String validProfileRequest() {
        return """
                {
                  "displayName": "Alex",
                  "accountHolderName": "Alex Smith",
                  "accountHolderType": "INDIVIDUAL",
                  "country": "GB"
                }
                """;
    }

    private static CurrentUserResult currentUserResult() {
        return new CurrentUserResult(
                USER_ID,
                "user@test.com",
                "Alex",
                "GB",
                UserStatus.ACTIVE,
                new CurrentSettlementAccountResult(
                        SETTLEMENT_ACCOUNT_ID,
                        SettlementProvider.STRIPE,
                        null,
                        "Alex Smith",
                        SettlementAccountHolderType.INDIVIDUAL,
                        "GB",
                        SettlementAccountStatus.PENDING_ONBOARDING,
                        null,
                        SettlementAccountRequiredAction.CONTINUE_ONBOARDING,
                        false,
                        NOW,
                        NOW
                ),
                NOW,
                NOW
        );
    }
}
