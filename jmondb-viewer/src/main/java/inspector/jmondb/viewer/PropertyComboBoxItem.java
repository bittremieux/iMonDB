package inspector.jmondb.viewer;

public class PropertyComboBoxItem {

	private String name;
	private String accession;

	public PropertyComboBoxItem(String name, String accession) {
		this.name = name;
		this.accession = accession;
	}

	public String getName() {
		return name;
	}

	public String getAccession() {
		return accession;
	}

	@Override
	public String toString() {
		return name + " (" + accession + ")";
	}
}
