package inspector.jmondb.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;

@Entity
@Table(name = "imon_value")
public class Value {

	@Transient
	private static final Logger logger = LogManager.getLogger(Value.class);

	/** read-only iMonDB primary key; generated by JPA */
	@Id
	@Column(name="id", nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	/** the name of the value */
	@Column(name="name", nullable=false, length=200)
	private String name;
	/** the type of the value */
	@Column(name="type", nullable=false, length=20)
	private String type;
	/** the accession number that identifies the value in the controlled vocabulary */
	@Column(name="accession", nullable=false, length=255)
	private String accession;
	/** the controlled vocabulary that defines the value */
	@ManyToOne(cascade=CascadeType.MERGE, fetch=FetchType.EAGER)
	@JoinColumn(name="l_imon_cv_id", referencedColumnName="id")
	private CV cv;

	/** indicates whether the Value signifies numerical data */
	@Column(name="isnumeric", nullable=false)
	private Boolean isNumeric;
	/** the first observation */
	@Column(name="firstvalue", length=50)
	private String firstValue;
	/** the number of observations used to calculate the summary value */
	@Column(name="n")
	private Integer n;
	/** the number of different observations */
	@Column(name="n_diffvalues")
	private Integer nDiffValues;
	/** the number of observations present */
	@Column(name="n_notmissingvalues")
	private Integer nNotMissingValues;
	/** the minimum observation */
	@Column(name="min")
	private Float min;
	/** the maximum observation */
	@Column(name="max")
	private Float max;
	/** the mean observation */
	@Column(name="mean")
	private Float mean;
	/** the median observation */
	@Column(name="median")
	private Float median;
	/** the standard deviation */
	@Column(name="sd")
	private Float sd;
	/** the first quartile */
	@Column(name="q1")
	private Float q1;
	/** the third quartile */
	@Column(name="q3")
	private Float q3;

	/** inverse part of the bi-directional relationship with {@link Run} */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="l_imon_run_id", referencedColumnName="id")
	private Run fromRun;

	/**
	 * Default constructor required by JPA.
	 * Protected access modification to enforce that client code uses the constructor that sets the required member variables.
	 */
	protected Value() {

	}

	/**
	 * Creates a Value. Use the {@link ValueBuilder} to easily create a Value with a specific set of member variables.
	 *
	 * This Value signifies a summary value calculated out of a range of different observations.
	 *
	 * The id is automatically determined by the database as primary key.
	 *
	 * @param name  The name of the value
	 * @param type  The type of the value
	 * @param accession  The accession number that identifies the value in the controlled vocabulary
	 * @param cv  The controlled vocabulary that defines the value
	 * @param isNumeric  Indicates whether the Value signifies numerical data
	 * @param firstValue  The first observation
	 * @param n  The number of observations used to calculate the summary value
	 * @param nDiffValues  The number of different observations
	 * @param nNotMissingValues  The number of observations present
	 * @param min  The minimum observation
	 * @param max  The maximum observation
	 * @param mean  The mean observation
	 * @param median  The median observation
	 * @param sd  The standard deviation
	 * @param q1  The first quartile
	 * @param q3  The third quartile
	 */
	public Value(String name, String type, String accession, CV cv, Boolean isNumeric, String firstValue, Integer n, Integer nDiffValues, Integer nNotMissingValues, Float min, Float max, Float mean, Float median, Float sd, Float q1, Float q3) {
		this();

		this.name = name;
		this.type = type;
		this.accession = accession;
		this.cv = cv;
		this.isNumeric = isNumeric;
		this.firstValue = firstValue;
		this.n = n;
		this.nDiffValues = nDiffValues;
		this.nNotMissingValues = nNotMissingValues;
		this.min = min;
		this.max = max;
		this.mean = mean;
		this.median = median;
		this.sd = sd;
		this.q1 = q1;
		this.q3 = q3;
	}

