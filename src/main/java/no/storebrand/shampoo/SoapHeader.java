package no.storebrand.shampoo;

import io.vavr.control.Option;
import org.jdom2.Element;

public final class SoapHeader {
    public final Element element;

    public SoapHeader(Element element) {
        this.element = element;
    }

    public <A> Option<A> transform(FromElement<A> f) {
        return f.fromElement(element);
    }

    public static <A> SoapHeader of(A a, ToElement<A> to) {
        return new SoapHeader(to.toElement(a));
    }
}
