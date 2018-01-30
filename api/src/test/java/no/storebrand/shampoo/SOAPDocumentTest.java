package no.storebrand.shampoo;

import org.jdom2.Namespace;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static no.storebrand.shampoo.JDOM2Utils.elem;
import static org.junit.Assert.*;

public class SOAPDocumentTest {

    @Test
    public void soap12fault() throws Exception {
        Result<SoapFault, SoapDocument> doc = SoapDocument.fromStream(getClass().getResourceAsStream("/soap/soap12fault.xml"));
        assertTrue(doc.isFailure());
        SoapFault left = doc.swap().getOrElse(() -> SoapFault.parse("Not a soap fault"));
        assertEquals("env:Receiver", left.code);
    }

    @Test
    public void soap12success() throws Exception {
        Result<SoapFault, SoapDocument> docResult = SoapDocument.fromStream(getClass().getResourceAsStream("/soap/soap12success.xml"));
        assertTrue(docResult.isSuccess());
        SoapBody soapBody = new SoapBody(elem("Echo", Namespace.getNamespace("http://example.org/Echo"), "Goodbye"));
        SoapDocument doc = docResult.getOrElse(() -> SoapDocument.soap12(Collections.emptyList(), soapBody));
        assertEquals(doc.ns, SoapDocument.SOAP_12);
        assertEquals(Optional.of("Hello"), doc.transform(JDOM2Utils::getText));
    }

    @Test
    public void soap11success() throws Exception {
        Result<SoapFault, SoapDocument> docResult = SoapDocument.fromStream(getClass().getResourceAsStream("/soap/soap11success.xml"));
        assertTrue(docResult.isSuccess());
        SoapBody soapBody = new SoapBody(elem("Echo", Namespace.getNamespace("http://example.org/Echo"), "Goodbye"));
        SoapDocument doc = docResult.getOrElse(() -> SoapDocument.soap11(Collections.emptyList(), soapBody));
        assertEquals(doc.ns, SoapDocument.SOAP_11);
        assertEquals(Optional.of("Hello"), doc.transform(JDOM2Utils::getText));
    }
}
