package inspector.jmondb.viewer.view.gui;

/*
 * #%L
 * jMonDB Viewer
 * %%
 * Copyright (C) 2014 InSPECtor
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 * Adapted from: https://stackoverflow.com/a/8387251
 */
public class JLabelLink {

	private static final String A_HREF = "<a href=\"";
	private static final String HREF_CLOSED = "\">";
	private static final String HREF_END = "</a>";
	private static final String HTML = "<html>";
	private static final String HTML_END = "</html>";

	private JPanel panel;

	public JLabelLink(String textBefore, String textLink, String link, String textAfter) {
		panel = new JPanel();

		if(textBefore != null) {
			JLabel labelBefore = new JLabel(textBefore);
			panel.add(labelBefore);
		}

		JLabel labelLink = new JLabel(textLink);
		panel.add(labelLink);
		if(isBrowsingSupported()) {
			makeLinkable(labelLink, link, new LinkMouseListener());
		}

		if(textAfter != null) {
			JLabel labelAfter = new JLabel(textAfter);
			panel.add(labelAfter);
		}
	}

	public JPanel getPanel() {
		return panel;
	}

	private static boolean isBrowsingSupported() {
		return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
	}

	private void makeLinkable(JLabel label, String link, MouseListener ml) {
		label.setText(htmlIfy(linkIfy(label.getText(), link)));
		label.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		label.addMouseListener(ml);
	}

	// WARNING: This method requires that s is a plain string that requires no further escaping
	private String linkIfy(String text, String url) {
		return A_HREF.concat(url).concat(HREF_CLOSED).concat(text).concat(HREF_END);
	}

	// WARNING: This method requires that s is a plain string that requires no further escaping
	private String htmlIfy(String s) {
		return HTML.concat(s).concat(HTML_END);
	}

	private class LinkMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(java.awt.event.MouseEvent evt) {
			JLabel l = (JLabel) evt.getSource();
			try {
				URI uri = new java.net.URI(getPlainLink(l.getText()));
				(new LinkRunner(uri)).execute();
			} catch(URISyntaxException use) {
				throw new AssertionError(use + ": " + l.getText()); //NOI18N
			}
		}

		private String getPlainLink(String s) {
			return s.substring(s.indexOf(A_HREF) + A_HREF.length(), s.indexOf(HREF_CLOSED));
		}
	}

	private class LinkRunner extends SwingWorker<Void, Void> {

		private final URI uri;

		private LinkRunner(URI u) {
			if(u == null) {
				throw new NullPointerException();
			}
			uri = u;
		}

		@Override
		protected Void doInBackground() throws Exception {
			Desktop desktop = java.awt.Desktop.getDesktop();
			desktop.browse(uri);
			return null;
		}

		@Override
		protected void done() {
			try {
				get();
			} catch(ExecutionException | InterruptedException ee) {
				JOptionPane.showMessageDialog(panel,
                        "A problem occurred while trying to open link <" + uri + "> in your system's standard browser.",
                        "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
