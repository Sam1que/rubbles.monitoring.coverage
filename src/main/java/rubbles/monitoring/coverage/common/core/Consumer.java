package rubbles.monitoring.coverage.common.core;

public interface Consumer<T> {
    void accept(T message);
}
