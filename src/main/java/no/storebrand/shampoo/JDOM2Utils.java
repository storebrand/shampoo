package no.storebrand.shampoo;

import io.vavr.collection.List;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jdom2.*;
import org.jdom2.xpath.XPathExpression;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Function;

public final class JDOM2Utils {
    private JDOM2Utils() {
    }

    public static Option<Element> getChild(Element parent, String name) {
        return Option.of(parent.getChild(name));
    }

    public static Option<String> getText(Element parent) {
        return Option.of(parent.getTextTrim()).filter(s -> !s.isEmpty());
    }

    public static Option<String> evalFirstText(XPathExpression<Element> expr, Document doc) {
        return Option.of(expr.evaluateFirst(doc)).flatMap(JDOM2Utils::getText);
    }

    public static Option<Element> evalFirst(XPathExpression<Element> expr, Document doc) {
        return Option.of(expr.evaluateFirst(doc));
    }

    public static <A> Option<A> flatMapEvalFirst(XPathExpression<Element> expr, Document doc, Function<Element, Option<A>> f) {
        return evalFirst(expr, doc).flatMap(f);
    }

    public static Option<String> getChildText(Element parent, String name) {
        return Option.of(parent.getChildTextTrim(name)).filter(s -> !s.isEmpty());
    }

    public static Option<Integer> getChildTextAsInteger(Element parent, String name) {
        return flatMapChildText(parent, name, s -> Try.of(() -> Integer.parseInt(s)).toOption());
    }

    public static Option<Integer> getChildTextAsInteger(Element parent, String name, Namespace ns) {
        return flatMapChildText(parent, name, ns, s -> Try.of(() -> Integer.parseInt(s)).toOption());
    }

    public static <A> Option<A> mapChildText(Element parent, String name, Function<String, A> f) {
        return getChildText(parent, name).map(f);
    }

    public static <A> Option<A> mapChildText(Element parent, String name, Namespace ns, Function<String, A> f) {
        return getChildText(parent, name, ns).map(f);
    }

    public static <A> Option<A> flatMapChildText(Element parent, String name, Function<String, Option<A>> f) {
        return getChildText(parent, name).flatMap(f);
    }

    public static <A> Option<A> flatMapChildText(Element parent, String name, Namespace ns, Function<String, Option<A>> f) {
        return getChildText(parent, name, ns).flatMap(f);
    }

    public static Option<String> getChildText(Element parent, String name, Namespace ns) {
        return Option.of(parent.getChildTextTrim(name, ns)).filter(s -> !s.isEmpty());
    }

    public static Option<Element> getChild(Element parent, String name, Namespace ns) {
        return Option.of(parent.getChild(name, ns));
    }

    public static Option<Element> getGrandChild(Element parent, String name, String childName, Namespace ns) {
        return getChild(parent, name, ns).flatMap(e -> getChild(e, childName, ns));
    }

    public static Option<Element> getGrandChild(Element parent, String name, String childName) {
        return getChild(parent, name).flatMap(e -> getChild(e, childName));
    }

    public static Option<String> getGrandChildText(Element parent, String name, String childName, Namespace ns) {
        return getChild(parent, name, ns).flatMap(e -> getChildText(e, childName, ns));
    }

    public static <A> Option<A> flatMapGrandChildText(Element parent, String name, String childName, Namespace ns, Function<String, Option<A>> f) {
        return getGrandChildText(parent, name, childName, ns).flatMap(f);
    }

    public static <A> Option<A> flatMapGrandChildText(Element parent, String name, String childName, Function<String, Option<A>> f) {
        return getGrandChildText(parent, name, childName).flatMap(f);
    }

    public static <A> Option<A> mapGrandChildText(Element parent, String name, String childName, Namespace ns, Function<String, A> f) {
        return getGrandChildText(parent, name, childName, ns).map(f);
    }

    public static <A> Option<A> mapGrandChildText(Element parent, String name, String childName, Function<String, A> f) {
        return getGrandChildText(parent, name, childName).map(f);
    }

    public static Option<String> getGrandChildText(Element parent, String name, String childName) {
        return getChild(parent, name).flatMap(e -> getChildText(e, childName));
    }

    public static Option<Element> firstChild(Element element) {
        return Option.ofOptional(element.getChildren().stream().findFirst());
    }

    public static Option<BigDecimal> getChildTextAsBigDecimal(Element element, String name) {
        return flatMapChildText(element, name, s -> Try.of(() -> new BigDecimal(s)).toOption());
    }

    public static Option<BigDecimal> getChildTextAsBigDecimal(Element element, String name, Namespace ns) {
        return flatMapChildText(element, name, ns, s -> Try.of(() -> new BigDecimal(s)).toOption());
    }

    public static <A> Option<List<A>> mapChildrenOf(Element element, String name, Function<Element, Option<A>> f) {
        return getChild(element, name).map(e -> e.getChildren().stream().flatMap(item -> f.apply(item).toJavaStream()).collect(List.collector()));
    }

    public static <A> Option<Vector<A>> mapChildrenOfVector(Element element, String name, Function<Element, Option<A>> f) {
        return getChild(element, name).map(e -> e.getChildren().stream().flatMap(item -> f.apply(item).toJavaStream()).collect(Vector.collector()));
    }

    public static <A> Option<List<A>> mapChildrenOf(Element element, String name, Namespace ns, Function<Element, Option<A>> f) {
        return getChild(element, name, ns).map(e -> e.getChildren().stream().flatMap(item -> f.apply(item).toJavaStream()).collect(List.collector()));
    }

    public static <A> Option<List<A>> mapChildren(Element element, String name, Function<Element, Option<A>> f) {
        return Option.of(element.getChildren(name)).map(list -> list.stream().flatMap(item -> f.apply(item).toJavaStream()).collect(List.collector()));
    }

    public static <A> Option<List<A>> mapChildren(Element element, String name, Namespace ns, Function<Element, Option<A>> f) {
        return Option.of(element.getChildren(name, ns)).map(list -> list.stream().flatMap(item -> f.apply(item).toJavaStream()).collect(List.collector()));
    }

    public static Element elem(String name, String value) {
        return new Element(name).setText(value);
    }


    public static Element elem(String name, Namespace ns, String value) {
        return new Element(name, ns).setText(value);
    }

    public static Element elem(String name, Option<String> value) {
        return elem(name, value.map(Text::new));
    }

    public static Element elem(String name, Namespace ns, Option<String> value) {
        return elem(name, ns, value.map(Text::new));
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
