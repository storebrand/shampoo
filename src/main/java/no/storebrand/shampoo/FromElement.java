package no.storebrand.shampoo;

import org.jdom2.Element;

import java.util.Optional;

@FunctionalInterface
public interface FromElement<A> {
    Optional<A> fromElement(Element element);

    static <A> FromElement<A> constant(A value) {
        return ignore -> Optional.ofNullable(value);
    }
}
