package no.storebrand.shampoo;

import okhttp3.MediaType;
import okio.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MTOM {
    public final SoapDocument document;
    public final List<Attachment> attachments;

    public MTOM(SoapDocument document, List<Attachment> attachments) {
        this.document = document;
        this.attachments = attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MTOM mtom = (MTOM) o;
        return Objects.equals(document, mtom.document) &&
                Objects.equals(attachments, mtom.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document, attachments);
    }

    public static Result<SoapFault, MTOM> fromInputStream(String contentType, InputStream inputStream) throws IOException {
        Map<String, String> parameters = parseContentTypeParameters(contentType);
        MultipartStream stream = new MultipartStream(inputStream, parameters.get("boundary").getBytes(StandardCharsets.UTF_8), MultipartStream.DEFAULT_BUFSIZE);

        Result<SoapFault, SoapDocument> parsed = Result.failure(SoapFault.parse("No XOP data found"));

        ArrayList<Attachment> list = new ArrayList<>();

        boolean nextPart = stream.skipPreamble();
        while (nextPart) {
            Map<String, List<String>> headers = parseHeaders(stream.readHeaders());
            String ctHeader = headers.get("content-type").get(0);
            String idHeader = headers.get("content-id").get(0);
            MediaType type = MediaType.parse(ctHeader);
            // create some output stream
            ByteString data = stream.readBody().readByteString();

            if (isCompatible(type, MediaType.parse("application/xop+xml"))) {
                parsed = SoapDocument.fromString(data.utf8());
            } else {
                list.add(new Attachment(new ContentId(idHeader), type, data));
            }
            nextPart = stream.readBoundary();
        }

        return parsed.map(doc -> new MTOM(doc, Collections.unmodifiableList(list)));
    }

    private static boolean isCompatible(MediaType type, MediaType toCheck) {
        return type.type().equals(toCheck.type()) && (type.subtype().equals("*") || type.subtype().equals(toCheck.subtype()));
    }

    private static Map<String, String> parseContentTypeParameters(String contentType) {
        Map<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("(\\w+)/([\\w+*]+);?(.*)");

        Matcher matcher = pattern.matcher(contentType);
        if (matcher.matches()) {
            String group = matcher.group(3);
            Scanner scanner = new Scanner(group).useDelimiter(";");
            while(scanner.hasNext()) {
                String next = scanner.next().trim();
                String[] parts = next.split("=", 2);
                if (parts.length == 1) {
                    map.put(parts[0].trim().toLowerCase(), "");
                } else if (parts.length == 2) {
                    map.put(parts[0].trim().toLowerCase(), parts[1].replace("\\\"", ""));
                }
            }
        }

        return map;
    }

    private static Map<String, List<String>> parseHeaders(String headers) {
        Map<String, List<String>> map = new HashMap<>();
        String[] split = headers.split("\\r\\n");
        for (String headerString : split) {
            String[] parts = headerString.trim().split(":", 2);
            if (parts.length == 2) {
                List<String> list = map.getOrDefault(parts[0].trim(), new ArrayList<>());
                list.add(parts[1].trim());
                map.put(parts[0].trim().toLowerCase(), list);
            }
        }
        return map;
    }
}
