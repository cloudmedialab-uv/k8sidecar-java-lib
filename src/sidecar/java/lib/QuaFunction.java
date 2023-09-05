package sidecar.java.lib;

/**
 * A functional interface representing a function that accepts four arguments and produces a result.
 */
@FunctionalInterface
public interface QuaFunction<T, U, V, R, L> {
  L apply(T t, U u, V v, R r);
}
