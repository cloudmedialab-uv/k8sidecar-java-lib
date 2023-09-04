package sidecar.java.lib;

@FunctionalInterface
public interface QuaFunction<T, U, V, R, L> {
  L apply(T t, U u, V v, R r);
}
