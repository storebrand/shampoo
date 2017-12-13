package no.storebrand.shampoo;

import io.vavr.control.Option;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class SoapFault {
    public final String code;
    public final String message;
    public final Option<String> detail;

    public SoapFault(String code, String message, Option<String> detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public static SoapFault client(String message) {
        return new SoapFault("soap:Client", message, Option.none());
    }

    public static SoapFault server(String message) {
        return new SoapFault("soap:Server", message, Option.none());
    }

    public static SoapFault parse(String message) {
        return new SoapFault("parse", message, Option.none());
    }

    public static SoapFault exception(String code, Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        writer.flush();
        return new SoapFault(code, Option.of(e.getMessage()).getOrElse(e.getClass().getName()), Option.some(writer.toString()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoapFault soapFault = (SoapFault) o;

        if (!code.equals(soapFault.code)) return false;
        if (!message.equals(soapFault.message)) return false;
        return detail.equals(soapFault.detail);
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + message.hashCode();
        result = 31 * result + detail.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SoapFault{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", detail=" + detail.getOrElse("") +
                '}';
    }
}
