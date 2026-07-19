package io.payguard.userservice.application.payment.events;

public interface StripeEventHandler<T> {

    StripeEventType supports();

    Class<T> eventClass();

    void handle(T event);

}