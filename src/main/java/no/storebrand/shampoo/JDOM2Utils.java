package no.storebrand.shampoo;

import org.jdom2.*;
import org.jdom2.xpath.XPathExpression;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JDOM2Utils {
    private JDOM2Utils() {
    }

    public static Optional<Element> getChild(Element parent, String name) {
        return Optional.ofNullable(parent.getChild(name));
    }

    public static Optional<String> getText(Element parent) {
        return Optional.ofNullable(parent.getTextTrim()).filter(s -> !s.isEmpty());
    }

    public static Optional<String> evalFirstText(XPathExpression<Element> expr, Document doc) {
        return Optional.ofNullable(expr.evaluateFirst(doc)).flatMap(JDOM2Utils::getText);
    }

    public static Optional<Element> evalFirst(XPathExpression<Element> expr, Document doc) {
        return Optional.ofNullable(expr.evaluateFirst(doc));
    }

    public static <A> Optional<A> flatMapEvalFirst(XPathExpression<Element> expr, Document doc, Function<Element, Optional<A>> f) {
        return evalFirst(expr, doc).flatMap(f);
    }

    public static Optional<String> getChildText(Element parent, String name) {
        return Optional.ofNullable(parent.getChildTextTrim(name)).filter(s -> !s.isEmpty());
    }

    public static Optional<Integer> getChildTextAsInteger(Element parent, String name) {
        return flatMapChildText(parent, name, s -> Result.fromTryCatch(() -> Integer.parseInt(s), NumberFormatException.class).toOptional());
    }

    public static Optional<Integer> getChildTextAsInteger(Element parent, String name, Namespace ns) {
        return flatMapChildText(parent, name, ns, s -> Result.fromTryCatch(() -> Integer.parseInt(s), NumberFormatException.class).toOptional());
    }

    public static <A> Optional<A> mapChildText(Element parent, String name, Function<String, A> f) {
        return getChildText(parent, name).map(f);
    }

    public static <A> Optional<A> mapChildText(Element parent, String name, Namespace ns, Function<String, A> f) {
        return getChildText(parent, name, ns).map(f);
    }

    public static <A> Optional<A> flatMapChildText(Element parent, String name, Function<String, Optional<A>> f) {
        return getChildText(parent, name).flatMap(f);
    }

    public static <A> Optional<A> flatMapChildText(Element parent, String name, Namespace ns, Function<String, Optional<A>> f) {
        return getChildText(parent, name, ns).flatMap(f);
    }

    public static Optional<String> getChildText(Element parent, String name, Namespace ns) {
        return Optional.ofNullable(parent.getChildTextTrim(name, ns)).filter(s -> !s.isEmpty());
    }

    public static Optional<Element> getChild(Element parent, String name, Namespace ns) {
        return Optional.ofNullable(parent.getChild(name, ns));
    }

    public static Optional<Element> getGrandChild(Element parent, String name, String childName, Namespace ns) {
        return getChild(parent, name, ns).flatMap(e -> getChild(e, childName, ns));
    }

    public static Optional<Element> getGrandChild(Element parent, String name, String childName) {
        return getChild(parent, name).flatMap(e -> getChild(e, childName));
    }

    public static Optional<String> getGrandChildText(Element parent, String name, String childName, Namespace ns) {
        return getChild(parent, name, ns).flatMap(e -> getChildText(e, childName, ns));
    }

    public static <A> Optional<A> flatMapGrandChildText(Element parent, String name, String childName, Namespace ns, Function<String, Optional<A>> f) {
        return getGrandChildText(parent, name, childName, ns).flatMap(f);
    }

    public static <A> Optional<A> flatMapGrandChildText(Element parent, String name, String childName, Function<String, Optional<A>> f) {
        return getGrandChildText(parent, name, childName).flatMap(f);
    }

    public static <A> Optional<A> mapGrandChildText(Element parent, String name, String childName, Namespace ns, Function<String, A> f) {
        return getGrandChildText(parent, name, childName, ns).map(f);
    }

    public static <A> Optional<A> mapGrandChildText(Element parent, String name, String childName, Function<String, A> f) {
        return getGrandChildText(parent, name, childName).map(f);
    }

    public static Optional<String> getGrandChildText(Element parent, String name, String childName) {
        return getChild(parent, name).flatMap(e -> getChildText(e, childName));
    }

    public static Optional<Element> firstChild(Element element) {
        return element.getChildren().stream().findFirst();
    }

    public static Optional<BigDecimal> getChildTextAsBigDecimal(Element element, String name) {
        return flatMapChildText(element, name, s -> Result.fromTryCatch(() -> new BigDecimal(s), NumberFormatException.class).toOptional());
    }

    public static Optional<BigDecimal> getChildTextAsBigDecimal(Element element, String name, Namespace ns) {
        return flatMapChildText(element, name, ns, s -> Result.fromTryCatch(() -> new BigDecimal(s), NumberFormatException.class).toOptional());
    }

    public static <A> Optional<List<A>> mapChildrenOf(Element element, String name, Function<Element, Optional<A>> f) {
        return getChild(element, name).map(e -> e.getChildren().stream().flatMap(item -> toStream(f.apply(item))).collect(Collectors.toList()));
    }

    private static <A> Stream<A> toStream(Optional<A> opt) {
        return opt.isPresent() ? Stream.of(opt.get()) : Stream.empty();
    }

    public static <A> Optional<List<A>> mapChildrenOf(Element element, String name, Namespace ns, Function<Element, Optional<A>> f) {
        return getChild(element, name, ns).map(e -> e.getChildren().stream().flatMap(item -> toStream(f.apply(item))).collect(Collectors.toList()));
    }

    public static <A> Optional<List<A>> mapChildren(Element element, String name, Function<Element, Optional<A>> f) {
        return Optional.ofNullable(element.getChildren(name)).map(list -> list.stream().flatMap(item -> toStream(f.apply(item))).collect(Collectors.toList()));
    }

    public static <A> Optional<List<A>> mapChildren(Element element, String name, Namespace ns, Function<Element, Optional<A>> f) {
        return Optional.ofNullable(element.getChildren(name, ns)).map(list -> list.stream().flatMap(item -> toStream(f.apply(item))).collect(Collectors.toList()));
    }

    public static Element elem(String name, String value) {
        return new Element(name).setText(value);
    }


    public static Element elem(String name, Namespace ns, String value) {
        return new Element(name, ns).setText(value);
    }

    public static Element elem(String name, Optional<String> value) {
        return elem(name, value.orElse(null));
    }

    public static Element elem(String name, Namespace ns, Optional<String> value) {
        return elem(name, ns, value.orElse(null));
    }

    public static Element elem(String name, Element... content) {
        return elem(name, Arrays.asList(content));
    }

    public static Element elem(String name, Namespace ns, Element... content) {
        return elem(name, ns, Arrays.asList(content));
    }

    public static Element elem(String name, Namespace ns, Iterable<Namespace> additional, Element... content) {
        return elem(name, ns, additional, Arrays.asList(content));
    }

    public static <C extends Content> Element elem(String name, Iterable<C> content) {
        Element element = new Element(name);
        for (C e : content) {
            element.addContent(e);
        }
        return element;
    }

    public static <C extends Content> Element elem(String name, Namespace ns, Iterable<C> content) {
        Element element = new Element(name, ns);
        for (C e : content) {
            element.addContent(e);
        }
        return element;
    }

    public static <C extends Content> Element elem(String name, Namespace ns, Iterable<Namespace> additional, Iterable<C> content) {
        Element element = new Element(name, ns);
        for (Namespace namespace : additional) {
            element.addNamespaceDeclaration(namespace);
        }
        for (C e : content) {
            element.addContent(e);
        }
        return element;
    }
}
