package ui_term_properties;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import catalogue_object.Term;
import global_manager.GlobalManager;
import i18n_messages.CBMessages;

/**
 * Class to drawn scopenotes with links This class manages also opening the
 * browser (for links) Moreover, this class manages the right click menu which
 * can be opened in the scopenotes. In this menu it is possible to search the
 * selected words in google/google images/ wikipedia/google translate... An
 * automatic recognition of single clicked words is present (i.e. if you right
 * click a word the program automatically selects it)
 * 
 * @author shahaal
 * @author avonva
 *
 */

public class ScopenotesWithLinks {

	protected static final String GOOGLE_WEB = "https://www.google.com/#q=";
	protected static final String GOOGLE_IMAGE = "https://www.google.com/search?tbm=isch&q=";
	protected static final String WIKI = "https://en.wikipedia.org/wiki/";
	protected static final String GOOGLE_TRANSLATE = "https://translate.google.it/#en/it/";

	protected static final String linkDelim = "�";

	Point backupScopenotesSelection = null; // Backup of scopenotetext selection
	int backupScopenotesCaretPosition = -1; // Backup of caret position for scopenotes, used in the paste function

	Text textScopenotes; // text box which contains the scopenotes
	Link linkScopenotes; // text box which contains the links of the scopenotes

	/**
	 * Set the current term
	 * 
	 * @param term
	 */
	public void setTerm(Term term) {

		if (term == null || term.getScopenotes() == null) {
			textScopenotes.setText("");
			linkScopenotes.setText("");
			return;
		}

		// get an instance of the global manager
		GlobalManager manager = GlobalManager.getInstance();

		// if read only show scopenotes without links, otherwise show also links
		if (manager.isReadOnly())
			textScopenotes.setText(term.getNotesWithoutLink(linkDelim, term.getScopenotes()));
		else
			textScopenotes.setText(term.getScopenotes());

		// get the links from the scopenotes
		linkScopenotes.setText(term.getHtmlLinksFromNotes(linkDelim, term.getScopenotes()));
	}

	/**
	 * Set the editability of scopenotes
	 */
	public void setEditable(boolean editable) {
		textScopenotes.setEditable(editable);
	}

	public void setEnabled(boolean enabled) {
		textScopenotes.setEnabled(enabled);
		linkScopenotes.setEnabled(enabled);
	}

	/**
	 * Get the text box of the scopenotes
	 * 
	 * @return
	 */
	public Text getTextScopenotes() {
		return textScopenotes;
	}

	/**
	 * Constructor, create scopenotes and links Create also the scopenotes
	 * contextual menu with the button for searching on the web the words
	 * 
	 * @param parent
	 */
	public ScopenotesWithLinks(final Composite parent) {

		// ### Scope notes ###
		textScopenotes = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.NONE);
		textScopenotes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		textScopenotes.setEditable(false);

