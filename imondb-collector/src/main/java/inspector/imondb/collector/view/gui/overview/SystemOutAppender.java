package inspector.imondb.collector.view.gui.overview;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;

@Plugin(name="SystemOutAppender", category="Core", elementType="appender", printObject=true)
public class SystemOutAppender extends AbstractAppender {

    private SystemOutAppender(Layout<? extends Serializable> layout, Filter filter, String name) {
        super(name, filter, layout, false);
    }

    @Override
    public void append(LogEvent event) {
        if(event.getLevel() == Level.FATAL || event.getLevel() == Level.ERROR) {
            System.err.println(new String(getLayout().toByteArray(event)));
        } else {
            System.out.println(new String(getLayout().toByteArray(event)));
        }
    }

    @PluginFactory
    public static SystemOutAppender createAppender(@PluginElement("Layout") Layout<? extends Serializable> layout,
                                                   @PluginElement("Filter") final Filter filter,
                                                   @PluginAttribute("name") final String name) {
        return new SystemOutAppender(layout, filter, name);
    }
}