	//TODO: temporary copy constructor
	public Value(Value other) {
		this();

		setName(other.getName());
		setType(other.getType());
		setAccession(other.getAccession());
		setCv(other.getCv());
		setNumeric(other.getNumeric());
		setFirstValue(other.getFirstValue());
		setN(other.getN());
		setNDiffValues(other.getNDiffValues());
		setNNotMissingValues(other.getNNotMissingValues());
		setMin(other.getMin());
		setMax(other.getMax());
		setMean(other.getMean());
		setMedian(other.getMedian());
		setSd(other.getSd());
		setQ1(other.getQ1());
		setQ3(other.getQ3());
	}

	public Long getId() {
		return id;
	}

	/* package private: read-only key to be set by the JPA implementation */
	void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public CV getCv() {
		return cv;
	}

	public void setCv(CV cv) {
		this.cv = cv;
	}

	public Boolean getNumeric() {
		return isNumeric;
	}

	public void setNumeric(Boolean numeric) {
		this.isNumeric = numeric;
	}

	public String getFirstValue() {
		return firstValue;
	}

	public void setFirstValue(String firstValue) {
		this.firstValue = firstValue;
	}

	public Integer getN() {
		return n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	public Integer getNDiffValues() {
		return nDiffValues;
	}

	public void setNDiffValues(Integer nDiffValues) {
		this.nDiffValues = nDiffValues;
	}

	public Integer getNNotMissingValues() {
		return nNotMissingValues;
	}

	public void setNNotMissingValues(Integer nNotMissingValues) {
		this.nNotMissingValues = nNotMissingValues;
	}

	public Float getMin() {
		return min;
	}

	public void setMin(Float min) {
		this.min = min;
	}

	public Float getMax() {
		return max;
	}

	public void setMax(Float max) {
		this.max = max;
	}

	public Float getMean() {
		return mean;
	}

	public void setMean(Float mean) {
		this.mean = mean;
	}

	public Float getMedian() {
		return median;
	}

	public void setMedian(Float median) {
		this.median = median;
	}

	public Float getSd() {
		return sd;
	}

	public void setSd(Float sd) {
		this.sd = sd;
	}

	public Float getQ1() {
		return q1;
	}

	public void setQ1(Float q1) {
		this.q1 = q1;
	}

	public Float getQ3() {
		return q3;
	}

	public void setQ3(Float q3) {
		this.q3 = q3;
	}

	public void setFromRun(Run run) {
		this.fromRun = run;
	}

	public String toString() {
		return "Value \t" + getNumeric() + "\t" + getFirstValue() + "\t" + getN() + "\t" + getNDiffValues()
				+ "\t" + getNNotMissingValues() + "\t" + getMin() + "\t" + getMax() + "\t" + getMean()
				+ "\t" + getMedian() + "\t" + getSd() + "\t" + getQ1() + "\t" + getQ3();
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		Value that = (Value) o;

		if(id != null ? !id.equals(that.id) : that.id != null) return false;
		if(name != null ? !name.equals(that.name) : that.name != null) return false;
		if(type != null ? !type.equals(that.type) : that.type != null) return false;
		if(accession != null ? !accession.equals(that.accession) : that.accession != null) return false;
		if(cv != null ? !cv.equals(that.cv) : that.cv != null) return false;
		if(firstValue != null ? !firstValue.equals(that.firstValue) : that.firstValue != null) return false;
		if(isNumeric != null ? !isNumeric.equals(that.isNumeric) : that.isNumeric != null) return false;
		if(max != null ? !max.equals(that.max) : that.max != null) return false;
		if(mean != null ? !mean.equals(that.mean) : that.mean != null) return false;
		if(median != null ? !median.equals(that.median) : that.median != null) return false;
		if(min != null ? !min.equals(that.min) : that.min != null) return false;
		if(n != null ? !n.equals(that.n) : that.n != null) return false;
		if(nDiffValues != null ? !nDiffValues.equals(that.nDiffValues) : that.nDiffValues != null) return false;
		if(nNotMissingValues != null ? !nNotMissingValues.equals(that.nNotMissingValues) : that.nNotMissingValues != null)
			return false;
		if(q1 != null ? !q1.equals(that.q1) : that.q1 != null) return false;
		if(q3 != null ? !q3.equals(that.q3) : that.q3 != null) return false;
		if(sd != null ? !sd.equals(that.sd) : that.sd != null) return false;

		return true;
	}
}
