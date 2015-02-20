package inspector.imondb.collector.view.gui.overview;

/*
 * #%L
 * iMonDB Collector
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
