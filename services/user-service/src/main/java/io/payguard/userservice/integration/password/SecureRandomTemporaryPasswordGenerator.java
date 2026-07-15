package io.payguard.userservice.integration.password;

import io.payguard.userservice.application.password.TemporaryPasswordGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SecureRandomTemporaryPasswordGenerator
        implements TemporaryPasswordGenerator {

    private static final String UPPERCASE =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String LOWERCASE =
            "abcdefghijklmnopqrstuvwxyz";

    private static final String DIGITS =
            "0123456789";

    private static final String SPECIAL =
            "!@#$%^&*()-_=+";

    private static final String ALL =
            UPPERCASE +
                    LOWERCASE +
                    DIGITS +
                    SPECIAL;

    private static final int PASSWORD_LENGTH = 16;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {

        List<Character> password = new ArrayList<>(PASSWORD_LENGTH);

        password.add(randomChar(UPPERCASE));
        password.add(randomChar(LOWERCASE));
        password.add(randomChar(DIGITS));
        password.add(randomChar(SPECIAL));

        while (password.size() < PASSWORD_LENGTH) {
            password.add(randomChar(ALL));
        }

        Collections.shuffle(password, random);

        StringBuilder builder = new StringBuilder(PASSWORD_LENGTH);

        password.forEach(builder::append);

        return builder.toString();
    }

    private char randomChar(String source) {

        return source.charAt(
                random.nextInt(source.length())
        );
    }

}