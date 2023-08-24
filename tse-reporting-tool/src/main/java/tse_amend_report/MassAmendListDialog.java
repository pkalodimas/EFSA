package tse_amend_report;

import dataset.RCLDatasetStatus;
import i18n_messages.Messages;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import providers.IReportService;
import providers.ITableDaoService;
import providers.TseReportService;
import report.IMassAmendReportDialog;
import report.ReportActions;
import table_dialog.DialogBuilder;
import table_skeleton.TableRowList;
import tse_report.TseReport;
import tse_summarized_information.TseReportActions;

import java.util.ArrayList;

/**
 * Dialog showing the amended reports of the collection
 * @author chalvatzaras
 *
 */
public class MassAmendListDialog extends Dialog implements IMassAmendReportDialog {

	private TableRowList list;

	private ArrayList<TseReport> reportList;

	protected TseReportService reportService;

	protected ITableDaoService daoService;

	private DialogBuilder viewer;

	public MassAmendListDialog(Shell parent, TableRowList list, IReportService reportService, ITableDaoService daoService, int style) {
		super(parent, style);
		this.list = list;
		this.daoService = daoService;
		this.reportService = (TseReportService) reportService;
	}

	public void setReportList(TableRowList list) {
		this.list = list;
		this.reportList = convertTableRowsToReports(list);
	}

	protected void createContents(Shell shell) {
		SelectionListener sendListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

				TseReport aggregatedReport = reportService.createAggregatedReport(reportList);
				ReportActions actions = new TseReportActions(shell, aggregatedReport, reportService);
				actions.send(reportService.getSendMessageConfiguration(aggregatedReport), arg01 -> updateUI());
				updateUI();
			}
		};

		SelectionListener submitListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateUI();
			}
		};

		SelectionListener refreshStateListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateUI();
			}
		};

//		SelectionListener displayReportListener = new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent arg0) {
//				updateUI();
//			}
//		};

		viewer = new DialogBuilder(shell);

		// add the toolbar composite
		viewer.addComposite("panel", new GridLayout(1, false), new GridData(SWT.FILL, SWT.FILL, true, false))
			.addGroupToComposite("buttonsComp", "panel", "Toolbar",
					new GridLayout(8, false), new GridData(SWT.FILL, SWT.FILL, true, false))
			// add the buttons to the toolbar
			.addButtonToComposite("sendBtn", "buttonsComp", "Send", sendListener)
			.addButtonToComposite("submitBtn", "buttonsComp", "Submit", submitListener)
			.addButtonToComposite("refreshBtn", "buttonsComp", "Refresh Status",
					refreshStateListener);
//			.addButtonToComposite("displayReportBtn", "buttonsComp", "Display Report",
//					displayReportListener);
		addTableToComposite();

		initUI();
		updateUI();

		shell.pack();
	}

	private void addTableToComposite() {
		Composite composite = new Composite(viewer.getComposite(), SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewer table = new TableViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.HIDE_SELECTION | SWT.NONE);
		table.getTable().setHeaderVisible(true);
		table.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setContentProvider(new MassAmendReportContentProvider());
		table.getTable().setLinesVisible(true);

		String[][] headers = new String[][] {
				{"year", Messages.get("ma.header.year")},
				{"month", Messages.get("ma.header.month")},
				{"country", Messages.get("ma.header.country")}
		};

		for (String[] header : headers) {
			// Add the column to the parent table
			TableViewerColumn col = new TableViewerColumn(table, SWT.NONE | SWT.BORDER);
			col.getColumn().setText(header[1]);
			col.setLabelProvider(new MassAmendReportLabelProvider(header[0]));
			col.getColumn().setWidth(150);
		}

		table.setInput(list);
	}

	/**
	 * Initialise the labels to their initial state
	 */
	private void initUI() {
		viewer.setEnabled("refreshBtn", true);
		viewer.setEnabled("sendBtn", canAllBeSent() && !listIsEmpty());
		viewer.setEnabled("submitBtn", canAllBeSubmitted());
//		viewer.setEnabled("displayReportBtn", true);

		// add image to send button
		Image sendImage = new Image(Display.getCurrent(),
				this.getClass().getClassLoader().getResourceAsStream("send-icon.png"));
		viewer.addButtonImage("sendBtn", sendImage);

		// add image to refresh button
		Image submitImage = new Image(Display.getCurrent(),
				this.getClass().getClassLoader().getResourceAsStream("submit-icon.png"));
		viewer.addButtonImage("submitBtn", submitImage);

		// add image to refresh button
		Image refreshImage = new Image(Display.getCurrent(),
				this.getClass().getClassLoader().getResourceAsStream("refresh-icon.png"));
		viewer.addButtonImage("refreshBtn", refreshImage);

		// add image to display button
//		Image displayImage = new Image(Display.getCurrent(),
//				this.getClass().getClassLoader().getResourceAsStream("displayAck-icon.png"));
//		viewer.addButtonImage("displayReportBtn", displayImage);
	}

	/**
	 * Update the ui using the report information
	 */
	public void updateUI() {
		viewer.setEnabled("sendBtn", canAllBeSent());
		viewer.setEnabled("submitBtn", canAllBeSubmitted());
		viewer.setEnabled("refreshBtn", true);
//		viewer.setEnabled("displayReportBtn", true);
	}

	public void open() {

		Shell shell = new Shell(getParent(), SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		shell.setLayout(new GridLayout(1, false));
		shell.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		shell.setText(Messages.get("ma.dialog.title"));
		shell.setImage(getParent().getImage());

		createContents(shell);

		shell.open();

		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private boolean canAllBeSent() {
		return reportList.stream()
				.allMatch(tseReport -> tseReport.getRCLStatus().equals(RCLDatasetStatus.LOCALLY_VALIDATED));
	}

	private boolean canAllBeSubmitted() {
		return reportList.stream()
				.allMatch(tseReport -> tseReport.getRCLStatus().canBeSubmitted());
	}

	private boolean listIsEmpty() {
		return reportList.isEmpty();
	}

	/**
	 * Converts TableRowListToReports.
	 * @param list Table Row List.
	 *
	 * @return  Array list of reports.
	 */
	private ArrayList<TseReport> convertTableRowsToReports(TableRowList list) {
		ArrayList<TseReport> reports = new ArrayList<>();
		list.forEach(tr -> reports.add(new TseReport(tr)));
		return reports;
	}
}
