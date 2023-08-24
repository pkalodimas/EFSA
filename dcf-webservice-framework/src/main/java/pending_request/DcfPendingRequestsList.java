package pending_request;

import java.util.ArrayList;

public class DcfPendingRequestsList extends ArrayList<IPendingRequest> implements IDcfPendingRequestsList<IPendingRequest> {
	private static final long serialVersionUID = -2833190111143069333L;

	@Override
	public IPendingRequest create() {
		return new PendingRequest();
	}
}
