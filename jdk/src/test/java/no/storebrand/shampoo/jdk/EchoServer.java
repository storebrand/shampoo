package no.storebrand.shampoo.jdk;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.util.concurrent.ExecutorService;

@WebService(name ="Echo", targetNamespace = "http://echo", serviceName = "EchoService", portName = "EchoPort")
public class EchoServer {
    @WebMethod(operationName = "echo")
    @RequestWrapper(localName = "echoRequest", targetNamespace = "http://echo")
    @ResponseWrapper(localName = "echoResponse", targetNamespace = "http://echo")
    public String echo(@WebParam(name = "input", targetNamespace = "http://echo") String input) {
        return input;
    }

    public static Endpoint createEndpoint(int port, ExecutorService service) {
        Endpoint end = Endpoint.create(new EchoServer());
        end.setExecutor(service);
        end.publish(String.format("http://localhost:%s/", port));
        return end;
    }
}
