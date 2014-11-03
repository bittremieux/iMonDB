package inspector.jmondb.config;

import inspector.jmondb.model.Metadata;
import inspector.jmondb.model.Run;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A mapper which applies metadata to {@link Run}s, for example based on their file name and/or path name.
 *
 * The different metadata items are defined in a config file and can be retrieved by a {@link ConfigFile}.
 */
public class MetadataMapper {

	protected static final Logger logger = LogManager.getLogger(MetadataMapper.class);

	private List<Map<String, String>> metadata;

	/**
	 * Creates a mapper with information about all applicable metadata.
	 *
	 * @param metadata  a {@link List} of {@link Map}s where each {@code Map} signifies a separate metadata item
	 */
	public MetadataMapper(List<Map<String, String>> metadata) {
		this.metadata = metadata;
	}

	/**
	 * Checks whether the given {@link Run} satisfies metadata criteria and sets the applicable metadata.
	 *
	 * @param run  the {@code Run} to which metadata will be applied
	 * @param file  the file from which the {@code Run} originates
	 */
	public void applyMetadata(Run run, File file) {
		String fileName = file.getName();
		String filePath;
		try {
			filePath = FilenameUtils.getFullPath(file.getCanonicalPath());
		} catch(IOException e) {
			logger.error("Error while evaluating the file path: {}", e.getMessage());
			throw new IllegalArgumentException("Error while evaluating the file path: " + e.getMessage());
		}

		for(Map<String, String> md : metadata) {
			if(md.get("regex-source").equals("name")) {
				if(fileName.matches(md.get("regex"))) {
					logger.trace("Apply metadata <{} = {}> for run <{}>", md.get("name"), md.get("value"), run.getName());
					new Metadata(md.get("name"), md.get("value"), run);
				}
			}
			else if(md.get("regex-source").equals("path")) {
				if(filePath.matches(md.get("regex"))) {
					logger.trace("Apply metadata <{} = {}> for run <{}>", md.get("name"), md.get("value"), run.getName());
					new Metadata(md.get("name"), md.get("value"), run);
				}
			}
			// else: possibility for raw file comment field
			//TODO
		}
	}
}
