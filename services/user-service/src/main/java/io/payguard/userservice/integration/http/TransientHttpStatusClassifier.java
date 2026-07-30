package io.payguard.userservice.integration.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public final class TransientHttpStatusClassifier {

    private TransientHttpStatusClassifier() {
    }

    public static boolean isTransient(HttpStatusCode statusCode) {

        return statusCode.value() == HttpStatus.REQUEST_TIMEOUT.value()
                || statusCode.value()
                == HttpStatus.TOO_MANY_REQUESTS.value();
    }
}
