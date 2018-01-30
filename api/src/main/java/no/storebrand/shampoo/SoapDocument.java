package no.storebrand.shampoo;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public static Result<SoapFault, SoapDocument> fromString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Result.failure(SoapFault.client("null or empty string"));
        }

        return fromReader(new StringReader(input));
    }

    public static Result<SoapFault, SoapDocument> fromStream(InputStream input) {
        if (input == null) {
            return Result.failure(SoapFault.client("null stream"));
        }
        return fromInputSource(new InputSource(input));
    }

    public static Result<SoapFault, SoapDocument> fromReader(Reader input) {
        if (input == null) {
            return Result.failure(SoapFault.client("null stream"));
        }
        return fromInputSource(new InputSource(input));
    }

    public static Result<SoapFault, SoapDocument> fromInputSource(InputSource input) {
        if (input == null) {
            return Result.failure(SoapFault.client("null stream"));
        }
        try {
            Document doc = new SAXBuilder().build(input);
            return fromDocument(doc);
        } catch (Exception e) {
            return Result.failure(SoapFault.parse(e.getMessage()));
        }
    }

    public static Result<SoapFault, SoapDocument> fromDocument(Document doc) {
        Namespace ns = doc.getRootElement().getNamespace();
        if (ns.equals(SOAP_11)) {
            return soap11(doc);
        }
        return soap12(doc);
    }

    private static Result<SoapFault, SoapDocument> soap11(Document doc) {
        Optional<Element> maybeBodyish = getChild(doc.getRootElement(), "Body", SOAP_11);
        Optional<Element> maybeHeaderIsh = getChild(doc.getRootElement(), "Header", SOAP_11);

        Optional<SoapFault> maybeFault = maybeBodyish.flatMap(e -> getChild(e, "Fault", SOAP_11)).flatMap(e ->
                getChildText(e, "faultcode").map(code -> {
                    return new SoapFault(code, getChildText(e, "faultstring").orElse(""), getChild(e, "detail").flatMap(SoapDocument::faultDetail));
                })
        );

        if (maybeFault.isPresent()) {
            return Result.failure(maybeFault.get());
        }

        List<SoapHeader> maybeHeader = maybeHeaderIsh.map(e -> e.getChildren().stream().map(SoapHeader::new).collect(Collectors.toList())).orElse(Collections.emptyList());
        Optional<SoapBody> maybeBody = maybeBodyish.flatMap(JDOM2Utils::firstChild).map(SoapBody::new);

        if (!maybeBody.isPresent() && maybeHeader.isEmpty()) {
            return Result.failure(SoapFault.client("Both soap:Header and soap:Body is empty."));
        }
        return Result.fromOptional(maybeBody.map(b -> new SoapDocument(maybeHeader, b, SOAP_11)), () -> SoapFault.parse("Missing Body"));
    }

    private static Optional<String> faultDetail(Element detail) {
        Optional<String> maybeDetail = firstChild(detail).map(child -> new XMLOutputter(Format.getPrettyFormat()).outputString(child));
        return maybeDetail.isPresent() ? maybeDetail : getText(detail);
    }

    private static Result<SoapFault, SoapDocument> soap12(Document doc) {
        Optional<Element> maybeBodyish = getChild(doc.getRootElement(), "Body", SOAP_12);

        Optional<SoapFault> maybeFault = maybeBodyish.flatMap(e -> getChild(e, "Fault", SOAP_12)).flatMap(e ->
                        getGrandChildText(e, "Code", "Value", SOAP_12).map(code -> new SoapFault(code, getChildText(e, "Reason", SOAP_12).orElse(""), getChild(e, "Detail", SOAP_12).flatMap(SoapDocument::faultDetail)))
        );

        if (maybeFault.isPresent()) {
            return Result.failure(maybeFault.get());
        }

        List<SoapHeader> maybeHeader = getChild(doc.getRootElement(), "Header", SOAP_12).map(e -> e.getChildren().stream().map(SoapHeader::new).collect(Collectors.toList())).orElse(Collections.emptyList());
        Optional<SoapBody> maybeBody =  maybeBodyish.flatMap(JDOM2Utils::firstChild).map(SoapBody::new);

        if (!maybeBody.isPresent() && maybeHeader.isEmpty()) {
            return Result.failure(SoapFault.client("Both soap12:Header and soap12:Body is empty."));
        }
        return Result.fromOptional(maybeBody.map(b -> new SoapDocument(maybeHeader, b, SOAP_12)), () -> SoapFault.parse("Missing Body"));
    }

    public void write(OutputStream stream) throws IOException {
        write(stream, Format.getCompactFormat());
    }

    public void write(OutputStream stream, Format format) throws IOException {
        new XMLOutputter(format).output(toXML(), stream);
    }

    public Document toXML() {
        return new Document(elem("Envelope", ns,
                elem("Header", ns, header.stream().map(h -> h.element.detach()).collect(Collectors.toList())),
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

    public <A> Optional<A> transform(FromElement<A> f) {
        return body.transform(f);
    }
}
