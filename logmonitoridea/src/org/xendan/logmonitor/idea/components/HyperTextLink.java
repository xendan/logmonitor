package org.xendan.logmonitor.idea.components;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HyperTextLink extends JEditorPane {
	public HyperTextLink(String linkText, Runnable doOnClick) {
		setContentType("text/html");
		setText(linkText);
		addHyperlinkListener(new OnClickListener(doOnClick));
		setEditable(false);
		setOpaque(false);
	}

	private class OnClickListener implements HyperlinkListener {
		private Runnable doOnClick;

		public OnClickListener(Runnable doOnClick) {
			this.doOnClick = doOnClick;
		}

		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
				doOnClick.run();
			}
		}
	}
}
