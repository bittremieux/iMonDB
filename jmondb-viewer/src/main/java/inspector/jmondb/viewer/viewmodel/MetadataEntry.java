package inspector.jmondb.viewer.viewmodel;

public class MetadataEntry {

    private String key;
    private MetadataOperator operator;
    private String value;

    public MetadataEntry(String key, MetadataOperator operator, String value) {
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public MetadataOperator getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + " " + operator.toString() + " " + value;
    }
}
