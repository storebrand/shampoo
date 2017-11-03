package no.storebrand.shampoo;

import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;

import java.io.*;

import static no.storebrand.shampoo.JDOM2Utils.*;

public final class SoapDocument {
    public static final Namespace SOAP_11 = Namespace.getNamespace("soap11", "http://schemas.xmlsoap.org/soap/envelope/");
    public static final Namespace SOAP_12 = Namespace.getNamespace("soap12", "http://www.w3.org/2003/05/soap-envelope");

    public final List<SoapHeader> header;
    public final SoapBody body;
    public final Namespace ns;

    private SoapDocument(List<SoapHeader> header, SoapBody body, Namespace ns) {
        this.header = header;
        this.body = body;
        this.ns = ns;
    }

    public static SoapDocument soap11(List<SoapHeader> header, SoapBody body) {
        return new SoapDocument(header, body, SOAP_11);
    }

    public static SoapDocument soap12(List<SoapHeader> header, SoapBody body) {
        return new SoapDocument(header, body, SOAP_12);
    }

    public static Either<SoapFault, SoapDocument> fromString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Either.left(SoapFault.client("null or empty string"));
        }

        return fromReader(new StringReader(input));
    }

    public static Either<SoapFault, SoapDocument> fromStream(InputStream input) {
        return fromInputSource(new InputSource(input));
    }

    public static Either<SoapFault, SoapDocument> fromReader(Reader input) {
        return fromInputSource(new InputSource(input));
    }

    public static Either<SoapFault, SoapDocument> fromInputSource(InputSource input) {
        if (input == null) {
            return Either.left(SoapFault.client("null stream"));
        }
        try {
            Document doc = new SAXBuilder().build(input);
            return fromDocument(doc);
        } catch (Exception e) {
            return Either.left(SoapFault.parse(e.getMessage()));
        }
    }

    public static Either<SoapFault, SoapDocument> fromDocument(Document doc) {
        Namespace ns = doc.getRootElement().getNamespace();
        if (ns.equals(SOAP_11)) {
            return soap11(doc);
        }
        return soap12(doc);
    }

    private static Either<SoapFault, SoapDocument> soap11(Document doc) {
        Option<Element> maybeBodyish = getChild(doc.getRootElement(), "Body", SOAP_11);
        Option<Element> maybeHeaderIsh = getChild(doc.getRootElement(), "Header", SOAP_11);

        Option<SoapFault> maybeFault = maybeBodyish.flatMap(e -> getChild(e, "Fault", SOAP_11)).flatMap(e ->
                getChildText(e, "faultcode").map(code -> {
                    return new SoapFault(code, getChildText(e, "faultstring").getOrElse(""), getChild(e, "detail").flatMap(SoapDocument::faultDetail));
                })
        );

        if (maybeFault.isDefined()) {
            return Either.left(maybeFault.get());
        }

        List<SoapHeader> maybeHeader = maybeHeaderIsh.map(e -> e.getChildren().stream().map(SoapHeader::new).collect(List.collector())).getOrElse(List.empty());
        Option<SoapBody> maybeBody = maybeBodyish.flatMap(JDOM2Utils::firstChild).map(SoapBody::new);

        if (maybeBody.isEmpty() && maybeHeader.isEmpty()) {
            return Either.left(SoapFault.client("Both soap:Header and soap:Body is empty."));
        }
        return maybeBody.map(b -> new SoapDocument(maybeHeader, b, SOAP_11)).toRight(SoapFault.parse("Missing Body"));
    }

    private static Option<String> faultDetail(Element detail) {
        return firstChild(detail).map(child -> new XMLOutputter(Format.getPrettyFormat()).outputString(child)).orElse(getText(detail));
    }

    private static Either<SoapFault, SoapDocument> soap12(Document doc) {
        Option<Element> maybeBodyish = getChild(doc.getRootElement(), "Body", SOAP_12);

        Option<SoapFault> maybeFault = maybeBodyish.flatMap(e -> getChild(e, "Fault", SOAP_12)).flatMap(e ->
                        getGrandChildText(e, "Code", "Value", SOAP_12).map(code -> new SoapFault(code, getChildText(e, "Reason", SOAP_12).getOrElse(""), getChild(e, "Detail", SOAP_12).flatMap(SoapDocument::faultDetail)))
        );

        if (maybeFault.isDefined()) {
            return Either.left(maybeFault.get());
        }

        List<SoapHeader> maybeHeader = getChild(doc.getRootElement(), "Header", SOAP_12).map(e -> e.getChildren().stream().map(SoapHeader::new).collect(List.collector())).getOrElse(List.empty());
        Option<SoapBody> maybeBody =  maybeBodyish.flatMap(JDOM2Utils::firstChild).map(SoapBody::new);

        if (maybeBody.isEmpty() && maybeHeader.isEmpty()) {
            return Either.left(SoapFault.client("Both soap12:Header and soap12:Body is empty."));
        }
        return maybeBody.map(b -> new SoapDocument(maybeHeader, b, SOAP_12)).toRight(SoapFault.parse("Missing Body"));
    }

    public void write(OutputStream stream) throws IOException {
        write(stream, Format.getCompactFormat());
    }

    public void write(OutputStream stream, Format format) throws IOException {
        new XMLOutputter(format).output(toXML(), stream);
    }

    public Document toXML() {
        return new Document(elem("Envelope", ns,
                elem("Header", ns, header.map(h -> h.element.detach()).toJavaList()),
                elem("Body", ns, body.body.detach())
        ));
    }

    public String toXMLString(Format format) {
        return new XMLOutputter(format).outputString(toXML());
    }

    public String toCompactString() {
        return toXMLString(Format.getCompactFormat());
    }

    @Override
    public String toString() {
        return toXMLString(Format.getPrettyFormat());
    }

    public <A> Option<A> transform(FromElement<A> f) {
        return body.transform(f);
    }
}
