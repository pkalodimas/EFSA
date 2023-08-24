package table_dialog;

import java.io.IOException;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import global_utils.Warnings;
import i18n_messages.Messages;
import table_database.TableDao;
import table_dialog.DialogBuilder.RowCreationMode;
import table_dialog.RowCreatorViewer.CatalogChangedListener;
import table_list.TableMetaData;
import table_skeleton.TableCell;
import table_skeleton.TableColumn;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;
import xml_catalog_reader.Selection;

/**
 * Generic dialog for showing a table that follows a {@link TableSchema}.
 * 
 * Each custom dialog that needs to show the content of a table of this type
 * should extend this class.
 * 
 * In particular, remember that only the tables identified in the
 * {@link CustomPaths#APP_CONFIG_FILE} .xslx file are considered.
 * 
 * Features that are provided by this class:
 * <ul>
 * <li>automatic creation of table with {@link TableColumn} which are related to
 * a column which is visible.</li>
 * 
 * <li>automatic editor added to all the columns which are marked as editable
 * according to the column type.</li>
 * 
 * <li>automatic generation of {@link HelpViewer} to show help to the user with
 * .html files.</li>
 * 
 * <li>automatic generation of new rows by pressing a button:
 * <ul>
 * <li>It is possible to add a simple button by setting {@link #mode} to
 * {@link RowCreationMode#STANDARD} in the constructor of the class.
 * <li>If instead it is needed to first select an element from a catalogue to
 * Initialise a new row, then set {@link #mode} to
 * {@link RowCreationMode#SELECTOR}, in order to have a {@link RowCreatorViewer}
 * which allows creating new rows in the table by selecting a parameter from a
 * list.
 * <li>In both cases if the plus icon is pressed, the
 * {@link #createNewRow(TableSchema, Selection)} method is called. To disable
 * the addition of new rows, simply put as {@link #mode} the value
 * {@link RowCreationMode#NONE}.</li>
 * </ul>
 * 
 * <li>automatic generation of a {@link Button} to apply changes. Need to be
 * specified by setting {@link #addSaveBtn} to true. If the button is pressed
 * the {@link #apply(TableSchema, Collection, TableRow)} method is called.</li>
 * 
 * <li>You can specify if the table should be shown in a new dialog or in an
 * already existing shell by setting {@link #createPopUp} accordingly.</li>
 * </ul>
 * 
 * @author avonva
 * @author shahaal
 *
 */
public abstract class TableDialog {

	private static final Logger LOGGER = LogManager.getLogger(TableDialog.class);

	private Shell parent;
	private Shell dialog;
	private DialogBuilder panel;
	private Button saveButton;

	private TableRow parentFilter; // if set, only the rows children of this parent are shown in the table
	private TableSchema schema; // schema of the table that is shown

	private String title;
	private boolean createPopUp;
	private boolean addSaveBtn;
	private boolean autoSave; // if rows should be auto saved in the db

	private EditorListener editorListener;

	private String type = "type";

	/**
	 * Create a dialog with a {@link HelpViewer}, a {@link TableView} and possibly a
	 * {@link RowCreatorViewer} if {@code addSelector} is set to true.
	 * 
	 * @param parent      the shell parent
	 * @param title       the title of the pop up (used only if {@code createPopUp}
	 *                    is true)
	 * @param helpMessage the help message to be displayed in the {@link HelpViewer}
	 * @param editable    if the table can be edited or not
	 * @param mode        see {@link TableDialog}
	 * @param createPopUp true to create a new shell, false to use the parent shell
	 * @param addSaveBtn  true to create a button below the table
	 */
	public TableDialog(Shell parent, String title, boolean createPopUp, boolean addSaveBtn) {
		this(parent, title, createPopUp, addSaveBtn, true);
	}

	public TableDialog(Shell parent, String title, boolean createPopUp, boolean addSaveBtn, boolean autoSave) {

		this.parent = parent;
		this.title = title;
		this.createPopUp = createPopUp;
		this.addSaveBtn = addSaveBtn;
		this.autoSave = autoSave;

		this.schema = TableSchemaList.getByName(getSchemaSheetName());
		this.schema.sort();
	}

