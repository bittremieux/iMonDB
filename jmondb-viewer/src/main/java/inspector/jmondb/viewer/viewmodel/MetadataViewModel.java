package inspector.jmondb.viewer.viewmodel;

import inspector.jmondb.model.Metadata;

import java.util.ArrayList;
import java.util.List;

public class MetadataViewModel {

    private List<Metadata> metadataOptions;

    private MetadataFilter metadataFilter;

    public MetadataViewModel() {
        metadataOptions = new ArrayList<>();
    }

    public void addMetadataOption(Metadata metadata) {
        this.metadataOptions.add(metadata);
    }

    public void reset() {
        metadataFilter = null;
    }

    public void setMetadataFilter(MetadataFilter filter) {
        this.metadataFilter = filter;
    }

    public void clearAll() {
        metadataOptions.clear();
        metadataFilter = null;
    }

    public List<Metadata> getMetadataOptions() {
        return metadataOptions;
    }

    public MetadataFilter getMetadataFilter() {
        return metadataFilter;
    }
}
