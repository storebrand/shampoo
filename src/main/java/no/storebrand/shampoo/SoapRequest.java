package no.storebrand.shampoo;

import io.vavr.collection.List;
import okhttp3.Headers;

public final class SoapRequest {
    public final Headers headers;
    public final SoapDocument soapDocument;
    public final SoapAction action;

    private SoapRequest(Headers headers, SoapDocument document, SoapAction action) {
        this.headers = headers;
        this.action = action;
        this.soapDocument = document;
    }

    public static SoapRequest soap11(SoapBody body, List<SoapHeader> header, SoapAction action) {
        SoapDocument doc = SoapDocument.soap11(header, body);
        return new SoapRequest(Headers.of(), doc, action);
    }

    public static SoapRequest soap12(SoapBody body, List<SoapHeader> header, SoapAction action) {
        SoapDocument doc = SoapDocument.soap12(header, body);
        return new SoapRequest(Headers.of(), doc, action);
    }

    public SoapRequest addHttpHeader(String name, String value) {
        return new SoapRequest(headers.newBuilder().add(name, value).build(), soapDocument, action);
    }
}
