package no.storebrand.shampoo;

public interface SoapClient {
    Result<SoapFault, SoapDocument> execute(SoapRequest req);
}
