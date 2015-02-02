package inspector.jmondb.viewer.viewmodel;

public enum MetadataOperator {

    EQUAL("="),
    NOT_EQUAL("≠");

    private final String value;

    MetadataOperator(String value) {
        this.value = value;
    }

    public String toQueryString() {
        switch(this) {
            case NOT_EQUAL:
                return "!=";
            case EQUAL:
            default:
                return "=";
        }
    }

    @Override
    public String toString() {
        return value;
    }

    public static MetadataOperator fromString(String text) {
        if(text != null) {
            for(MetadataOperator operator : values()) {
                if(text.equals(operator.toString())) {
                    return operator;
                }
            }
        }

        return null;
    }
}
