package rubbles.monitoring.commcoverage.common.core;

public interface Supplier<T extends Message> {
    T get();
}
