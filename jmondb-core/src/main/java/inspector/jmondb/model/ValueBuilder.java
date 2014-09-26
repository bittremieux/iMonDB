package inspector.jmondb.model;

/**
 * Builder pattern to facilitate creating a {@link Value} with a specific set of member variables.
 */
public class ValueBuilder {

	private String firstValue;
	private Integer n;
	private Integer nDiffValues;
	private Double min;
	private Double max;
	private Double mean;
	private Double median;
	private Double sd;
	private Double q1;
	private Double q3;
	private Property property;
	private Run run;

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

	public ValueBuilder setMin(Double min) {
		this.min = min;
		return this;
	}

	public ValueBuilder setMax(Double max) {
		this.max = max;
		return this;
	}

	public ValueBuilder setMean(Double mean) {
		this.mean = mean;
		return this;
	}

	public ValueBuilder setMedian(Double median) {
		this.median = median;
		return this;
	}

	public ValueBuilder setSd(Double sd) {
		this.sd = sd;
		return this;
	}

	public ValueBuilder setQ1(Double q1) {
		this.q1 = q1;
		return this;
	}

	public ValueBuilder setQ3(Double q3) {
		this.q3 = q3;
		return this;
	}

	public ValueBuilder setDefiningProperty(Property property) {
		this.property = property;
		return this;
	}

	public ValueBuilder setOriginatingRun(Run run) {
		this.run = run;
		return this;
	}

	public Value createValue() {
		return new Value(firstValue, n, nDiffValues, min, max, mean, median, sd, q1, q3, property, run);
	}
}
