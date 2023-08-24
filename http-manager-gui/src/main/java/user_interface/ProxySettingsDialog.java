package user_interface;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import config.ProxyConfig;
import proxy.ProxyMode;

public class ProxySettingsDialog extends Dialog {

	private static final Logger LOGGER = LogManager.getLogger(ProxySettingsDialog.class);
	
	private static final String TEST_CONN_URL = "http://www.google.com";
	public static final String PROXY_ERROR_MSG = "The tool could not establish a connection. Please check/change the 'Proxy configuration'. If the issue persists ask for support in the relevant functional mailbox.";

	public ProxySettingsDialog(Shell parent, int style) {
		super(parent, style);
	}
	
	/**
	 * Open the dialog
	 * @throws IOException configuration file not found
	 */
	public void open() throws IOException {
		Shell shell = new Shell(getParent(), getStyle());
		createContents(shell);
		shell.pack();
		shell.open();
	}
	
	public static void createContents(Shell shell) throws IOException {
		
		shell.setLayout(new GridLayout(2, false));
		
		Label proxyModeLabel = new Label(shell, SWT.NONE);
		proxyModeLabel.setText("Proxy:");
		
		ComboViewer proxyMode = new ComboViewer(shell);
		proxyMode.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void dispose() {}

			@Override
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {}

			@Override
			public Object[] getElements(Object arg0) {
				return (ProxyMode[]) arg0;
			}
		});
		
		proxyMode.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ProxyMode) element).getLabel();
			}
		});
		
		proxyMode.setInput(ProxyMode.values());
		
		Label hostnameLabel = new Label(shell, SWT.NONE);
		hostnameLabel.setText("Hostname:");
		
		Text hostname = new Text(shell, SWT.NONE);
		
		Label portLabel = new Label(shell, SWT.NONE);
		portLabel.setText("Port:");
		
		Text port = new Text(shell, SWT.NONE);
		
		Button testConnectionBtn = new Button(shell, SWT.NONE);
		testConnectionBtn.setText("Test connection");
		
		Button saveBtn = new Button(shell, SWT.NONE);
		saveBtn.setText("Save");
		
		proxyMode.getCombo().addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				if(proxyMode.getSelection().isEmpty())
					return;
				
				ProxyMode mode = (ProxyMode) ((IStructuredSelection) proxyMode.getSelection()).getFirstElement();
				
				boolean manualMode = mode == ProxyMode.MANUAL;
				
				hostname.setEnabled(manualMode);
				port.setEnabled(manualMode);
			}
		});
		
		testConnectionBtn.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				
				shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
				
				ProxyMode mode = (ProxyMode) ((IStructuredSelection) proxyMode.getSelection()).getFirstElement();
				
				// save the configuration before testing
				// otherwise not working
				try {
					Actions.save(mode, hostname.getText(), port.getText());
				} catch (Exception e) {
					LOGGER.error("There is an error upon saving the configuration before testing", e);
					e.printStackTrace();
					return;
				}
				
				boolean success = Actions.testConnection(TEST_CONN_URL);
				
				String message = null;
				int icon;
				
				if (success) {
					message = "Test connection: success!";
					icon = SWT.ICON_INFORMATION;
				}
				else {
					message = PROXY_ERROR_MSG;
					icon = SWT.ICON_ERROR;
				}
				
				shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));

				MessageBox box = new MessageBox(shell, icon);
				box.setMessage(message);
				box.open();
			}
		});
		
		saveBtn.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				ProxyMode mode = (ProxyMode) ((IStructuredSelection) proxyMode.getSelection()).getFirstElement();
				
				try {
					Actions.save(mode, hostname.getText(), port.getText());
				} catch (IOException e) {
					LOGGER.error("There is an error upon saving the configuration ", e);
					e.printStackTrace();
				}
				
				shell.close();
				shell.dispose();
			}
		});
		
		shell.setText("Proxy settings");

		// set default values contained in the file
		ProxyConfig config = new ProxyConfig();
		hostname.setText(config.getProxyHostname());
		port.setText(config.getProxyPort());

		boolean manual = config.getProxyMode() == ProxyMode.MANUAL;
		hostname.setEnabled(manual);
		port.setEnabled(manual);
		proxyMode.setSelection(new StructuredSelection(config.getProxyMode()));
	}
	
	public static void main(String[] args) throws IOException {
		Display display = new Display();
		Shell shell = new Shell(display);
		ProxySettingsDialog dialog = new ProxySettingsDialog(shell, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		dialog.open();
		
		while(!shell.isDisposed()) {
			display.readAndDispatch();
		}
	}
}
