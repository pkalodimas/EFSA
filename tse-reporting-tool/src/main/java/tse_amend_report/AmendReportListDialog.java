package tse_amend_report;

import app_config.AppPaths;
import app_config.PropertiesReader;
import dataset.RCLDatasetStatus;
import global_utils.Message;
import global_utils.Warnings;
import i18n_messages.Messages;
import i18n_messages.TSEMessages;
import message.MessageConfigBuilder;
import message_creator.OperationType;
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
import progress_bar.IndeterminateProgressDialog;
import providers.ITableDaoService;
import providers.TseReportService;
import report.EFSAReport;
import report.Report;
import report.ReportActions;
import report.ReportType;
import report.ThreadFinishedListener;
import session_manager.TSERestoreableWindowDao;
import soap.DetailedSOAPException;
import table_dialog.DialogBuilder;
import table_skeleton.TableRowList;
import tse_report.RefreshStatusThread;
import tse_report.TseReport;
import tse_summarized_information.TseReportActions;
import window_restorer.RestoreableWindow;
import xlsx_reader.TableSchemaList;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Dialog showing the amended reports of the collection
 *
 * @author chalvatzaras
 */
public class AmendReportListDialog extends Dialog {

    private final String windowCode;
    private RestoreableWindow window;
    private final String dcCode;
    private List<TseReport> reports;
    private TseReport aggrReport;
    private DialogBuilder viewer;

    private TseReportService reportService;
    private ITableDaoService daoService;

