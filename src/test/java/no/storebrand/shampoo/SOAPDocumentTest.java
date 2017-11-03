package no.storebrand.shampoo;

import io.vavr.control.Either;
import org.junit.Test;

import static org.junit.Assert.*;

public class SOAPDocumentTest {

    @Test
    public void soap12fault() throws Exception {
        Either<SoapFault, SoapDocument> doc = SoapDocument.fromStream(getClass().getResourceAsStream("/soap/soap12fault.xml"));
        assertTrue(doc.isLeft());
        SoapFault left = doc.getLeft();
        assertEquals("env:Receiver", left.code);
    }
}
