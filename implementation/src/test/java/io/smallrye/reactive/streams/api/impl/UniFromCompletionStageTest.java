package io.smallrye.reactive.streams.api.impl;

import io.smallrye.reactive.streams.api.AssertSubscriber;
import io.smallrye.reactive.streams.api.Uni;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class UniFromCompletionStageTest {

    @Test
    public void testThatNullValueAreAccepted() {
        AssertSubscriber<String> ts = AssertSubscriber.create();
        CompletionStage<String> cs = new CompletableFuture<>();
        Uni.from().completionStage(cs).subscribe().withSubscriber(ts);
        cs.toCompletableFuture().complete(null);
        ts.assertCompletedSuccessfully().assertResult(null);
    }


    @Test
    public void testWithNonNullValue() {
        AssertSubscriber<String> ts = AssertSubscriber.create();
        CompletionStage<String> cs = new CompletableFuture<>();
        Uni.from().completionStage(cs).subscribe().withSubscriber(ts);
        cs.toCompletableFuture().complete("1");
        ts.assertCompletedSuccessfully().assertResult("1");
    }


    @Test
    public void testWithException() {
        AssertSubscriber<String> ts = AssertSubscriber.create();
        CompletionStage<String> cs = new CompletableFuture<>();
        Uni.from().completionStage(cs).subscribe().withSubscriber(ts);
        cs.toCompletableFuture().completeExceptionally(new IOException("boom"));
        ts.assertFailure(IOException.class, "boom");
    }

    @Test
    public void testThatNullValueAreAcceptedWithSupplier() {
        AssertSubscriber<Void> ts = AssertSubscriber.create();
        Uni.from().<Void>completionStage(() -> CompletableFuture.completedFuture(null)).subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(null);
    }


    @Test
    public void testWithNonNullValueWithSupplier() {
        AssertSubscriber<String> ts = AssertSubscriber.create();
        CompletionStage<String> cs = new CompletableFuture<>();
        Uni.from().completionStage(() -> cs).subscribe().withSubscriber(ts);
        cs.toCompletableFuture().complete("1");
        ts.assertCompletedSuccessfully().assertResult("1");
    }


    @Test
    public void testWithExceptionWithSupplier() {
        AssertSubscriber<String> ts = AssertSubscriber.create();
        CompletionStage<String> cs = new CompletableFuture<>();
        Uni.from().completionStage(() -> cs).subscribe().withSubscriber(ts);
        cs.toCompletableFuture().completeExceptionally(new IOException("boom"));
        ts.assertFailure(IOException.class, "boom");
    }

    @Test
    public void testThatValueIsNotEmittedBeforeSubscription() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        cs.complete(1);
        Uni<Integer> uni = Uni.from().completionStage(cs).map(i -> {
            called.set(true);
            return i + 1;
        });


        assertThat(called).isFalse();

        uni.subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(2);
        assertThat(called).isTrue();
    }

    @Test
    public void testThatValueIsNotEmittedBeforeSubscriptionWithSupplier() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        CompletableFuture<Integer> cs = new CompletableFuture<>();

        Uni<Integer> uni = Uni.from().completionStage(() -> {
            called.set(true);
            return cs;
        }).map(i -> i + 1);

        assertThat(called).isFalse();

        cs.complete(1);

        assertThat(called).isFalse();

        uni.subscribe().withSubscriber(ts);
        ts.assertCompletedSuccessfully().assertResult(2);
        assertThat(called).isTrue();
    }

    @Test
    public void testThatSubscriberIsIncompleteIfTheStageDoesNotEmit() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> uni = Uni.from().completionStage(cs).map(i -> {
            called.set(true);
            return i + 1;
        });


        assertThat(called).isFalse();

        uni.subscribe().withSubscriber(ts);
        assertThat(called).isFalse();
        ts.assertNotCompleted();
    }

    @Test
    public void testThatSubscriberIsIncompleteIfTheStageDoesNotEmitFromSupplier() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        AtomicBoolean called = new AtomicBoolean();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> uni = Uni.from().completionStage(() -> cs).map(i -> {
            called.set(true);
            return i + 1;
        });


        assertThat(called).isFalse();

        uni.subscribe().withSubscriber(ts);
        assertThat(called).isFalse();
        ts.assertNotCompleted();
    }

    @Test
    public void testThatSubscriberCanCancelBeforeEmission() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> uni = Uni.from().completionStage(cs).map(i -> i + 1);

        uni.subscribe().withSubscriber(ts);
        ts.cancel();

        cs.complete(1);

        ts.assertNotCompleted();
    }

    @Test
    public void testThatSubscriberCanCancelBeforeEmissionWithSupplier() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> uni = Uni.from().completionStage(() -> cs).map(i -> i + 1);

        uni.subscribe().withSubscriber(ts);
        ts.cancel();

        cs.complete(1);

        ts.assertNotCompleted();
    }

    @Test
    public void testThatSubscriberCanCancelAfterEmission() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> uni = Uni.from().completionStage(cs).map(i -> i + 1);

        uni.subscribe().withSubscriber(ts);
        cs.complete(1);
        ts.cancel();

        ts.assertResult(2);
    }

    @Test
    public void testThatSubscriberCanCancelAfterEmissionWithSupplier() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        CompletableFuture<Integer> cs = new CompletableFuture<>();
        Uni<Integer> uni = Uni.from().completionStage(() -> cs).map(i -> i + 1);

        uni.subscribe().withSubscriber(ts);
        cs.complete(1);
        ts.cancel();

        ts.assertResult(2);
    }

    @Test(expected = NullPointerException.class)
    public void testThatCompletionStageCannotBeNull() {
        Uni.from().completionStage((CompletionStage<Void>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testThatCompletionStageSupplierCannotBeNull() {
        Uni.from().completionStage((Supplier<CompletionStage<Void>>) null);
    }

    @Test
    public void testThatCompletionStageSupplierCannotReturnNull() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Uni<Integer> uni = Uni.from().completionStage(() -> null);

        uni.subscribe().withSubscriber(ts);
        ts.assertFailure(NullPointerException.class, "");
    }


}