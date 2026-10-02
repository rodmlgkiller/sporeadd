package com.sporeadds.sporeaddsmod.capabilities;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Minimal replacement for Forge's LazyOptional (removed in NeoForge), covering the API the mod uses. */
public final class LazyOptional<T> {

    private static final LazyOptional<?> EMPTY = new LazyOptional<>(null);

    private final Supplier<T> supplier;

    private LazyOptional(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazyOptional<T> of(Supplier<T> supplier) {
        return new LazyOptional<>(supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> empty() {
        return (LazyOptional<T>) EMPTY;
    }

    public boolean isPresent() {
        return supplier != null && supplier.get() != null;
    }

    public void ifPresent(Consumer<? super T> action) {
        T value = supplier == null ? null : supplier.get();
        if (value != null) {
            action.accept(value);
        }
    }

    public <U> LazyOptional<U> map(Function<? super T, ? extends U> mapper) {
        T value = supplier == null ? null : supplier.get();
        if (value == null) {
            return empty();
        }
        U mapped = mapper.apply(value);
        return mapped == null ? empty() : new LazyOptional<>(() -> mapped);
    }

    public LazyOptional<T> filter(Predicate<? super T> predicate) {
        T value = supplier == null ? null : supplier.get();
        return value != null && predicate.test(value) ? this : empty();
    }

    public T orElse(T other) {
        T value = supplier == null ? null : supplier.get();
        return value != null ? value : other;
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        T value = supplier == null ? null : supplier.get();
        if (value == null) {
            throw exceptionSupplier.get();
        }
        return value;
    }

    public T orElseThrow() {
        return orElseThrow(() -> new java.util.NoSuchElementException("No value present"));
    }

    public Optional<T> resolve() {
        return Optional.ofNullable(supplier == null ? null : supplier.get());
    }

    @SuppressWarnings("unchecked")
    public <R> LazyOptional<R> cast() {
        return (LazyOptional<R>) this;
    }

    public void invalidate() {
    }

    public void addListener(Consumer<LazyOptional<T>> listener) {
    }
}
