package no.storebrand.shampoo;

import org.jdom2.Element;

import java.util.Optional;

public final class SoapHeader {
    public final Element element;

    public SoapHeader(Element element) {
        this.element = element;
    }

    public <A> Optional<A> transform(FromElement<A> f) {
        return f.fromElement(element);
    }

    public static <A> SoapHeader of(A a, ToElement<A> to) {
        return new SoapHeader(to.toElement(a));
    }
}
