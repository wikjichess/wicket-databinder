package net.databinder.models;

import org.apache.wicket.request.cycle.IRequestCycleListener;

/**
 * Request cycle that should be notified on the first use of a data session.
 */
public interface DataRequestCycleListener extends IRequestCycleListener {
	public void dataSessionRequested(Object key);
}
