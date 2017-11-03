package no.storebrand.shampoo;

import io.vavr.Function2;
import io.vavr.Tuple2;
import org.jdom2.Element;

@FunctionalInterface
public interface ToElement<A> {
    Element toElement(A someType);

    static <A> ToElement<A> constant(Element element) {
        return ignore -> element;
    }

    static <A, B> ToElement<Tuple2<A, B>> from(Function2<A, B, Element> f) {
        return tuple -> f.tupled().apply(tuple);
    }
}
