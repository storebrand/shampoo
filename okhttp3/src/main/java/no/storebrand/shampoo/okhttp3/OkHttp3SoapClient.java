package no.storebrand.shampoo.okhttp3;

import no.storebrand.shampoo.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

public final class OkHttp3SoapClient implements SoapClient {
    private final Call.Factory client;
    private final URI apiURI;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public OkHttp3SoapClient(Call.Factory client, URI apiURI) {
        this.client = client;
        this.apiURI = apiURI;
    }

    @Override
    public Result<SoapFault, SoapDocument> execute(SoapRequest req) {
        return request(req, (contentType, rb) -> soapDoc(req, rb));
    }

    public Result<SoapFault, MTOM> executeMTOM(SoapRequest req) {
        return request(req, (contentType, rb) -> {
            if ("multipart".equals(rb.contentType().type())) {
                return MTOM.fromInputStream(contentType, rb.byteStream());
            } else {
                return soapDoc(req, rb).map(doc -> new MTOM(doc, Collections.emptyList()));
            }
        });
    }

    private Result<SoapFault, SoapDocument> soapDoc(SoapRequest req, ResponseBody rb) throws IOException {
        if (exists(Optional.ofNullable(rb.contentType()), (ct -> ct.subtype().contains("xml")))) {
            if (logger.isDebugEnabled()) {
                String data = rb.string();
                logger.debug("response data is:\n{}", data);
                return SoapDocument.fromString(data);
            } else {
                return SoapDocument.fromStream(rb.byteStream());
            }
        } else {
            if (logger.isDebugEnabled()) {
                String data = rb.string();
                logger.debug("response data is:\n{}", data);
            }
            return Result.failure(SoapFault.server("Not XML from " + req.action.action));
        }
    }

    private <A> Result<SoapFault, A> request(SoapRequest req, IOFunction<String, ResponseBody, Result<SoapFault, A>> fromBody) {
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
                    .headers(Headers.of(req.headers))
                    .addHeader("SOAPAction", req.action.format())
                    .post(RequestBody.create(toMediaType(req.soapDocument), req.soapDocument.toCompactString()))
                    .build();

            Call call = client.newCall(request);
            Response resp = call.execute();
            try (ResponseBody body = resp.body()) {
                return fromBody.apply(resp.header("Content-Type"), body);
            }
        } catch (RuntimeException e) {
            return Result.failure(SoapFault.exception("soap:Client", e));
        } catch (Exception e) {
            return Result.failure(SoapFault.exception("soap:Server", e));
        }
    }

    private static <T> boolean exists(Optional<T> opt, Predicate<T> predicate) {
        return opt.isPresent() && predicate.test(opt.get());
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
