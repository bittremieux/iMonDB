package inspector.imondb.viewer.view;

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

import inspector.imondb.model.Event;
import inspector.imondb.model.EventType;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.LineSpacing;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.VerticalAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

public class EventsReportWriter {

    // text styles
    private static StyleBuilder titleBigStyle = stl.style().bold().setFontSize(30)
            .setHorizontalAlignment(HorizontalAlignment.LEFT).setVerticalAlignment(VerticalAlignment.MIDDLE);
    private static StyleBuilder titleNormalStyle = stl.style().bold().setFontSize(15)
            .setHorizontalAlignment(HorizontalAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setLineSpacing(LineSpacing.SINGLE);

    private static StyleBuilder keyStyle = stl.style().bold()
            .setHorizontalAlignment(HorizontalAlignment.LEFT).setVerticalAlignment(VerticalAlignment.TOP);
    private static StyleBuilder valueStyle = stl.style()
            .setHorizontalAlignment(HorizontalAlignment.LEFT).setVerticalAlignment(VerticalAlignment.TOP);
    private static StyleBuilder dateStyle = valueStyle.setPattern("dd/MM/yyyy");

    private EventsReportWriter() {

    }

    public static void writeReport(String instrumentName, List<Event> events, File file) throws DRException, IOException {

        JasperReportBuilder report = report();

        // create report
        // logo
        BufferedImage logo = ImageIO.read(EventsReportWriter.class.getResource("/images/logo.png"));

        // header text fields
        int keyWidth = 35;
        TextFieldBuilder<String> dateHeader = cmp.text("Date:").setStyle(keyStyle).setWidth(keyWidth);
        TextFieldBuilder<String> typeHeader = cmp.text("Type:").setStyle(keyStyle).setWidth(keyWidth);
        TextFieldBuilder<String> problemHeader = cmp.text("Problem:").setStyle(keyStyle).setWidth(keyWidth);
        TextFieldBuilder<String> solutionHeader = cmp.text("Solution:").setStyle(keyStyle).setWidth(keyWidth);
        TextFieldBuilder<String> extraHeader = cmp.text("Additional information:").setStyle(keyStyle).setWidth(keyWidth);

        // value text fields
        TextFieldBuilder<Date> dateValue = cmp.text(field("date", type.dateType())).setStyle(dateStyle);
        TextFieldBuilder<EventType> typeValue = cmp.text(field("type", EventType.class)).setStyle(valueStyle);
        TextFieldBuilder<String> problemValue = cmp.text(field("problem", type.stringType())).setStyle(valueStyle);
        TextFieldBuilder<String> solutionValue = cmp.text(field("solution", type.stringType())).setStyle(valueStyle);
        TextFieldBuilder<String> extraValue = cmp.text(field("extra", type.stringType())).setStyle(valueStyle);

        // visualization lists
        HorizontalListBuilder dateList = cmp.horizontalList(dateHeader, dateValue);
        HorizontalListBuilder typeList = cmp.horizontalList(typeHeader, typeValue);
        HorizontalListBuilder problemList = cmp.horizontalList(problemHeader, problemValue);
        HorizontalListBuilder solutionList = cmp.horizontalList(solutionHeader, solutionValue);
        HorizontalListBuilder extraList = cmp.horizontalList(extraHeader, extraValue);

        VerticalListBuilder rowList = cmp.verticalList(cmp.text(""), dateList, typeList, problemList, solutionList,
                extraList, cmp.text("")).setGap(3);
        rowList.setStyle(stl.style().setBottomBorder(stl.pen2Point()));

        // add to the report
        report.columns(col.componentColumn(rowList));

        report.pageFooter(cmp.pageXofY());

        report.setPageFormat(PageType.A4);
        report.setPageMargin(margin().setTop(20).setBottom(10).setLeft(30).setRight(30));

        // fill in customizable data
        // title
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        HorizontalListBuilder titleList = cmp.horizontalList(cmp.image(logo).setFixedDimension(62, 50),
                cmp.horizontalGap(10),
                cmp.text("iMonDB").setStyle(titleBigStyle).setWidth(40),
                cmp.verticalList(
                        cmp.text("Event log for instrument <" + instrumentName + ">").setStyle(titleNormalStyle),
                        cmp.text("Generated on " + sdf.format(new Date())).setStyle(titleNormalStyle)))
                .newRow()
                .add(cmp.filler().setStyle(stl.style().setBottomBorder(stl.pen2Point())).setFixedHeight(10));
        report.title(titleList);

        // add all events
        report.setDataSource(new JRBeanCollectionDataSource(events));

        // output to file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            report.toPdf(fos);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }
}
