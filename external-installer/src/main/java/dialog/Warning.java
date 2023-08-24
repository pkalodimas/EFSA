package dialog;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class Warning {

	private static JDialog createDialog() {
		final JDialog dialog = new JDialog();
		dialog.setAlwaysOnTop(true);
		return dialog;
	}
	
	public static void warnUser(String title, String msg, int style) {
		final JDialog dialog = createDialog();
		JOptionPane.showMessageDialog(dialog, msg, title, style);
		dialog.dispose();
	}
	
	public static int askUser(String title, String msg, int style) {
		final JDialog dialog = createDialog();
		int val = JOptionPane.showConfirmDialog(dialog, msg, title, style);
		dialog.dispose();
		
		return val;
	}
	
	public static String getStackTrace(Exception e) {
		
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement ste : e.getStackTrace()) {
	        sb.append("\n\tat ");
	        sb.append(ste);
	    }
	    String trace = sb.toString();
	    
	    return trace;
	}
}
