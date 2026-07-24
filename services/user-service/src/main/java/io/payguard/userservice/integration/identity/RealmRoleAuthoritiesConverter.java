package io.payguard.userservice.integration.identity;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Component
public class RealmRoleAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS);
        List<GrantedAuthority> authorities = new ArrayList<>();
        List<String> roles = realmAccess == null ? Collections.emptyList() :
                (List<String>) realmAccess.getOrDefault(ROLES, Collections.emptyList());

        roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .forEach(authorities::add);

        String scope = jwt.getClaimAsString("scope");
        if (scope != null) {
            for (String value : scope.split(" ")) {
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + value));
            }
        }

        return authorities;
    }

}
