package net.infotrek.util.prefs;

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

import org.apache.commons.io.IOUtils;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Preferences implementation that stores to a user-defined file. See FilePreferencesFactory.
 *
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id: FilePreferences.java 283 2009-06-18 17:06:58Z david $
 *
 * Retrieved from: http://www.davidc.net/programming/java/java-preferences-using-file-backing-store
 */
public class FilePreferences extends AbstractPreferences {

    private static final Logger LOGGER = Logger.getLogger(FilePreferences.class.getName());

    private Map<String, String> root;
    private Map<String, FilePreferences> children;
    private boolean isRemoved = false;

    public FilePreferences(AbstractPreferences parent, String name) {
        super(parent, name);

        LOGGER.finest("Instantiating node " + name);

        root = new TreeMap<>();
        children = new TreeMap<>();

        try {
            sync();
        } catch(BackingStoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to sync on creation of node " + name, e);
        }
    }

    protected void putSpi(String key, String value) {
        root.put(key, value);
        try {
            flush();
        } catch(BackingStoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to flush after putting " + key, e);
        }
    }

    protected String getSpi(String key) {
        return root.get(key);
    }

    protected void removeSpi(String key) {
        root.remove(key);
        try {
            flush();
        } catch(BackingStoreException e) {
            LOGGER.log(Level.SEVERE, "Unable to flush after removing " + key, e);
        }
    }

    protected void removeNodeSpi() throws BackingStoreException {
        isRemoved = true;
        flush();
    }

    protected String[] keysSpi() throws BackingStoreException {
        return root.keySet().toArray(new String[root.keySet().size()]);
    }

    protected String[] childrenNamesSpi() throws BackingStoreException {
        return children.keySet().toArray(new String[children.keySet().size()]);
    }

    protected FilePreferences childSpi(String name) {
        FilePreferences child = children.get(name);
        if(child == null || child.isRemoved()) {
            child = new FilePreferences(this, name);
            children.put(name, child);
        }
        return child;
    }


    protected void syncSpi() throws BackingStoreException {
        if(isRemoved()) {
            return;
        }

        final File file = FilePreferencesFactory.getPreferencesFile();

        if(!file.exists()) {
            return;
        }

        synchronized(file) {
            Properties p = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                p.load(fis);

                StringBuilder sb = new StringBuilder();
                getPath(sb);
                String path = sb.toString();

                final Enumeration<?> pnen = p.propertyNames();
                while(pnen.hasMoreElements()) {
                    String propKey = (String) pnen.nextElement();
                    if(propKey.startsWith(path)) {
                        String subKey = propKey.substring(path.length());
                        // Only load immediate descendants
                        if(subKey.indexOf('.') == -1) {
                            root.put(subKey, p.getProperty(propKey));
                        }
                    }
                }
            } catch(IOException e) {
                throw new BackingStoreException(e);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
    }

    private void getPath(StringBuilder sb) {
        final FilePreferences parent = (FilePreferences) parent();
        if(parent == null) {
            return;
        }

        parent.getPath(sb);
        sb.append(name()).append('.');
    }

    protected void flushSpi() throws BackingStoreException {
        final File file = FilePreferencesFactory.getPreferencesFile();

        synchronized(file) {
            Properties p = new Properties();
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                StringBuilder sb = new StringBuilder();
                getPath(sb);
                String path = sb.toString();

                if(file.exists()) {
                    fis = new FileInputStream(file);
                    p.load(fis);

                    List<String> toRemove = new ArrayList<>();

                    // Make a list of all direct children of this node to be removed
                    final Enumeration<?> pnen = p.propertyNames();
                    while(pnen.hasMoreElements()) {
                        String propKey = (String) pnen.nextElement();
                        if(propKey.startsWith(path)) {
                            String subKey = propKey.substring(path.length());
                            // Only do immediate descendants
                            if(subKey.indexOf('.') == -1) {
                                toRemove.add(propKey);
                            }
                        }
                    }

                    // Remove them now that the enumeration is done with
                    toRemove.forEach(p::remove);
                }

                // If this node hasn't been removed, add back in any values
                if(!isRemoved) {
                    for(String s : root.keySet()) {
                        p.setProperty(path + s, root.get(s));
                    }
                }

                fos = new FileOutputStream(file);
                p.store(fos, "FilePreferences");
            } catch(IOException e) {
                throw new BackingStoreException(e);
            } finally {
                IOUtils.closeQuietly(fis);
                IOUtils.closeQuietly(fos);
            }
        }
    }
}
