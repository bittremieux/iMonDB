package inspector.imondb.collector.model;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RegexMapper<T extends RegexMap> {

    Collection<T> maps;

    public RegexMapper(Collection<T> maps) {
        this.maps = maps;
    }

    public List<T> getApplicableMaps(File file) {
        if(maps.isEmpty()) {
            return Collections.emptyList();
        } else {
            String fileName = file.getName();
            String filePath;
            try {
                filePath = FilenameUtils.getFullPath(file.getCanonicalPath());
            } catch(IOException e) {
                throw new IllegalArgumentException("Error while evaluating the file path: " + e.getMessage());
            }

            List<T> result = new ArrayList<>(maps.size());
            for(T map : maps) {
                if((map.getSource() == RegexSource.NAME && fileName.matches(map.getRegex())) ||
                        (map.getSource() == RegexSource.PATH && filePath.matches(map.getRegex()))) {
                    result.add(map);
                }
            }

            return result;
        }
    }
}
