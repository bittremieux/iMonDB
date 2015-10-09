package inspector.imondb.viewer.view.io;

import inspector.imondb.model.Event;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class EventsCsvExporter implements EventsExporter {

    private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    private File file;
    private List<Event> events;

    private Object[] header = new String[]{ "Date", "Type", "Problem", "Solution", "Additional information" };

    public EventsCsvExporter(File file, List<Event> events) {
        this.file = file;
        this.events = events;
    }

    @Override
    public void export() throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.EXCEL.withDelimiter(';'));

        // write column headers
        csvPrinter.printRecord(header);

        // write all events
        for(Event event : events) {
            csvPrinter.printRecord(df.format(event.getDate()), event.getType().toString(),
                    event.getProblem(), event.getSolution(), event.getExtra());
        }

        csvPrinter.flush();
        csvPrinter.close();
        fileWriter.close();
    }
}
