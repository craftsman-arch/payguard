package io.payguard.userservice.application.payment.events;

import io.payguard.userservice.integration.payment.events.StripeEventReader;
import io.payguard.userservice.integration.payment.events.StripeSignatureVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessStripeEventService {

    private final StripeSignatureVerifier signatureVerifier;
    private final StripeEventReader eventReader;
    private final StripeEventDispatcher dispatcher;

    public void execute(
            ProcessStripeEventCommand command
    ) {

        signatureVerifier.verify(command.payload(), command.signature());
        dispatcher.dispatch(eventReader.read(command.payload()));
    }

}