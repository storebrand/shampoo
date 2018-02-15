package no.storebrand.shampoo;

import java.util.Objects;

public final class SoapAction {
    public static final SoapAction empty = new SoapAction("");

    public final String action;

    public SoapAction(String action) {
        this.action = action;
    }

    public static SoapAction of(String action) {
        if (action == null || action.trim().isEmpty()) {
            return empty;
        }
        return new SoapAction(action.trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoapAction that = (SoapAction) o;
        return Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {

        return Objects.hash(action);
    }

    @Override
    public String toString() {
        return action;
    }

    public String format() {
        return "\"" + action + "\"";
    }
}
