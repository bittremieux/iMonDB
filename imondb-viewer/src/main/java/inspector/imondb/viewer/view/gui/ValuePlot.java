package inspector.imondb.viewer.view.gui;

/*
 * #%L
 * iMonDB Viewer
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

import inspector.imondb.model.Value;
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

public class ValuePlot {

    private XYPlot plot;

    public ValuePlot(java.util.List<Object[]> values) {
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

        // create axes
        DateAxis dateAxis = new DateAxis("Date");
        dateAxis.setDateFormatOverride(new SimpleDateFormat("dd/MM/yyyy"));
        dateAxis.setVerticalTickLabels(true);

        NumberAxis valueAxis = new NumberAxis("Value");
        valueAxis.setAutoRangeIncludesZero(false);

        // create plot
        plot = new XYPlot();
        plot.setDomainAxis(dateAxis);
        plot.setRangeAxis(valueAxis);
        plot.setDataset(0, medianCollection);
        plot.setDataset(1, q1q3Collection);
        plot.setDataset(2, minMaxCollection);
        plot.setRenderer(0, medianRenderer);
        plot.setRenderer(1, q1q3Renderer);
        plot.setRenderer(2, minMaxRenderer);
    }

    public XYPlot getPlot() {
        return plot;
    }
}
