package resource;

import java.util.ArrayList;

import soap.GetResourcesList;

/**
 * Result of {@link GetResourcesList} call.
 * @author avonva
 *
 */
public class DcfResourcesList extends ArrayList<IDcfResourceReference> implements IDcfResourcesList<IDcfResourceReference> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Get the list of resources that match the {@code resourceType}
	 * @param resourceType
	 * @return
	 */
	public DcfResourcesList getByType(String resourceType) {
		
		DcfResourcesList resources = new DcfResourcesList();
		
		for (IDcfResourceReference ref : this) {
			if (ref.getType().equals(resourceType))
				resources.add(ref);
		}

		return resources;
	}

	@Override
	public IDcfResourceReference create() {
		return new DcfResourceReference();
	}
}
