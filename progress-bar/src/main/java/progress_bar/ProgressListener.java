package progress_bar;

/**
 * Listener used to notify a process of the
 * progress of another thread.
 * @author avonva
 *
 */
public interface ProgressListener {
	
	/**
	 * Called if the progress of the thread is changed
	 * @param currentProgress current progress of the thread
	 * @param maxProgress maximum progress that the thread could reach
	 * @param bar the progress bar involved
	 */
	public void progressChanged(double currentProgress, double maxProgress);
	
	
	public void progressChanged(double currentProgress);
	
	/**
	 * Called if the process is ended
	 */
	public void progressCompleted();
	
	/**
	 * Called if the progress of the thread was stopped
	 * due to errors.
	 * @param exception raised
	 */
	public void progressStopped(Exception exception);
}
