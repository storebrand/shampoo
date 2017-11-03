package no.storebrand.shampoo;

import io.vavr.control.Option;
import org.jdom2.Element;

@FunctionalInterface
public interface FromElement<A> {
    Option<A> fromElement(Element element);

    static <A> FromElement<A> constant(A value) {
        return ignore -> Option.some(value);
    }
}
