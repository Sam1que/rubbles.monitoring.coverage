package rubbles.monitoring.coverage.common.core;

public interface Supplier<T extends Message> {
    T get();
}
