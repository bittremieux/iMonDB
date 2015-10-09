package inspector.imondb.viewer.view.io;

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
