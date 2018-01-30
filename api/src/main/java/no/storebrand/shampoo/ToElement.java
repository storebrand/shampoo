package no.storebrand.shampoo;

import org.jdom2.Element;

@FunctionalInterface
public interface ToElement<A> {
    Element toElement(A someType);

    static <A> ToElement<A> constant(Element element) {
        return ignore -> element;
    }
}
