package no.storebrand.shampoo;

import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResultTest {

    @Test
    public void right() {
        Result<Object, String> hello = Result.success("Hello");
        Result<Exception, String> hello2 = Result.success("Hello");
        assertTrue("Was not success", hello.isSuccess());
        assertTrue("Was not success", hello2.isSuccess());
        assertEquals(hello, hello2);
    }

    @Test
    public void left() {
        Result<String, String> hello = Result.failure("Hello");
        Result<String, String> hello2 = Result.failure("Hello");
        assertTrue("Was not failure", hello.isFailure());
        assertFalse("Was not failure", hello.isSuccess());
        assertTrue("Was not failure", hello2.isFailure());
        assertFalse("Was not failure", hello2.isSuccess());

        assertEquals(hello, hello2);
    }

    @Test
    public void map() {
        Result<Object, String> one = Result.success("1");
        assertEquals(Result.success(1), one.map(Integer::valueOf));
        assertEquals(one, one.map(Function.identity()));
        assertEquals(one, one.map(Integer::valueOf).map(String::valueOf));
        Function<String, Integer> toInt = Integer::valueOf;
        assertEquals(one, one.map(toInt.andThen(String::valueOf)));
    }

    @Test
    public void fromTryCatch() {
        Result<Exception, String> one = Result.success("1");
        Result<Exception, String> hello = Result.success("hello");

        Function<String, Result<Exception, Integer>> parser =
                s -> Result.fromTryCatch(() -> Integer.parseInt(s), Exception.class);

        Result<Exception, Integer> parsed = one.flatMap(parser);
        assertEquals(Result.success(1), parsed);
        Result<Exception, Integer> parsedFail = hello.flatMap(parser);
        Exception ex = parsedFail.swap().getOrElse(RuntimeException::new);
        assertTrue(ex instanceof NumberFormatException);
    }

    @Test
    public void merge() {
        Result<String, String> one = Result.success("1");
        Result<String, String> hello = Result.failure("hello");

        assertEquals("1", Result.merge(one));
        assertEquals("hello", Result.merge(hello));
    }


    @Test
    public void optional() {
        Result<String, String> one = Result.fromOptional(Optional.of("1"), () -> "2");
        Result<String, String> two = Result.fromOptional(Optional.empty(), () -> "2");

        assertEquals("1", Result.merge(one));
        assertEquals("2", Result.merge(two));
        assertEquals(Optional.of("1"), one.toOptional());
        assertEquals(Optional.empty(), two.toOptional());
        assertEquals(Optional.of("2"), two.swap().toOptional());
    }

    @Test
    public void toList() {
        Result<String, String> one = Result.success("1");
        assertEquals(Arrays.asList("1"), one.toList());
    }

    @Test
    public void orElse() {
        Result<String, String> one = Result.success("1");
        Result<String, String> two = Result.failure("2");

        assertEquals(one, one.orElse(() -> two));
        assertEquals(one, two.orElse(() -> one));
    }

    @Test
    public void leftMap() {
        Result<String, String> two = Result.failure("2");
        Result<Integer, String> oneIntFail = two.leftMap(Integer::parseInt);

        assertTrue(oneIntFail.isFailure());
        assertEquals(Integer.valueOf(2), oneIntFail.swap().toOptional().get());
    }

    @Test
    public void biMap() {
        Result<Object, String> two = Result.success("2");
        Result<Object, String> one = Result.failure("1");
        assertEquals("2", Result.merge(two.bimap(Object::toString, Function.identity())));
        assertEquals("1", Result.merge(one.bimap(Object::toString, Function.identity())));
    }

    @Test
    public void exists() {
        Predicate<String> p = s -> s.matches("\\d+");
        Predicate<String> nonWord = s -> s.matches("\\W+");

        Result<Object, String> two = Result.success("2232");
        Result<Object, String> one = Result.failure("bambus");
        assertTrue(two.exists(p));
        assertFalse(two.exists(nonWord));
        assertFalse(one.exists(p));
    }

    @Test
    public void forall() {
        Predicate<String> p = s -> s.matches("\\d+");
        Predicate<String> nonWord = s -> s.matches("\\W+");

        Result<Object, String> two = Result.success("2232");
        Result<Object, String> one = Result.failure("bambus");
        assertTrue(two.forall(p));
        assertFalse(two.forall(nonWord));
        assertTrue(one.forall(p));
    }
}
