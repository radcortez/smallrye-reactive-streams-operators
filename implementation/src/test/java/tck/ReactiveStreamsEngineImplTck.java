package tck;

import io.smallrye.reactive.streams.Engine;
import org.eclipse.microprofile.reactive.streams.tck.ReactiveStreamsTck;
import org.reactivestreams.tck.TestEnvironment;

/**
 * Executes the TCK again the implementation.
 */
public class ReactiveStreamsEngineImplTck extends ReactiveStreamsTck<Engine> {

    public ReactiveStreamsEngineImplTck() {
        super(new TestEnvironment(100));
    }

    @Override
    protected Engine createEngine() {
        return new Engine();
    }

}
