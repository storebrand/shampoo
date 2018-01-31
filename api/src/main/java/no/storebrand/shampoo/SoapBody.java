package no.storebrand.shampoo;

import org.jdom2.Element;

import java.util.Optional;

public final class SoapBody {
    public final Element body;

    public SoapBody(Element body) {
        this.body = body;
    }
    
    public <A> Optional<A> transform(FromElement<A> f) {
        return f.fromElement(body);
    }

    public static <A> SoapBody from(A a, ToElement<A> to) {
        return new SoapBody(to.toElement(a));
    }
}
