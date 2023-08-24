package tse_amend_report;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import providers.IReportService;
import providers.ITableDaoService;
import session_manager.TSERestoreableWindowDao;
import table_skeleton.TableRowList;
import window_restorer.RestoreableWindow;

/**
 * Class which allows adding and editing a summarized information report.
 * 
 * @author avonva
 * @author shahaal
 *
 */
public class TSEAmendReportsDialog extends MassAmendListDialog {

	private static final String WINDOW_CODE = "MassAmendReportsListDialog";
	private RestoreableWindow window;

	public TSEAmendReportsDialog(Shell parent, IReportService reportService, ITableDaoService daoService, TableRowList list) {
		super(parent, list, reportService, daoService, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
	}

	@Override
	protected void createContents(Shell shell) {

		super.createContents(shell);

		window = new RestoreableWindow(shell, WINDOW_CODE);
		window.restore(TSERestoreableWindowDao.class);
		window.saveOnClosure(TSERestoreableWindowDao.class);
	}
}
