package inspector.jmondb.viewer.viewmodel;

/*
 * #%L
 * jMonDB Viewer
 * %%
 * Copyright (C) 2014 - 2015 InSPECtor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
        reset();
    }

    public List<Metadata> getMetadataOptions() {
        return metadataOptions;
    }

    public MetadataFilter getMetadataFilter() {
        return metadataFilter;
    }
}
