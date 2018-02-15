package no.storebrand.shampoo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SoapActionTest {

    @Test
    public void Empty() {
        assertEquals(SoapAction.of(""), SoapAction.empty);
        assertEquals(SoapAction.of(" "), SoapAction.empty);
        assertEquals(SoapAction.of(null), SoapAction.empty);
    }

    @Test
    public void other() {
        SoapAction action = SoapAction.of("example");

        assertEquals("example", action.action);
    }

    @Test
    public void formatted() {
        SoapAction action = SoapAction.of("example");

        assertEquals("\"example\"", action.format());
    }
}
