package tse_report;

import dataset.RCLDatasetStatus;
import providers.IReportService;
import report.Report;
import report.ReportType;
import report.ThreadFinishedListener;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GetAmendedReportsThread extends Thread {

	private TableRowList reports;

	private ThreadFinishedListener listener;

	public GetAmendedReportsThread(List<TseReport> reports) {
		this.reports = new TableRowList(new ArrayList<TableRow>(reports));
	}
	
	@Override
	public void run() {
		reports = getAmendedReports();
		if (listener != null) {
			listener.finished(this);
		}
	}
	
	public void setListener(ThreadFinishedListener listener) {
		this.listener = listener;
	}
	
	public TableRowList getAmendedReportsList() {
		return this.reports;
	}

	/**
	 * Get a list of all the amended reports of the data collection
	 *
	 * @return
	 */
	private TableRowList getAmendedReports() {
		return null;
//		List<TseReport> allReports = reportService.getAllReports()
//				.stream()
//				.map(TseReport::new)
//				.collect(Collectors.toList());
//
//		return allReports.stream()
//				.filter(Report::isVisible)
//				.filter(r -> r.getDcCode().equals(dcCode))
//				.filter(r -> r.getRCLStatus().equals(RCLDatasetStatus.LOCALLY_VALIDATED))
//				.filter(r -> Integer.parseInt(r.getVersion()) > 0)
//				.collect(Collectors.toCollection(TableRowList::new));

//		List<String> aggregatedIds = allReports.stream()
//				.map(TseReport::new)
//				.filter(r ->r.getDcCode().equals(dcCode))
//				.filter(r-> ReportType.COLLECTION_AGGREGATION.equals(r.getType()))
//				.filter(r-> Boolean.FALSE.equals(r.getRCLStatus().isFinalized()))
//				.map(Report::getId)
//				.collect(Collectors.toList());
//
//		if( Boolean.FALSE.equals(aggregatedIds.isEmpty()) ){
//			return new TableRowList(
//					allReports.stream()
//							.map(TseReport::new)
//							.filter(Report::isVisible)
//							.filter(r -> r.getDcCode().equals(dcCode))
//							.filter(r -> aggregatedIds.contains(r.getAggregatorId()))
//							.collect(Collectors.toList())
//			);
//		}
//		else {
//
//		}
	}
}
