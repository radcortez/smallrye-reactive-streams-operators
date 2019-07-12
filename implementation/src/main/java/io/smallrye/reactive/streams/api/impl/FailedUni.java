package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.UniSubscriber;

import java.util.Objects;
import java.util.function.Supplier;

public class FailedUni<O> extends UniOperator<Void, O> {
    private final Supplier<? extends Throwable> supplier;

    public FailedUni(Throwable throwable) {
        super(null);
        Objects.requireNonNull(throwable, "`throwable` cannot be `null`");
        this.supplier = () -> throwable;
    }

    public FailedUni(Supplier<? extends Throwable> supplier) {
        super(null);
        Objects.requireNonNull(supplier, "`supplier` cannot be `null`");
        this.supplier = supplier;
    }

    @Override
    public void subscribe(UniSubscriber<? super O> subscriber) {

        Throwable failure;

        try {
            failure = supplier.get();
        } catch (Exception e) {
            subscriber.onSubscribe(EmptySubscription.INSTANCE);
            // Propagate the exception thrown by the supplier
            subscriber.onFailure(e);
            return;
        }

        subscriber.onSubscribe(EmptySubscription.INSTANCE);
        // Propagate the exception produced by the supplier
        subscriber.onFailure(failure);

    }
}
