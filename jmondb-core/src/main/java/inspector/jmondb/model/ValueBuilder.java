package inspector.jmondb.model;

/**
 * Builder pattern to facilitate creating a {@link Value} with a specific set of member variables.
 */
public class ValueBuilder {

	private String name;
	private String type;
	private String accession;
	private CV cv;
	private Boolean isNumeric;
	private String firstValue;
	private Integer n;
	private Integer nDiffValues;
	private Integer nNotMissingValues;
	private Float min;
	private Float max;
	private Float mean;
	private Float median;
	private Float sd;
	private Float q1;
	private Float q3;

	public ValueBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public ValueBuilder setType(String type) {
		this.type = type;
		return this;
	}

	public ValueBuilder setAccession(String accession) {
		this.accession = accession;
		return this;
	}

	public ValueBuilder setCv(CV cv) {
		this.cv = cv;
		return this;
	}

	public ValueBuilder isNumeric(Boolean isNumeric) {
		this.isNumeric = isNumeric;
		return this;
	}

	public ValueBuilder setFirstValue(String firstValue) {
		this.firstValue = firstValue;
		return this;
	}

	public ValueBuilder setN(Integer n) {
		this.n = n;
		return this;
	}

	public ValueBuilder setNDiffValues(Integer nDiffValues) {
		this.nDiffValues = nDiffValues;
		return this;
	}

	public ValueBuilder setNNotMissingValues(Integer nNotMissingValues) {
		this.nNotMissingValues = nNotMissingValues;
		return this;
	}

	public ValueBuilder setMin(Float min) {
		this.min = min;
		return this;
	}

	public ValueBuilder setMax(Float max) {
		this.max = max;
		return this;
	}

	public ValueBuilder setMean(Float mean) {
		this.mean = mean;
		return this;
	}

	public ValueBuilder setMedian(Float median) {
		this.median = median;
		return this;
	}

	public ValueBuilder setSd(Float sd) {
		this.sd = sd;
		return this;
	}

	public ValueBuilder setQ1(Float q1) {
		this.q1 = q1;
		return this;
	}

	public ValueBuilder setQ3(Float q3) {
		this.q3 = q3;
		return this;
	}

	public Value createValue() {
		return new Value(name, type, accession, cv, isNumeric, firstValue, n, nDiffValues, nNotMissingValues, min, max, mean, median, sd, q1, q3);
	}
}
