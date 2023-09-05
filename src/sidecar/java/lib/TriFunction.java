package sidecar.java.lib;

/**
 * A functional interface representing a function that accepts three arguments and produces a result.
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
  R apply(T t, U u, V v);
}
