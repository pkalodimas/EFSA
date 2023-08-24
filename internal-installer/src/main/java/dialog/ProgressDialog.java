package dialog;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import config.GithubConfig;

/**
 * Progress bar
 * @author avonva
 *
 */
public class ProgressDialog {

	private JFrame frame;
	private JPanel panel;
	private JLabel label;
	private JProgressBar progressBar;
	
	public ProgressDialog() throws IOException {
		create();
	}
	
	public JProgressBar getProgressBar() {
		return this.progressBar;
	}
	
	private void create() throws IOException {
		
		this.frame = new JFrame();
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		
		// center the frame
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2 - frame.getSize().width/2, dim.height/2 - frame.getSize().height/2);
		
		this.panel = new JPanel();
		
		this.progressBar = new JProgressBar();
		
		this.label = new JLabel("Downloading latest release...");
		
		panel.add(label);
		panel.add(progressBar);
		frame.add(panel);
		
		// set application title
		GithubConfig config = new GithubConfig();
		String frameTitle = config.getApplicationName();
		if (frameTitle == null) {
			frameTitle = "Update";
		}
		
		frame.setTitle(frameTitle);
		
		// set the application icon
		String appIconPath = config.getApplicationIconPath();
		
		if (appIconPath != null) {
			File appIcon = new File(appIconPath);
	
			if (appIcon.exists()) {
				Image image = ImageIO.read(appIcon);
				frame.setIconImage(image);
			}
		}
		
		frame.pack();
		
		frame.setSize(200, 90);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void close() {
		this.frame.setVisible(false);
		this.frame.dispose();
	}
}
