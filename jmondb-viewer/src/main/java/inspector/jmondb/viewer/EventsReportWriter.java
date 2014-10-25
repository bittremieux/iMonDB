package inspector.jmondb.viewer;

import inspector.jmondb.model.Event;
import inspector.jmondb.model.EventType;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.*;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventsReportWriter {

	// text styles
	private static StyleBuilder titleBigStyle = stl.style().bold().setFontSize(40).setHorizontalAlignment(HorizontalAlignment.LEFT).setVerticalAlignment(VerticalAlignment.MIDDLE);
	private static StyleBuilder titleNormalStyle = stl.style().bold().setFontSize(15).setHorizontalAlignment(HorizontalAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.TOP).setLineSpacing(LineSpacing.SINGLE);

	private static StyleBuilder keyStyle = stl.style().bold().setHorizontalAlignment(HorizontalAlignment.LEFT).setVerticalAlignment(VerticalAlignment.TOP);
	private static StyleBuilder valueStyle = stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setVerticalAlignment(VerticalAlignment.TOP);
	private static StyleBuilder dateStyle = valueStyle.setPattern("dd/MM/yyyy");

	public static void writeReport(String instrumentName, List<Event> events, File file) throws DRException, IOException {

		JasperReportBuilder report = report();

		// create report
		// header text fields
		TextFieldBuilder<String> dateHeader = cmp.text("Date:").setStyle(keyStyle).setWidth(35);
		TextFieldBuilder<String> typeHeader = cmp.text("Type:").setStyle(keyStyle).setWidth(35);
		TextFieldBuilder<String> problemHeader = cmp.text("Problem:").setStyle(keyStyle).setWidth(35);
		TextFieldBuilder<String> solutionHeader = cmp.text("Solution:").setStyle(keyStyle).setWidth(35);
		TextFieldBuilder<String> extraHeader = cmp.text("Additional information:").setStyle(keyStyle).setWidth(35);

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

		VerticalListBuilder rowList = cmp.verticalList(dateList, typeList, problemList, solutionList, extraList).setGap(3);

		// add to the report
		report.setColumnStyle(stl.style().setBottomBorder(stl.penDouble()));
		report.columns(col.componentColumn(rowList));

		report.pageFooter(cmp.pageXofY());

		report.setPageFormat(PageType.A4);
		report.setPageMargin(margin().setTop(20).setBottom(10).setLeft(30).setRight(30));

		// fill in customizable data
		// title
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		HorizontalListBuilder titleList = cmp.horizontalList(cmp.text("iMonDB").setStyle(titleBigStyle).setWidth(40),
				cmp.verticalList(cmp.text("Event log for instrument <" + instrumentName + ">").setStyle(titleNormalStyle),
						cmp.text("Generated on " + sdf.format(new Date())).setStyle(titleNormalStyle)));
		report.title(titleList);

		// add all events
		report.setDataSource(new JRBeanCollectionDataSource(events));

		// output to file
		FileOutputStream fos = new FileOutputStream(file);
		report.toPdf(fos);
		fos.close();
	}
}
