package rubbles.monitoring.commcoverage.common.core;

public interface Consumer<T> {
    void accept(T message);
}
