package no.storebrand.shampoo;

import io.vavr.control.Option;
import org.jdom2.Element;

public final class SoapBody {
    public final Element body;

    public SoapBody(Element body) {
        this.body = body;
    }
    
    public <A> Option<A> transform(FromElement<A> f) {
        return f.fromElement(body);
    }

    public static <A> SoapBody from(A a, ToElement<A> to) {
        return new SoapBody(to.toElement(a));
    }
}