		// ### Links ###
		// Create the Link textbox
		linkScopenotes = new Link(parent, SWT.BORDER);
		linkScopenotes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Listener to open clicked URLs
		linkScopenotes.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) { // Called when a Hyperlink is clicked
				// Open the default browser using the clicked link
				org.eclipse.swt.program.Program.launch(e.text);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		/*
		 * ==============================================================
		 * 
		 * MENU OF SCOPENOTES TEXT - SEARCHING ON THE WEB
		 * 
		 * In the following it is created the right click menu which allows to navigate
		 * in the Internet. In particular, it is possible to search on google, google
		 * image and Wikipedia the selected text
		 * 
		 * ==============================================================
		 */

		Display display = parent.getDisplay();
		ClassLoader classLoader = ScopenotesWithLinks.class.getClassLoader();

		// Right click menu for search results
		final Menu scopenotesMenu = new Menu(parent.getShell(), SWT.POP_UP);
		
		// Create the buttons, their names and images
		final MenuItem googleWeb = new MenuItem(scopenotesMenu, SWT.PUSH);
		googleWeb.setImage(new Image(display, classLoader.getResourceAsStream("GoogleIcon.png")));

		final MenuItem googleImage = new MenuItem(scopenotesMenu, SWT.PUSH);
		googleImage.setImage(new Image(display, classLoader.getResourceAsStream("GoogleImageIcon.png")));

		final MenuItem wikipedia = new MenuItem(scopenotesMenu, SWT.PUSH);
		wikipedia.setImage(new Image(display, classLoader.getResourceAsStream("WikipediaIcon.png")));

		final MenuItem googleTranslate = new MenuItem(scopenotesMenu, SWT.PUSH);
		googleTranslate.setImage(new Image(display, classLoader.getResourceAsStream("GoogleIcon.png")));

		// Associate the menu to the textScopenotes
		textScopenotes.setMenu(scopenotesMenu);

		// Enable menu commands only if there is some text selected
		textScopenotes.addMouseListener(new MouseListener() {

			/**
			 * When mouse buttons are released in the textscopenotes
			 */
			@Override
			public void mouseUp(MouseEvent e) {

				// right click
				if (e.button == 3) {

					// Backup previous selection if necessary
					if (backupScopenotesSelection != null) {
						textScopenotes.setSelection(backupScopenotesSelection);
					}

					// If no text is selected or at most one word is selected
					if (textScopenotes.getSelectionText().isEmpty()
							|| wordSelectedCount(textScopenotes.getSelectionText()) == 1) {

						// Get the caret bound of the clicked word to select it
						int[] wordBounds = getWordBounds(textScopenotes.getText(), textScopenotes.getCaretPosition());

						// Select the word right clicked
						textScopenotes.setSelection(wordBounds[0], wordBounds[1]);

					}

					// After moving the caret we open the menu

					// There is some selected text? If yes enable the buttons
					String selectedText = textScopenotes.getSelectionText();
					boolean enableMenu = !selectedText.isEmpty();

					scopenotesMenu.setVisible(enableMenu);
					googleWeb.setEnabled(enableMenu);
					googleImage.setEnabled(enableMenu);
					wikipedia.setEnabled(enableMenu);
					googleTranslate.setEnabled(enableMenu);

					// If there is some text selected show the buttons with the selected text
					// in their title
					if (enableMenu) {

						// max character which are displayed, max is set up to 20
						// (avoid to have too long words in the menu)
						int maxCharacters = Math.min(20, selectedText.length());

						// Get the text substring
						String subsetSelectedText = selectedText.substring(0, maxCharacters);

						// Add dots if the selected text is longer than maxCharacter
						if (selectedText.length() > maxCharacters) {
							subsetSelectedText += "..."; //$NON-NLS-1$
						}

						// Set the buttons text using the selected text
						googleWeb.setText(CBMessages.getString("ScopenotesWithLinks.GoogleButton", subsetSelectedText));
						googleImage.setText(
								CBMessages.getString("ScopenotesWithLinks.GoogleImageButton", subsetSelectedText));
						wikipedia.setText(
								CBMessages.getString("ScopenotesWithLinks.WikipediaButton", subsetSelectedText));
						googleTranslate.setText(
								CBMessages.getString("ScopenotesWithLinks.GoogleTranslateButton", subsetSelectedText));
					}
				}
			}

			/**
			 * When mouse buttons are pressed but not released yet in the textscopenotes
			 */
			@Override
			public void mouseDown(MouseEvent e) {

				// Used in the paste function to insert text in the correct place
				backupScopenotesCaretPosition = textScopenotes.getCaretPosition();

				// If right click then:
				// if there is no selection or we have selected only one word =>
				// select the clicked word automatically and open the menu
				// if more than one words are selected => open the menu
				if (e.button == 3) {

					// If the user selected more than one word then save the selection,
					// it is used to fix the eclipse bug (no caret shifting with right click)
					if (wordSelectedCount(textScopenotes.getSelectionText()) > 1) {
						// backup the selection of the text
						backupScopenotesSelection = textScopenotes.getSelection();
					} else { // otherwise reset the selection
						backupScopenotesSelection = null;
					}

					// Instantiate a robot
					// Simulate a right click con the textScopenotes in order to
					// move the text caret with right clicks
					// it is an eclipse bug, the right click does not move the caret
					// so this is a fix to the bug

					Robot robot;
					try {

						robot = new Robot();

						// Get the position of the right clicked event in the monitor
						Point mouseLocation = display.getCursorLocation();

						// Move the mouse in that position and make a left click simulation
						robot.mouseMove(mouseLocation.x, mouseLocation.y);
						robot.mousePress(InputEvent.BUTTON1_MASK); // call mouseDown
						robot.mouseRelease(InputEvent.BUTTON1_MASK); // call mouseUp

						// Here the caret is moved to the position of the right click as
						// consequence of the simulated left click

					} catch (AWTException e1) {
						e1.printStackTrace();
					}

				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		// In the following are coded the actions which are taken when
		// the user clicks the menu items

		/* Search on google web if googleWeb is clicked */
		googleWeb.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// launch the google web link
				launchLink(GOOGLE_WEB);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		/* Search on google image if googleImage is clicked */
		googleImage.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// launch the google img link
				launchLink(GOOGLE_IMAGE);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		/* Search on Wikipedia if wikipedia is clicked */
		wikipedia.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// launch the wiki link
				launchLink(WIKI);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		/* Search on Google translate if Google Translate is clicked */
		googleTranslate.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// launch the google translate link
				launchLink(GOOGLE_TRANSLATE);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	/**
	 * method used to launch the link with input informaion
	 * 
	 * @author shahaal
	 * @param link
	 */
	private void launchLink(String link) {

		// Get scopenotes selected text
		String selectedText = textScopenotes.getSelectionText();

		// If it is not empty
		if (!selectedText.isEmpty()) {
			// open the default browser and translate with google translate
			org.eclipse.swt.program.Program.launch(link + selectedText);
		}
	}

	/**
	 * Count the number of word selected with the mouse
	 * 
	 * @param selectedText
	 * @return
	 */
	private int wordSelectedCount(String selectedText) {

		// word delimiters
		String delimiters = " \n\t"; //$NON-NLS-1$

		int wordCount = 0;
		// Recognize where links start using the delim character
		StringTokenizer st = new StringTokenizer(selectedText, delimiters);

		// Get the first token (it is the note text)
		while (st.hasMoreTokens()) {
			st.nextToken();
			wordCount++;
		}

		return (wordCount);
	}

	/**
	 * Function which finds the right bound of the selected word in terms of caret
	 * position
	 * 
	 * @param text
	 * @param position
	 * @return
	 */
	private int retrieveWordOnTheRight(String text, int position) {

		if (position == text.length()) {
			return position;
		}

		int rightPosition = position; // start from the caret position

		// Go forward until space occurred or end of string occurred
		char currentChar = text.charAt(rightPosition);

		if (currentChar != ' ') {

			while (rightPosition < text.length() - 1 && currentChar != ' ') {
				currentChar = text.charAt(rightPosition + 1);
				rightPosition = rightPosition + 1;
			}

			// If the last char was a space
			if (currentChar == ' ') {
				// do not consider it in the selection
				rightPosition = rightPosition - 1;
			}

			return (rightPosition);
		} else {
			rightPosition = rightPosition - 1; // delete the space
			return (rightPosition);
		}
	}

	/**
	 * Function which finds the left bound of the selected word in terms of caret
	 * position
	 * 
	 * @param text
	 * @param position
	 * @return
	 */
	private int retrieveWordOnTheLeft(String text, int position) {

		if (position == 0) {
			return (position);
		}

		int leftPosition = position - 1; // try to go on the left to see what's happening

		// Go backward until a space occurred or start of string occurred
		char currentChar = text.charAt(leftPosition);

		// if the left character is not again a space character
		if (currentChar != ' ') {

			// try to go on the left to retrieve a word
			while (leftPosition != 0 && currentChar != ' ') {
				currentChar = text.charAt(leftPosition - 1);
				leftPosition = leftPosition - 1;
			}

			// If the last char was a space
			if (currentChar == ' ') {
				// do not consider it in the selection
				leftPosition = leftPosition + 1;
			}

			return (leftPosition);
		}

		else {
			return (position);
		}
	}

	/**
	 * Given a text and given a caret on that text, this method return the bounds of
	 * the word on which the caret is lying
	 * 
	 * @param text
	 * @param caretPosition
	 * @return
	 */
	private int[] getWordBounds(String text, int caretPosition) {

		// Left and right cursors
		int leftPosition = caretPosition;
		int rightPosition = caretPosition;

		// if we are at the end of the string then try to retrieve the word on the left
		if (caretPosition == text.length()) {
			leftPosition = retrieveWordOnTheLeft(text, leftPosition);
			return (new int[] { leftPosition, rightPosition });
		}

		// Try to retrieve the word
		leftPosition = retrieveWordOnTheLeft(text, leftPosition);
		rightPosition = retrieveWordOnTheRight(text, rightPosition);

		/*
		 * this condition means that we have encountered a space as the first selected
		 * char and we are not in the case that on the left there is no word (beginning
		 * of the text) This code is used to select the word on the left if the first
		 * char selected was a space.
		 * 
		 * For example, consider the text: hello world! if the cursor is moved to the
		 * right of the 'o' character of hello the selected char will be the white space
		 * between the two words
		 * 
		 * With the following code then we are trying to select the "hello" word
		 */

		if (leftPosition == rightPosition && leftPosition != 0) {

			leftPosition = retrieveWordOnTheLeft(text, leftPosition);
		}

		// Return the bound of the word in terms of text caret
		return (new int[] { leftPosition, rightPosition + 1 });
	}
}
