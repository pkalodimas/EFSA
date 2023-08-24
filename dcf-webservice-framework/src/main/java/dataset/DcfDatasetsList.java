package dataset;

import java.util.ArrayList;
import java.util.Collection;

import soap.GetDatasetsList;

/**
 * List of dataset received by calling {@link GetDatasetsList}
 * @author avonva
 *
 */
public class DcfDatasetsList extends ArrayList<IDcfDataset> implements IDcfDatasetsList<IDcfDataset> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Filter the datasets by their sender id (regex)
	 * @param regex
	 * @return
	 */
	public DcfDatasetsList filterByDatasetId(String regex) {
		
		DcfDatasetsList filteredList = new DcfDatasetsList();
		
		for (IDcfDataset dataset : this) {
			
			String datasetId = dataset.getId();
			
			// avoid null senderId
			if (datasetId == null)
				continue;
			
			if (datasetId.matches(regex))
				filteredList.add(dataset);
		}
		
		return filteredList;
	}
	
	/**
	 * Filter the datasets by their sender id (regex)
	 * @param regex
	 * @return
	 */
	public DcfDatasetsList filterBySenderId(String regex) {
		
		DcfDatasetsList filteredList = new DcfDatasetsList();
		
		for (IDcfDataset dataset : this) {
			
			String senderId = dataset.getSenderId();
			
			// avoid null senderId
			if (senderId == null)
				continue;
			
			if (senderId.matches(regex))
				filteredList.add(dataset);
		}
		
		return filteredList;
	}
	
	/**
	 * Filter by inclusion
	 * @param statusFilter
	 * @return
	 */
	public DcfDatasetsList filterByStatus(Collection<DcfDatasetStatus> statusFilter) {
		return filterByStatus(statusFilter, false);
	}
	
	/**
	 * Filter the datasets by their status
	 * @param statusFilter
	 * @return
	 */
	public DcfDatasetsList filterByStatus(Collection<DcfDatasetStatus> statusFilter, boolean exclude) {
		
		DcfDatasetsList filteredList = new DcfDatasetsList();
		for (IDcfDataset d: this) {
			
			boolean contained = statusFilter.contains(d.getStatus());
			
			// if contained and we filter by inclusion
			// of if not contained and we filter by exclusion add it
			boolean addIt = (contained && !exclude) || (!contained && exclude);
			
			if (addIt) {
				filteredList.add(d);
			}
		}
		
		return filteredList;
	}
	
	public DcfDatasetsList filterByStatus(DcfDatasetStatus statusFilter, boolean exclude) {
		
		Collection<DcfDatasetStatus> status = new ArrayList<>();
		status.add(statusFilter);
		
		return filterByStatus(status, exclude);
	}
	
	public DcfDatasetsList filterByStatus(DcfDatasetStatus statusFilter) {		
		return filterByStatus(statusFilter, false);
	}

	@Override
	public IDcfDataset create() {
		return new DcfDataset();
	}
}
