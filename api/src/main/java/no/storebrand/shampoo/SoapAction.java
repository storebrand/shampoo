package no.storebrand.shampoo;

public final class SoapAction {
    public static final SoapAction empty = new SoapAction("");

    public final String action;

    public SoapAction(String action) {
        this.action = action;
    }

    public static SoapAction of(String action) {
        return new SoapAction(action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoapAction that = (SoapAction) o;

        return action.equals(that.action);
    }

    @Override
    public int hashCode() {
        return action.hashCode();
    }

    @Override
    public String toString() {
        return action;
    }

    public String format() {
        return "\"" + action + "\"";
    }
}
