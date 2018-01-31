package no.storebrand.shampoo.jdk;

import no.storebrand.shampoo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class UrlConnectionSoapClient implements SoapClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private URL url;

    public UrlConnectionSoapClient(URI url) {
        try {
            this.url = url.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Not a valid URL", e);
        }
    }

    @Override
    public Result<SoapFault, SoapDocument> execute(SoapRequest req) {
        if (logger.isInfoEnabled()) {
            logger.info("SOAP request to {} ", url);
            logger.info("Action is '{}'", req.action.action);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("request is:\n{}", req.soapDocument);
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            req.headers.forEach(conn::setRequestProperty);
            conn.setRequestProperty("SOAPAction", req.action.format());
            conn.setRequestProperty("Content-Type", toMediaType(req.soapDocument));
            req.soapDocument.write(conn.getOutputStream());
            conn.connect();

            try(InputStream is = getInputStream(conn)) {
                if (is != null) {
                    return SoapDocument.fromStream(is);
                } else {
                    return Result.failure(SoapFault.client("No inputstream found in client"));
                }
            }
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("exception in soap request", e);
            }
            return Result.failure(SoapFault.client(e.getMessage()));
        }
    }

    private InputStream getInputStream(HttpURLConnection conn) throws IOException {
        if (conn.getResponseCode() >= 200 || conn.getResponseCode() < 300) {
            return conn.getInputStream();
        } else {
            return conn.getErrorStream();
        }
    }

    private String toMediaType(SoapDocument doc) {
        if (doc.ns.equals(SoapDocument.SOAP_12)) {
            return "application/soap+xml";
        }
        return "text/xml; charset=utf-8";
    }

}
