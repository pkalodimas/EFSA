package html_viewer;

import java.io.File;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.program.Program;

public class HtmlViewer {

	private static final Logger LOGGER = LogManager.getLogger(HtmlViewer.class);
	
	//private Shell parent;
	//private Shell dialog;
	
	/**
	 * Dialog to show an html page
	 * @param parent
	 */
	/*public HtmlViewer(Shell parent) {
		this.parent = parent;
	}*/
	
	/*public void open(File file) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		String html = new String(encoded, "UTF-8");
		this.open(html);
	}*/
	
	/**
	 * Open an html file in the browser
	 * @param file
	 */
	public void open(File file) {
		
		URI uri = file.toURI();
		
		if (uri == null) {
			LOGGER.error("File not found " + file.getName());
			return;
		}
		
		Program.launch(uri.toString());
	}
	
	public void open(String url) {
		Program.launch(url);
	}

	/**
	 * Open the dialog and show the html page
	 * @param html
	 */
	 
	/*public void open(String html) {

		this.dialog = new Shell(parent);
		
		Browser browser = new Browser(dialog, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		browser.setText(html);
		browser.setBounds(0, 0, 600, 600);

		// when the page is rendered open the dialog
		browser.addProgressListener(new ProgressListener() {
			
			@Override
			public void completed(ProgressEvent arg0) {
				dialog.redraw();
				dialog.open();
			}
			
			@Override
			public void changed(ProgressEvent arg0) {}
		});
		
		dialog.pack();
		
		while (!dialog.isDisposed())
		      if (!dialog.getDisplay().readAndDispatch())
		    	  dialog.getDisplay().sleep();
	}
	
	public static void main(String[] args) throws MalformedURLException {
		
		Display display = new Display();
		Shell shell = new Shell(display);
		
		String html = "<html><head><title>Simple Page</title></head><body bgcolor='#777779'><hr/><font size=50>This is Html content</font><hr/></body></html>";
		
		HtmlViewer viewer = new HtmlViewer(shell);

		viewer.jOpen();
	}*/
}
