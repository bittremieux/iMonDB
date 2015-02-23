package inspector.imondb.collector.view.cli;

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

import inspector.imondb.collector.view.ProgressReporter;
import org.apache.commons.lang.StringUtils;

public class SystemOutProgressBar implements ProgressReporter {

    @Override
    public void setProgress(int progress) {
        System.out.print("[" + StringUtils.repeat("=", progress / 2) + StringUtils.repeat(" ", 50 - progress / 2) + "] " + progress + " %\r");
    }

    @Override
    public void done() {
        System.out.println("Completed");
    }
}
