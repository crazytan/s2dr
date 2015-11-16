package Client;

/*
 * A wrapper class for unique IDs
 */
public final class UID {
    private String id;

    // protects the default constructor
    private UID() {}

    public UID(String id) {
        this.id = id;
    }

    public String get_id() {
        return id;
    }

    @Override public String toString() {
        return "UID: " + id;
    }
}
