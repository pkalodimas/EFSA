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
import providers.ITableDaoService;
import providers.TseReportService;
import report.IMassAmendReportDialog;
import report.ReportActions;
import report.ReportType;
import session_manager.TSERestoreableWindowDao;
import table_dialog.DialogBuilder;
import table_skeleton.TableRowList;
import tse_report.TseReport;
import tse_summarized_information.TseReportActions;
import window_restorer.RestoreableWindow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Dialog showing the amended reports of the collection
 * @author chalvatzaras
 *
 */
public class AmendReportListDialog extends Dialog {

	private final String windowCode;
	private RestoreableWindow window;

	private List<TseReport> reports;
	private DialogBuilder viewer;

	private TseReportService reportService;
	private ITableDaoService daoService;

	public AmendReportListDialog(Shell parent,
								 String windowCode,
								 List<TseReport> reports,
								 TseReportService reportService,
								 ITableDaoService daoService) {
		super(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		this.windowCode = windowCode;
		this.reports = reports;
		this.daoService = Objects.requireNonNull(daoService);
		this.reportService = Objects.requireNonNull(reportService);
	}

	protected void createContents(Shell shell) {
		SelectionListener sendListener =  new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				sendReports();
			}
		};
		SelectionListener submitListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				submit();
			}
		};
		SelectionListener refreshStateListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				refreshStatuses();
			}
		};


		viewer = new DialogBuilder(shell);

		// add the toolbar composite
		viewer.addComposite("panel", new GridLayout(1, false), new GridData(SWT.FILL, SWT.FILL, true, false))
			.addGroupToComposite("buttonsComp", "panel", "Toolbar",
					new GridLayout(8, false), new GridData(SWT.FILL, SWT.FILL, true, false))
			// add the buttons to the toolbar
			.addButtonToComposite("sendBtn", "buttonsComp", "Send", sendListener)
			.addButtonToComposite("submitBtn", "buttonsComp", "Submit", submitListener)
			.addButtonToComposite("refreshBtn", "buttonsComp", "Refresh Status", refreshStateListener);
		addTableToComposite();

		initUI();
		updateUI();
		shell.pack();

		window = new RestoreableWindow(shell, this.windowCode);
		window.restore(TSERestoreableWindowDao.class);
		window.saveOnClosure(TSERestoreableWindowDao.class);
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

		table.setInput(new TableRowList(reports));
	}

	/**
	 * Initialise the labels to their initial state
	 */
	private void initUI() {
		viewer.setEnabled("refreshBtn", true);
		viewer.setEnabled("sendBtn", canAllBeSent() && !listIsEmpty());
		viewer.setEnabled("submitBtn", canAllBeSubmitted());

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
	}

	public void open() {
		Shell shell = new Shell(getParent(), SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		shell.setLayout(new GridLayout(1, false));
		shell.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		shell.setText(Messages.get("ma.dialog.title"));
		shell.setImage(getParent().getImage());

		this.createContents(shell);

		shell.open();

		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private boolean canAllBeSent() {
		return reports.stream()
				.allMatch(tseReport -> tseReport.getRCLStatus().equals(RCLDatasetStatus.LOCALLY_VALIDATED));
	}

	private boolean canAllBeSubmitted() {
		return reports.stream()
				.allMatch(report -> report.getRCLStatus().canBeSubmitted());
	}

	private boolean listIsEmpty() {
		return reports.isEmpty();
	}

	private void sendReports(){
		TseReport report = this.reports.size() == 1
				? this.reports.get(0)
				: this.reportService.createAggregatedReport(reports);

//				ReportActions actions = new TseReportActions(shell, report, reportService);
//				actions.send(reportService.getSendMessageConfiguration(report), arg01 -> updateUI());
		report.setRCLStatus(RCLDatasetStatus.UPLOADED);
		report.setId(String.valueOf(System.currentTimeMillis()));
		this.daoService.update(report);

		if(ReportType.COLLECTION_AGGREGATION.equals(report.getType())){
			this.reports.forEach(r->{
				r.setRCLStatus(report.getRCLStatus());
				r.setAggregatorId(report.getId());
				this.daoService.update(r);
			});
		}
		updateUI();
	}

	private void refreshStatuses(){

	}

	private void submit(){

	}
}