	/**
	 * Create the interface
	 */
	protected Shell create() {

		// new shell if required
		if (createPopUp) {

			this.dialog = new Shell(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
			this.dialog.setText(this.title);

			// inherit also the icon
			Image image = parent.getImage();
			if (image != null)
				this.dialog.setImage(image);
		} else
			this.dialog = parent;

		this.dialog.setLayout(new GridLayout(1, false));
		this.dialog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		this.panel = new DialogBuilder(dialog);

		// add all the required widgets to the panel
		addWidgets(this.panel);

		if (!this.panel.isTableDefined()) {
			LOGGER.error("Cannot instantiate TableDialog without a table. Please check addWidgets().");
			return null;
		}

		this.panel.setMenu(createMenu());

		// set the validator label provider
		RowValidatorLabelProvider validator = getValidator();
		if (validator != null)
			this.panel.setValidatorLabelProvider(validator);

		// help listener
		this.panel.addHelpListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				showHelp();
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}
		});

		// avoid saving without applying changes
		this.panel.addTableEditorListener(new EditorListener() {

			@Override
			public void editStarted() {

				if (editorListener != null)
					editorListener.editStarted();

				if (saveButton != null)
					saveButton.setEnabled(false);
			}

			@Override
			public void editEnded(TableRow row, TableColumn field, boolean changed) {

				if (editorListener != null)
					editorListener.editEnded(row, field, changed);

				if (changed) {
					panel.getTable().refreshAndSave(row, autoSave);
				}

				if (saveButton != null)
					saveButton.setEnabled(panel.areMandatoryFilled());
			}
		});

		// add a selection listener to the selector
		this.addSelectionListener(new CatalogChangedListener() {

			@Override
			public void catalogConfirmed(Selection selectedItem) {
				addNewRow(selectedItem);
			}

			@Override
			public void catalogChanged(Selection selectedItem) {
			}
		});

		// added ability to directly open next level by double clicking
		this.addTableDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				nextLevel();
			}
		});

		// load all the rows into the table
		loadRows();

		if (addSaveBtn) {
			// save button
			this.saveButton = new Button(dialog, SWT.PUSH);
			this.saveButton.setText(Messages.get("save.button"));
			this.saveButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
			this.saveButton.setEnabled(panel.areMandatoryFilled());

			// save options
			this.saveButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {

					boolean ok = apply(panel.getSchema(), panel.getTableElements(), panel.getSelection());

					if (ok)
						dialog.close();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			});

			panel.setInputChangedListener(new Listener() {

				@Override
				public void handleEvent(Event arg0) {
					saveButton.setEnabled(panel.areMandatoryFilled());
				}
			});
		}

		dialog.pack();

		// make dialog longer
		dialog.setSize(dialog.getSize().x, dialog.getSize().y + 50);

		return dialog;
	}

	/**
	 * Open the dialog and wait that it is closed
	 */
	public void open() {

		// create the dialog if not created before
		if (dialog == null)
			create();

		this.dialog.open();

		// Event loop
		while (!dialog.isDisposed()) {
			if (!dialog.getDisplay().readAndDispatch())
				dialog.getDisplay().sleep();
		}
	}

	/**
	 * Save all the rows in the db this will work only if {@code autoSave} is false
	 * 
	 * @param rows
	 */
	public void saveRows(TableRow... rows) {

		if (autoSave)
			return;

		for (TableRow row : rows)
			panel.refreshAndSave(row, true);
	}

	/**
	 * Load the rows which are defined in the
	 * {@link #loadInitialRows(TableSchema, TableRow)} method
	 */
	public void loadRows() {

		panel.clearTable();

		// load the rows
		Collection<TableRow> rows = loadInitialRows(panel.getSchema(), parentFilter);

		// skip if null parameter
		if (rows == null)
			return;

		// add them to the table
		panel.addAll(rows);
	}

	/**
	 * If the current table is in many to one relation with a parent table, then use
	 * this method to set the parent. This will be passed then to the
	 * {@link #loadContents(TableSchema)} methods in order to allow loading just the
	 * records related to the parent table.
	 * 
	 * @param parentTable
	 */
	public void setParentFilter(TableRow parentFilter) {

		if (parentFilter == null)
			return;

		// save the new filter
		this.parentFilter = parentFilter;

		this.panel.clearTable();

		TableRowList rows = getRows();
		this.panel.setInput(rows);
	}

	public void setRows(TableRowList rows) {
		this.panel.setInput(rows);
	}

	public TableRowList getLoadedRows() {
		return this.panel.getTable().getTableElements();
	}

	public void setParents(TableRow... parents) {

		if (this.panel.getTable() == null) {
			LOGGER.error("Cannot setParents on panel builder without table");
			return;
		}

		this.panel.getTable().clear();
		for (TableRow parent : parents) {
			this.panel.getTable().addParentTable(parent);
		}
	}

	/**
	 * Clear all the table rows and the parent table object
	 */
	public void clear() {

		panel.clearTable();
		this.parentFilter = null;

		// disable the panel
		this.panel.setEnabled(false);
	}

	/**
	 * Set the text of the button
	 * 
	 * @param text
	 */
	public void setButtonText(String text) {

		if (!addSaveBtn) {
			LOGGER.error("DataDialog-" + getSchemaSheetName() + ":Cannot set the button text to " + text
					+ " since the button was not created. Please set addSaveBtn to true");
			return;
		}

		this.saveButton.setText(text);
		this.saveButton.pack();
	}

	/**
	 * Enable/disable the creation of new records
	 * 
	 * @param enabled
	 */
	public void setRowCreationEnabled(boolean enabled) {
		this.panel.setEnabled(enabled);
	}

	/**
	 * Refresh a single row of the table
	 * 
	 * @param row
	 */
	public void refreshAndSave(TableRow row, boolean saveInDb) {
		this.panel.refreshAndSave(row, saveInDb);
	}

	public void refresh(TableRow row) {
		this.panel.refresh(row);
	}

	public void refreshValidator(TableRow row) {
		this.panel.refreshValidator(row);
	}

	public void replace(TableRow row) {
		this.panel.replace(row);
	}

	/**
	 * Refresh the entire table
	 */
	public void refresh() {
		this.panel.refresh();
	}

	/**
	 * Change table editability
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.panel.setTableEditable(editable);
	}

	public boolean isEditable() {
		return this.panel.isTableEditable();
	}

	/**
	 * Show the html help if possible
	 */
	private void showHelp() {

		TableMetaData help = TableMetaData.getTableByName(getSchemaSheetName());

		// if no help found return
		if (help == null)
			return;

		// open the help viewer
		help.openHelp();
	}

	/**
	 * Get the parent table if it was set
	 * 
	 * @return
	 */
	public TableRow getParentFilter() {
		return parentFilter;
	}

	public Shell getParent() {
		return parent;
	}

	/**
	 * Get the table schema
	 * 
	 * @return
	 */
	public TableSchema getSchema() {
		return schema;
	}

	/**
	 * Get the selected row
	 * 
	 * @return
	 */
	public TableRow getSelection() {
		return this.panel.getSelection();
	}

	/**
	 * Get the panel builder
	 * 
	 * @return
	 */
	public DialogBuilder getPanelBuilder() {
		return this.panel;
	}

	/**
	 * Get the shell related to this window
	 * 
	 * @return
	 */
	public Shell getDialog() {
		return dialog;
	}

	/**
	 * Set the window size
	 * 
	 * @param width
	 * @param height
	 */
	public void setSize(int width, int height) {
		this.dialog.setSize(width, height);
	}

	/**
	 * Set the window height
	 * 
	 * @param height
	 */
	public void setHeight(int height) {
		setSize(dialog.getSize().x, height);
	}

	/**
	 * Add height to the dialog
	 * 
	 * @param height
	 */
	public void addHeight(int height) {
		setSize(dialog.getSize().x, dialog.getSize().y + height);
	}

	/**
	 * Warn the user with an ERROR message box
	 * 
	 * @param title
	 * @param message
	 * @param icon
	 */
	public int warnUser(String title, String message, int icon) {
		return Warnings.warnUser(getDialog(), title, message, icon);
	}

	/**
	 * Warn the user with a message box with error icon
	 * 
	 * @param title
	 * @param message
	 */
	public int warnUser(String title, String message) {
		return Warnings.warnUser(getDialog(), title, message, SWT.ICON_ERROR);
	}

	/**
	 * Add a new row to the table given a selected catalogue item (it can be null)
	 * 
	 * @param selectedItem
	 */
	private void addNewRow(Selection selectedItem) {

		// create a new row and
		// put the first cell in the row
		TableRow row = createNewRow(getSchema(), selectedItem);

		if (row == null)
			return;

		// Initialise the row fields with default values
		row.Initialise();

		// insert the row and save also the row id
		row.save();

		// Initialise the formulas with row id
		row.Initialise();

		// update the formulas
		row.updateFormulas();

		// update the row with the formulas solved
		row.update();

		// add the row to the table
		add(row);

		// call external function
		processNewRow(row);
	}

	/**
	 * shahaal create a clone row of the passed row
	 * 
	 * @param selectedItem
	 * @param row
	 */
	private void addNewCloneRow(Selection selectedItem, TableRow row) {

		// create a new row and
		// put the first cell in the row
		TableRow cloneRow = createNewRow(getSchema(), selectedItem);

		if (cloneRow == null)
			return;

		// Initialise the row fields with default values
		cloneRow.Initialise();

		// insert the row and save also the row id
		cloneRow.save();

		// Initialise the formulas with row id
		cloneRow.Initialise();

		// fill editable fields
		for (TableColumn col : this.getSchema()) {
			if (col.isEditable(row))
				cloneRow.put(col.getId(), row.get(col.getId()));
		}

		// update the formulas
		cloneRow.updateFormulas();

		// update the row with the formulas solved
		cloneRow.update();

		// add the row to the table
		add(cloneRow);

		// call external function
		processNewRow(cloneRow);
	}

	/**
	 * Add a row to the table
	 * 
	 * @param row
	 */
	public void add(TableRow row) {
		panel.add(row);
	}

	/**
	 * Remove from the table the selected row
	 */
	public void removeSelectedRow() {
		panel.removeSelectedRow();
	}

	/**
	 * clone the selected rows
	 * 
	 * @author shahaal
	 */
	public void cloneSelectedRow() {

		// return if table null
		if (panel.getTable() == null)
			return;

		// get the selected elements in the table
		TableRowList list = panel.getTable().getAllSelectedRows();

		// return if list empty
		if (list == null || list.isEmpty())
			return;

		for (TableRow row : list) {

			// get the type of the row
			TableCell cell = row.get(type);

			// create a new clone row
			if (!cell.isEmpty())
				addNewCloneRow(new Selection(cell), row);
		}
	}

	/**
	 * Get the record of the current table. If no {@link #parentFilter} was set,
	 * then all the rows are returned, otherwise the filtered rows are returned.
	 * 
	 * @return
	 */
	public TableRowList getRows() {
		TableRowList rows;

		// load parents rows
		TableDao dao = new TableDao();

		// if no filter get all
		if (parentFilter == null) {
			rows = dao.getAll(schema);
		}
		else {
			LOGGER.info("GetRows with filter=" + parentFilter);
			rows = dao.getByParentId(schema, parentFilter.getSchema().getSheetName(), parentFilter.getDatabaseId(),
					true);
		}
		return rows;
	}

	/**
	 * Add listener to the selector if it was added (i.e. {@link #addSelector} true)
	 * 
	 * @param listener
	 */
	public void addSelectionListener(CatalogChangedListener listener) {
		this.panel.addSelectionListener(listener);
	}

	/**
	 * Called when an element of the table is selected
	 * 
	 * @param listener
	 */
	public void addTableSelectionListener(ISelectionChangedListener listener) {
		this.panel.addSelectionChangedListener(listener);
	}

	/**
	 * Called when an element of the table is double clicked
	 * 
	 * @param listener
	 */
	public void addTableDoubleClickListener(IDoubleClickListener listener) {
		this.panel.addDoubleClickListener(listener);
	}

	/**
	 * Check if the table is empty or not
	 * 
	 * @return
	 */
	public boolean isTableEmpty() {
		return this.panel.isTableEmpty();
	}

	/**
	 * Called if a row is edited
	 * 
	 * @param editorListener
	 */
	public void setEditorListener(EditorListener editorListener) {
		this.editorListener = editorListener;
	}

	/**
	 * Create the structure of the table. Use the {@code viewer} methods to add
	 * widgets.
	 * 
	 * @param viewer
	 */
	public abstract void addWidgets(DialogBuilder viewer);

	/**
	 * Get the sheet which includes the schema for the table
	 * 
	 * @return
	 */
	public abstract String getSchemaSheetName();

	/**
	 * Create a menu for the table
	 * 
	 * @return
	 */
	public abstract Menu createMenu();

	/**
	 * Get the rows of the table when it is created. If there is no particular need,
	 * just return the {@link #getRows()} method to load all the table rows
	 * (possibly filtered by the {@link #parentFilter}).
	 * 
	 * @param schema       the table schema
	 * @param parentFilter the parent related to this table passed with the method
	 *                     {@link #setParentFilter(TableRow)}. Can be null if it was
	 *                     not set.
	 * @return
	 */
	public abstract Collection<TableRow> loadInitialRows(TableSchema schema, TableRow parentFilter);

	/**
	 * Create a new row with default values and foreign keys when the selector is
	 * used. Note that {@link #addSelector} should be set to true, otherwise no
	 * selector will be available and this method will never be called
	 * 
	 * @param element the element which is currently selected in the selector
	 * @return
	 * @throws IOException
	 */
	public abstract TableRow createNewRow(TableSchema schema, Selection type);

	/**
	 * Process the newly created row if necessary. Note that the row was already
	 * added to the db and has its id set.
	 * 
	 * @param row
	 */
	public abstract void processNewRow(TableRow row);

	/**
	 * open next level with double click on row
	 * 
	 */
	public abstract void nextLevel();

	/**
	 * Apply the changes that were made when the {@link #saveButton} is pressed.
	 * Note that the {@link #addSaveBtn} should be set to true to show the button,
	 * otherwise this method will never be called.
	 * 
	 * @param schema      the table schema
	 * @param rows        the table rows
	 * @param selectedRow the current selected row in the table (null if no
	 *                    selection)
	 * @return true if the current dialog should be closed or false otherwise
	 */
	public abstract boolean apply(TableSchema schema, Collection<TableRow> rows, TableRow selectedRow);

	/**
	 * Get a validator that highlights the errors for each row. Only implement this
	 * if {@link #editable} was set to true.
	 * 
	 * @return
	 */
	public abstract RowValidatorLabelProvider getValidator();
}
