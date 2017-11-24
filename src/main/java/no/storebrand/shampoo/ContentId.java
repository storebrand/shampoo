package no.storebrand.shampoo;

import java.util.Objects;

public final class ContentId {
    public final String value;

    public ContentId(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentId contentId = (ContentId) o;
        return Objects.equals(value, contentId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
