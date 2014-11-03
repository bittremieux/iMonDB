package inspector.jmondb.viewer;

import inspector.jmondb.model.Value;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class ValuePlot extends XYPlot {

	public ValuePlot(java.util.List<Object[]> values) {
		super();

		// add data
		XYSeries medianSeries = new XYSeries("Median");
		XYSeries q1Series = new XYSeries("Q1");
		XYSeries q3Series = new XYSeries("Q3");
		XYSeries minSeries = new XYSeries("Min");
		XYSeries maxSeries = new XYSeries("Max");
		for(Object[] objects : values) {
			Value value = (Value) objects[0];
			Timestamp time = (Timestamp) objects[1];
			medianSeries.add(time.getTime(), value.getMedian());
			q1Series.add(time.getTime(), value.getQ1());
			q3Series.add(time.getTime(), value.getQ3());
			minSeries.add(time.getTime(), value.getMin());
			maxSeries.add(time.getTime(), value.getMax());
		}

		XYSeriesCollection medianCollection = new XYSeriesCollection(medianSeries);
		XYSeriesCollection q1q3Collection = new XYSeriesCollection();
		q1q3Collection.addSeries(q1Series);
		q1q3Collection.addSeries(q3Series);
		XYSeriesCollection minMaxCollection = new XYSeriesCollection();
		minMaxCollection.addSeries(minSeries);
		minMaxCollection.addSeries(maxSeries);

		// renderer
		XYItemRenderer medianRenderer = new XYLineAndShapeRenderer();
		medianRenderer.setSeriesPaint(0, Color.BLACK);
		medianRenderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
		XYDifferenceRenderer q1q3Renderer = new XYDifferenceRenderer(Color.GRAY, Color.GRAY, true);
		q1q3Renderer.setSeriesPaint(0, Color.GRAY);
		q1q3Renderer.setSeriesPaint(1, Color.GRAY);
		q1q3Renderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
		q1q3Renderer.setSeriesShape(1, new Ellipse2D.Double(-2, -2, 4, 4));
		XYDifferenceRenderer minMaxRenderer = new XYDifferenceRenderer(Color.LIGHT_GRAY, Color.LIGHT_GRAY, true);
		minMaxRenderer.setSeriesPaint(0, Color.LIGHT_GRAY);
		minMaxRenderer.setSeriesPaint(1, Color.LIGHT_GRAY);
		minMaxRenderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4));
		minMaxRenderer.setSeriesShape(1, new Ellipse2D.Double(-2, -2, 4, 4));

		// create axis
		DateAxis dateAxis = new DateAxis("Date");
		dateAxis.setDateFormatOverride(new SimpleDateFormat("dd/MM/yyyy"));
		dateAxis.setVerticalTickLabels(true);

		NumberAxis valueAxis = new NumberAxis("Value");
		valueAxis.setAutoRangeIncludesZero(false);

		// create plot
		setDomainAxis(dateAxis);
		setRangeAxis(valueAxis);
		setDataset(0, medianCollection);
		setDataset(1, q1q3Collection);
		setDataset(2, minMaxCollection);
		setRenderer(0, medianRenderer);
		setRenderer(1, q1q3Renderer);
		setRenderer(2, minMaxRenderer);
	}
}
