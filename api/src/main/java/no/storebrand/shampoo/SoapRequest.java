package no.storebrand.shampoo;

import java.util.*;

public final class SoapRequest {
    public final Map<String, String> headers;
    public final SoapDocument soapDocument;
    public final SoapAction action;

    private SoapRequest(Map<String, String> headers, SoapDocument document, SoapAction action) {
        this.headers = headers;
        this.action = action;
        this.soapDocument = document;
    }

    public static SoapRequest soap11(SoapBody body, List<SoapHeader> header, SoapAction action) {
        SoapDocument doc = SoapDocument.soap11(header, body);
        return new SoapRequest(Collections.emptyMap(), doc, action);
    }

    public static SoapRequest soap12(SoapBody body, List<SoapHeader> header, SoapAction action) {
        SoapDocument doc = SoapDocument.soap12(header, body);
        return new SoapRequest(Collections.emptyMap(), doc, action);
    }

    public SoapRequest setHttpHeader(String name, String value) {
        Map<String, String> copyHeaders = new HashMap<>(headers);
        if (value == null) {
            copyHeaders.remove(name);
        }
        else {
            copyHeaders.put(name, value);
        }

        return new SoapRequest(copyHeaders, soapDocument, action);
    }

    public SoapRequest removeHttpHeader(String name) {
        if (!headers.containsKey(name)) {
            return this;
        }
        Map<String, String> copyHeaders = new HashMap<>(headers);
        copyHeaders.remove(name);
        return new SoapRequest(copyHeaders, soapDocument, action);
    }
}