    public AmendReportListDialog(Shell parent,
                                 String windowCode,
                                 String dcCode,
                                 TseReportService reportService,
                                 ITableDaoService daoService) {
        super(new Shell(parent), SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
        this.windowCode = windowCode;
        this.dcCode = Objects.requireNonNull(dcCode);
        this.daoService = Objects.requireNonNull(daoService);
        this.reportService = Objects.requireNonNull(reportService);
        this.loadAmendedMonthlyReports();
    }

    private void loadAmendedMonthlyReports() {
        this.aggrReport = this.reportService.getAllReports()
                .stream()
                .map(TseReport::new)
                .filter(r-> ReportType.COLLECTION_AGGREGATION.equals(r.getType()))
                .filter(r -> r.getDcCode().equals(dcCode))
                .filter(r -> Integer.parseInt(r.getVersion()) > 0)
                .filter(r -> Boolean.FALSE.equals(r.getRCLStatus().isFinalized()))
                .findAny()
                .orElse(null);

        if( Objects.nonNull(this.aggrReport) ){
            this.reports = this.reportService.getAllReports()
                    .stream()
                    .map(TseReport::new)
                    .filter(r->Objects.nonNull(r.getAggregatorId()))
                    .filter(TseReport::isVisible)
                    .filter(r-> aggrReport.getDatabaseId() == r.getAggregatorId())
                    // The below are a little bit redundant as according to business logic, they will always pass if the above filters pass.
                    // The aggregator report is deleted after it is finalized.
                    .filter(r -> r.getDcCode().equals(dcCode))
                    .filter(r -> Integer.parseInt(r.getVersion()) > 0)
                    .filter(r -> Boolean.FALSE.equals(r.getRCLStatus().isFinalized()))
                    .collect(Collectors.toList());
        }
        else {
            this.reports = this.reportService.getAllReports()
                    .stream()
                    .map(TseReport::new)
                    .filter(r -> r.getDcCode().equals(dcCode))
                    .filter(r -> Integer.parseInt(r.getVersion()) > 0)
                    .filter(r -> Boolean.FALSE.equals(r.getRCLStatus().isFinalized()))
                    .filter(r -> Boolean.FALSE.equals(RCLDatasetStatus.DRAFT.equals(r.getRCLStatus())))
                    .collect(Collectors.toList());
        }
    }

    protected void createContents(Shell shell) {
        SelectionListener sendListener = new SelectionAdapter() {
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
                .addLabelToComposite("datasetLabel", "panel")
                .addLabelToComposite("statusLabel", "panel")
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

        String[][] headers = new String[][]{
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
        viewer.setEnabled("refreshBtn", canRefreshStatus());
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

        refreshLabels();
    }

    /**
     * Update the ui using the report information
     */
    public void updateUI() {
        viewer.setEnabled("sendBtn", canAllBeSent());
        viewer.setEnabled("submitBtn", canAllBeSubmitted());
        viewer.setEnabled("refreshBtn", canRefreshStatus());
        refreshLabels();
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

    public void close(){
        this.getParent().close();
    }

    public void refreshLabels() {
        viewer.setLabelText("datasetLabel", TSEMessages.get("si.dataset.id", TSEMessages.get("si.no.data")));
        viewer.setLabelText("statusLabel", TSEMessages.get("si.dataset.status", TSEMessages.get("si.no.data")));
        if(this.reports.isEmpty()){
            return;
        }
        if( Objects.isNull(this.aggrReport) ){
            viewer.setLabelText("statusLabel", TSEMessages.get("si.dataset.status", this.reports.get(0).getRCLStatus().toString()));
            if( this.reports.size() == 1 ){
                viewer.setLabelText("datasetLabel", TSEMessages.get("si.dataset.id", this.reports.get(0).getId()));
            }
        }
        else{
            viewer.setLabelText("datasetLabel", TSEMessages.get("si.dataset.id", this.aggrReport.getId()));
            viewer.setLabelText("statusLabel", TSEMessages.get("si.dataset.status", this.aggrReport.getRCLStatus().toString()));
        }
    }

    private boolean canAllBeSent() {
        return !this.listIsEmpty() &&
                reports.stream().allMatch(tseReport -> tseReport.getRCLStatus().equals(RCLDatasetStatus.LOCALLY_VALIDATED));
    }

    private boolean canAllBeSubmitted() {
        return !this.listIsEmpty() &&
                reports.stream().allMatch(report -> report.getRCLStatus().canBeSubmitted());
    }

    private boolean canRefreshStatus() {
        return !this.listIsEmpty() &&
                this.reports.stream().anyMatch(report -> report.getRCLStatus().canBeRefreshed());
    }

    private boolean listIsEmpty() {
        return reports.isEmpty();
    }

    private void sendReports() {
        viewer.setEnabled("sendBtn", false);
        if( this.reports.size() > 1 ){
            this.aggrReport = this.reportService.createAggregatedReport(reports);
        }
        TseReport report = Objects.isNull(this.aggrReport) ? this.reports.get(0) : this.aggrReport;

        ReportActions actions = new TseReportActions(this.getParent(), report, reportService);

        actions.send(
                reportService.getSendMessageConfiguration(report),
                arg01 -> { this.onSendReportComplete(report); }
        );
    }

    private void onSendReportComplete(TseReport report){
        if (Boolean.FALSE.equals(ReportType.COLLECTION_AGGREGATION.equals(report.getType())) ) {
            this.loadAmendedMonthlyReports();
            updateUI();
            return;
        }
        // We used two versions for the aggregator report so the send process can compare the records and send only the updates/deletions.
        // After the aggregator report is send, we drop version "00",as we do not need it anymore.
        TseReport aggrReport = report.getAllVersions(this.daoService).stream()
                .peek(r->{
                    if( Boolean.FALSE.equals(report.getVersion().equals(r.getVersion())) ){
                        this.daoService.delete((Report)r);
                    }
                })
                .filter(r->report.getVersion().equals(r.getVersion()))
                .map(TseReport.class::cast)
                .findAny()
                .get();

        this.reports.forEach(r -> {
            r.setRCLStatus(aggrReport.getRCLStatus());
            r.setAggregatorId(aggrReport.getDatabaseId());
            this.daoService.update(r);
        });

        this.loadAmendedMonthlyReports();
        updateUI();
    }

    private void refreshStatuses() {
        Shell shell = this.getParent();
        TseReport report = this.reports.get(0);

        IndeterminateProgressDialog progressBar = new IndeterminateProgressDialog(
                shell,
                SWT.APPLICATION_MODAL,
                TSEMessages.get("refresh.status.progress.bar.label")
        );
        progressBar.open();

        RefreshStatusThread refreshStatus = new RefreshStatusThread(report, reportService, daoService);
        refreshStatus.setListener(new ThreadFinishedListener() {
            @Override
            public void finished(Runnable thread) {
                shell.getDisplay().asyncExec(() -> {
                    progressBar.close();
                    loadAmendedMonthlyReports();
                    updateUI();
                    Message log = refreshStatus.getLog();
                    if (log != null) {
                        log.open(shell);
                    }
                    if( listIsEmpty() ){
                        close();
                    }
                });
            }

            @Override
            public void terminated(Runnable thread, Exception e) {
                shell.getDisplay().asyncExec(() -> {
                    progressBar.close();
                    Message msg = (e instanceof DetailedSOAPException)
                            ? Warnings.createSOAPWarning((DetailedSOAPException) e)
                            : Warnings.createFatal(TSEMessages.get("refresh.status.error",
                            PropertiesReader.getSupportEmail()), report);
                    msg.open(shell);
                });
            }
        });
        refreshStatus.start();
    }

    private void submit() {
        if (this.reports.isEmpty()) {
            return;
        }
        this.getParent().setCursor(this.getParent().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

        TseReport report = Optional.of(this.reports.get(0))
                .filter(TseReport::isAggregated)
                .map(r->this.daoService.getById(TableSchemaList.getByName(AppPaths.REPORT_SHEET),r.getAggregatorId()))
                .map(TseReport::new)
                .orElse(this.reports.get(0));

        ReportActions actions = new TseReportActions(this.getParent(), report, reportService);
        MessageConfigBuilder config = reportService.getSendMessageConfiguration(report);
        config.setOpType(OperationType.SUBMIT);
        // Update the reports status with the one after submit.
        actions.perform(config, arg01 -> {
            this.reports.forEach(r -> {
                r.setRCLStatus(report.getRCLStatus());
                daoService.update(r);
            });
            this.loadAmendedMonthlyReports();
            updateUI();
            this.getParent().setCursor(this.getParent().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
        });
    }
}
