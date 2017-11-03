package no.storebrand.shampoo;

import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public final class SoapClient {
    private final Call.Factory client;
    private final URI apiURI;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SoapClient(Call.Factory client, URI apiURI) {
        this.client = client;
        this.apiURI = apiURI;
    }

    public Either<SoapFault, SoapDocument> execute(SoapRequest req) {
        return request(req, (contentType, rb) -> soapDoc(req, rb));
    }

    public Either<SoapFault, MTOM> executeMTOM(SoapRequest req) {
        return request(req, (contentType, rb) -> {
            if ("multipart".equals(rb.contentType().type())) {
                return MTOM.fromInputStream(contentType, rb.byteStream());
            } else {
                return soapDoc(req, rb).map(doc -> new MTOM(doc, List.empty()));
            }
        });
    }

    private Either<SoapFault, SoapDocument> soapDoc(SoapRequest req, ResponseBody rb) throws IOException {
        String data = rb.string();
        if (Option.of(rb.contentType()).exists(ct -> ct.subtype().contains("xml"))) {
            if (logger.isDebugEnabled()) {
                logger.debug("response is:\n{}", data);
            }
            return SoapDocument.fromString(data);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("response data is:\n{}", data);
            }
            return Either.left(SoapFault.server("Not XML from " + req.action.action));
        }
    }

    private <A> Either<SoapFault, A> request(SoapRequest req, IOFunction<String, ResponseBody, Either<SoapFault, A>> fromBody) {
        if (logger.isInfoEnabled()) {
            logger.info("SOAP request to {} ", apiURI);
            logger.info("Action is '{}'", req.action.action);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("request is:\n{}", req.soapDocument);
        }
        try {
            Request request = new Request.Builder()
                    .url(HttpUrl.get(apiURI))
                    .headers(req.headers)
                    .addHeader("SOAPAction", req.action.format())
                    .post(RequestBody.create(toMediaType(req.soapDocument), req.soapDocument.toCompactString()))
                    .build();

            Call call = client.newCall(request);
            Response resp = call.execute();
            try (ResponseBody body = resp.body()) {
                return fromBody.apply(resp.header("Content-Type"), body);
            }
        } catch (RuntimeException e) {
            return Either.left(SoapFault.exception("soap:Client", e));
        } catch (Exception e) {
            return Either.left(SoapFault.exception("soap:Server", e));
        }
    }

    private MediaType toMediaType(SoapDocument doc) {
        if (doc.ns.equals(SoapDocument.SOAP_12)) {
            return MediaType.parse("application/soap+xml");
        }
        return MediaType.parse("text/xml; charset=utf-8");
    }

    private interface IOFunction<A, B, C> {
        C apply(A arg1, B arg2) throws IOException;
    }
}
