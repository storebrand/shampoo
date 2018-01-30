package no.storebrand.shampoo;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class Result<L, R> implements Iterable<R>, Serializable {
    private Result() {
    }

    public static <L, R> Result<L, R> failure(L value) {
        return new Failure<>(value);
    }

    public static <L, R> Result<L, R> success(R value) {
        return new Success<>(value);
    }

    public static <L extends Throwable, R> Result<L, R> fromTryCatch(Supplier<R> b, Class<L> ex) {
        try {
            return Result.success(b.get());
        } catch (Throwable t) {
            if (ex.isInstance(t))
                return Result.failure(ex.cast(t));
            else
                throw t;
        }
    }

    public static <L, R> Result<L, R> fromOptional(Optional<R> opt, Supplier<L> orLeft) {
        return opt.isPresent() ? success(opt.get()) : failure(orLeft.get());
    }

    public static <A> A merge(Result<A, A> either) {
        return either.fold(Function.identity(), Function.identity());
    }

    public Result<R, L> swap(){
        return fold(Result::<R, L>success, Result::<R, L>failure);
    }

    public <C, D> Result<C, D> bimap(Function<? super L, ? extends C> left, Function<? super R, ? extends D> right){
        return fold(left.andThen(Result::<C, D>failure), right.andThen(Result::<C, D>success));
    }

    public <C> Result<C, R> leftMap(Function<? super L, ? extends C> f){
        return fold(f.andThen(Result::<C, R>failure), Result::<C, R>success);
    }

    public boolean exists(Predicate<R> p){
        return fold((a) -> false, p::test);
    }

    public boolean forall(Predicate<R> p){
        return fold((a) -> true, p::test);
    }

    public List<R> toList(){
        return fold((a) -> Collections.<R>emptyList(), Collections::singletonList);
    }

    public abstract <B> B fold(Function<? super L, ? extends B> fLeft, Function<? super R, ? extends B> fRight);

    public <B> Result<L, B> map(Function<? super R, ? extends B> f) {
        return fold(Result::failure, f.andThen(Result::success));
    }

    public <B> Result<L, B> flatMap(Function<? super R, ? extends Result<L, B>> f) {
        return fold(Result::failure, f);
    }

    public R getOrElse(Supplier<? extends R> b){
        return fold((a) -> b.get(), Function.identity());
    }

    public R valueOr(Function<? super L, ? extends R> f){
        return fold(f, Function.identity());
    }

    public Result<L, R> orElse(Supplier<? extends Result<L, R>> next){
        return fold((a) -> next.get(), (b) -> this);
    }

    @Override
    public Iterator<R> iterator() {
        return toList().iterator();
    }

    public Optional<R> toOptional() {
        return fold(ignore -> Optional.empty(), Optional::<R>of);
    }

    public boolean isFailure() {
        return fold(ignore -> true, ignore -> false);
    }

    public boolean isSuccess() {
        return fold(ignore -> false, ignore -> true);
    }

    public final static class Success<L, R> extends Result<L, R> {
        public final R value;

        public Success(R value) {
            this.value = value;
        }

        @Override
        public <B> B fold(Function<? super L, ? extends B> fLeft, Function<? super R, ? extends B> fRight) {
            return fRight.apply(this.value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Success<?, ?> success = (Success<?, ?>) o;
            return Objects.equals(value, success.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public final static class Failure<L, R> extends Result<L, R> {
        public final L value;

        public Failure(L value) {
            this.value = value;
        }

        @Override
        public <B> B fold(Function<? super L, ? extends B> fLeft, Function<? super R, ? extends B> fRight) {
            return fLeft.apply(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Failure<?, ?> failure = (Failure<?, ?>) o;
            return Objects.equals(value, failure.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
