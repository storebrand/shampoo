package no.storebrand.shampoo.okhttp3;

import no.storebrand.shampoo.*;
import okhttp3.OkHttpClient;
import org.jdom2.Namespace;
import org.junit.Test;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static no.storebrand.shampoo.JDOM2Utils.elem;
import static org.junit.Assert.*;

public class SoapClientTest {

    public static int randomPort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            socket.close();
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to get a random open port");
        }
    }

    @Test
    public void echoService() throws Exception {
        ExecutorService service = Executors.newSingleThreadExecutor();
        int port = randomPort();
        Endpoint endpoint = EchoServer.createEndpoint(port, service);
        OkHttpClient httpClient = new OkHttpClient();

        SoapClient client = new OkHttp3SoapClient(httpClient, URI.create(String.format("http://localhost:%s", port)));
        Namespace ns = Namespace.getNamespace("http://echo");
        try {
            ToElement<String> toEchoRequest = s -> elem("echoRequest", ns, elem("input", ns, s));
            Result<SoapFault, SoapDocument> responseOrError = client.execute(SoapRequest.soap11(
                    SoapBody.from("Hello", toEchoRequest),
                    Collections.emptyList(),
                    SoapAction.of("http://echo/Echo/echo")
            ));
            responseOrError.fold(fault -> {
                System.out.println("fault = " + fault);
                fail(fault.message);
                return null;
            }, success -> {
                Optional<String> maybeString = success.transform(e -> JDOM2Utils.getChildText(e, "return"));
                assertTrue("Did not match string", maybeString.isPresent());
                assertEquals("Hello", maybeString.get());
                return null;
            });
        } finally {
            endpoint.stop();
            service.shutdown();
            httpClient.dispatcher().executorService().shutdown();
        }
    }
}
