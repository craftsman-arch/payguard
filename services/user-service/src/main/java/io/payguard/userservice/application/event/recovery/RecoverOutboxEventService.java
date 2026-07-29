package io.payguard.userservice.application.event.recovery;

import io.payguard.userservice.application.event.OutboxEventRecoveryRepository;
import io.payguard.userservice.application.event.OutboxEventRecoveryResult;
import io.payguard.userservice.application.id.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoverOutboxEventService {

    private final OutboxEventRecoveryRepository recoveryRepository;
    private final IdGenerator idGenerator;

    public RecoverOutboxEventResult execute(RecoverOutboxEventCommand command) {

        UUID recoveryId = idGenerator.generate();

        OutboxEventRecoveryResult result =
                recoveryRepository.recover(
                        recoveryId,
                        command.eventId(),
                        command.recoveredBy(),
                        command.reason()
                );

        return switch (result) {

            case RECOVERED ->
                    recovered(
                            recoveryId,
                            command
                    );

            case EVENT_NOT_FOUND ->
                    throw new OutboxEventNotFoundException(
                            command.eventId()
                    );

            case EVENT_NOT_EXHAUSTED ->
                    throw new OutboxEventNotExhaustedException(
                            command.eventId()
                    );

            case EVENT_ALREADY_PUBLISHED ->
                    throw new OutboxEventAlreadyPublishedException(
                            command.eventId()
                    );
        };
    }

    private RecoverOutboxEventResult recovered(UUID recoveryId, RecoverOutboxEventCommand command) {

        log.info(
                "Recovered exhausted outbox event [{}] with recovery [{}] by operator [{}].",
                command.eventId(),
                recoveryId,
                command.recoveredBy()
        );

        return new RecoverOutboxEventResult(recoveryId, command.eventId());
    }
}
