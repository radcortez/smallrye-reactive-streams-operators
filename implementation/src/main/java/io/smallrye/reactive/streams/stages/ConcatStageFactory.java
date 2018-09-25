package io.smallrye.reactive.streams.stages;

import io.reactivex.Flowable;
import io.smallrye.reactive.streams.Engine;
import io.smallrye.reactive.streams.utils.CancellablePublisher;
import org.eclipse.microprofile.reactive.streams.spi.Graph;
import org.eclipse.microprofile.reactive.streams.spi.Stage;

import java.util.Objects;

/**
 * Implementation of the {@link Stage.Concat} stage. Because both streams can emits on different thread,
 * this operator takes care to called the user on a Vert.x context if the caller used one, otherwise it
 * uses the current thread.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConcatStageFactory implements PublisherStageFactory<Stage.Concat> {

    @Override
    public <OUT> PublisherStage<OUT> create(Engine engine, Stage.Concat stage) {
        Objects.requireNonNull(engine);
        Objects.requireNonNull(stage);
        Graph g1 = stage.getFirst();
        Graph g2 = stage.getSecond();
        return new ConcatStage<>(engine, g1, g2);
    }

    private class ConcatStage<OUT> implements PublisherStage<OUT> {
        private final Engine engine;
        private final Graph first;
        private final Graph second;

        ConcatStage(Engine engine, Graph g1, Graph g2) {
            this.engine = Objects.requireNonNull(engine);
            this.first = Objects.requireNonNull(g1);
            this.second = Objects.requireNonNull(g2);
        }

        @Override
        public Flowable<OUT> create() {
            CancellablePublisher<OUT> cancellable = new CancellablePublisher<>(engine.buildPublisher(second));
            return Flowable.concat(engine.buildPublisher(first), cancellable)
                    .doOnCancel(cancellable::cancelIfNotSubscribed)
                    .doOnTerminate(cancellable::cancelIfNotSubscribed);
        }
    }
}
