package inspector.jmondb.viewer.viewmodel;

public enum MetadataConnector {

    AND("AND"),
    OR("OR");

    private final String value;

    MetadataConnector(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static MetadataConnector fromString(String text) {
        if(text != null) {
            for(MetadataConnector connector : values()) {
                if(text.equals(connector.toString())) {
                    return connector;
                }
            }
        }

        return null;
    }
}
