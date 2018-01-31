package no.storebrand.shampoo;

import org.junit.Test;

import static org.junit.Assert.*;

public class SOAPDocumentTest {

    @Test
    public void soap12fault() throws Exception {
        Result<SoapFault, SoapDocument> doc = SoapDocument.fromStream(getClass().getResourceAsStream("/soap/soap12fault.xml"));
        assertTrue(doc.isFailure());
        SoapFault left = doc.swap().getOrElse(() -> SoapFault.parse("Not a soap fault"));
        assertEquals("env:Receiver", left.code);
    }
}
