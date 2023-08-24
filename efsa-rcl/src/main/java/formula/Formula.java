package formula;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import providers.ITableDaoService;
import table_skeleton.TableColumn;
import table_skeleton.TableRow;

/**
 * Class which models a generic formula which can be inserted in the .xlsx
 * configuration files for creating ReportTable. The formula can also be solved
 * by invoking {@link #solve()}
 * 
 * @author avonva
 * @author shahaal
 *
 */
public class Formula {

	private static final Logger LOGGER = LogManager.getLogger(Formula.class);

	private static HashMap<Cell, Integer> dependenciesCache;

	private String formula;
	private String solvedFormula;
	private String fieldHeader;
	private TableRow row;
	private TableColumn column;
	private int dependenciesCount;

	private ITableDaoService daoService;

	// private long debugTime;

	public Formula(TableRow row, TableColumn column, String fieldHeader, ITableDaoService daoService) {

		if (dependenciesCache == null)
			dependenciesCache = new HashMap<>();

		this.row = row;
		this.column = column;
		this.fieldHeader = fieldHeader;
		this.formula = column.getFieldByHeader(fieldHeader);
		this.daoService = daoService;

		this.dependenciesCount = 0;

		evalDependencies();

	}

	/**
	 * Get the number of dependencies in terms of \columnname.field
	 * 
	 * @return
	 */
	public int getDependenciesCount() {
		return dependenciesCount;
	}

	public TableRow getRow() {
		return row;
	}

	public TableColumn getColumn() {
		return column;
	}

	/**
	 * Get the solved formula. Can be used only after calling {@link #solve()},
	 * otherwise it returns null.
	 * 
	 * @return
	 */
	public String getSolvedFormula() {
		return solvedFormula;
	}

	public String getFormula() {
		return formula;
	}

	/**
	 * Solve the formula. Returns null if the field should be ignored
	 * 
	 * @return
	 * @throws FormulaException
	 */
	public String solve() throws FormulaException {

		if (formula == null || formula.isEmpty())
			return "";
		
		String value = formula;

		// solve special characters
		value = solveKeywords(value);
		value = solveRowKeywords(value);
		print(value, "KEYWORDS");

		// solve columns values if a row was passed
		value = solveColumnsFormula(value);
		print(value, "COLUMNS");

		// solve relations formulas
		value = solveRelationFormula(value);
		print(value, "RELATIONS");

		// solve logical == / !=
		value = solveLogicalOperators(value);
		print(value, "LOGIC OP");

		value = solveFunctionsFormula(value);
		print(value, "FUNCTIONS");

		this.solvedFormula = value.trim();
		
		return solvedFormula;
	}

	private void print(String value, String header) {
		if (column.getId().equals("sampId") && fieldHeader.equals("labelFormula"))
			LOGGER.info("Solving formula=" + value + " Solving formulas=" + header);
	}

	/**
	 * Solve the functions
	 * 
	 * @param value
	 * @return
	 * @throws FormulaException
	 */
	private String solveFunctionsFormula(String value) throws FormulaException {

		String command = value;

		String[] functionsOrder = new String[] { 
				FunctionFormula.AND, 
				FunctionFormula.OR, 
				FunctionFormula.SUM,
				FunctionFormula.ZERO_PADDING, 
				FunctionFormula.RIGHT_TRIM, 
				FunctionFormula.LEFT_TRIM, 
				FunctionFormula.IF, 
				FunctionFormula.IF_NOT_NULL,
				FunctionFormula.HASH, 
				FunctionFormula.NEXT };

		for (String function : functionsOrder) {
			
			FormulaList list = FormulaFinder.findFunctionFormulas(command, function);

			if (!list.isEmpty()) {
				command = replaceFormulasWithSolution(list, command, false);
				print(command, function);
			}
		}

		return command;
	}

	/**
	 * Resolve all columns dependencies (%column_name.(code|label)) with the columns
	 * values
	 * 
	 * @param value
	 * @return
	 * @throws FormulaException
	 * @throws IOException
	 */
	private String solveColumnsFormula(String value) throws FormulaException {
		FormulaList list = FormulaFinder.findColumnFormulas(value);
		return replaceFormulasWithSolution(list, value, true);
	}

	/**
	 * Solve all the RELATION(parent, field) statements
	 * 
	 * @param value
	 * @return
	 * @throws FormulaException
	 */
	private String solveRelationFormula(String value) throws FormulaException {
		FormulaList relFormulas = FormulaFinder.findRelationFormulas(value, daoService);
		return replaceFormulasWithSolution(relFormulas, value, true);
	}

	/**
	 * Solve the logical operators
	 * 
	 * @param value
	 * @return
	 * @throws FormulaException
	 */
	private String solveLogicalOperators(String value) throws FormulaException {
		FormulaList comparisonFormulas = new FormulaList();

		// compute equal
		comparisonFormulas.addAll(FormulaFinder.findComparatorFormulas(value, ComparatorFormula.EQUAL));

		// compute disequal
		comparisonFormulas.addAll(FormulaFinder.findComparatorFormulas(value, ComparatorFormula.DISEQUAL));

		return replaceFormulasWithSolution(comparisonFormulas, value, false);
	}

	/**
	 * Solve keywords with their values
	 * 
	 * @param value
	 * @return
	 * @throws FormulaException
	 */
	private String solveKeywords(String value) throws FormulaException {
		FormulaList list = FormulaFinder.findKeywordFormulas(value);
		return replaceFormulasWithSolution(list, value, false);
	}

	/**
	 * Solve keywords with their values (dependent on the row values)
	 * 
	 * @param value
	 * @return
	 * @throws FormulaException
	 */
	private String solveRowKeywords(String value) throws FormulaException {
		FormulaList list = FormulaFinder.findRowKeywordFormulas(value);
		return replaceFormulasWithSolution(list, value, true);
	}

	/**
	 * Get all the formulas in the list and replace their solved version into the
	 * text
	 * 
	 * @param formulas list of formulas that will be applied to the text
	 * @param text     string which contains formulas in plain text
	 * @param useRow   if the formulas should be solved using the row information or
	 *                 not
	 * @return the text with the solved formulas
	 * @throws FormulaException
	 */
	private String replaceFormulasWithSolution(FormulaList formulas, String text, boolean useRow)
			throws FormulaException {

		String command = text;

		for (IFormula f : formulas) {

			String solved = useRow ? f.solve(row) : f.solve();

			if (solved != null)
				command = command.replace(f.getUnsolvedFormula(), solved);
		}
		
		return command;
	}

	@Override
	public String toString() {
		return "Column " + column.getId() + " formula " + formula + " solved " + solvedFormula;
	}

	/**
	 * Check if a column has a dependency with another column of the same table
	 * 
	 * @param col
	 * @param value
	 * @return
	 */
	private int isDependentBy(TableColumn col, String value) {

		if (value == null)
			return 0;

		Pattern p = Pattern.compile("\\%" + col.getId() + "\\.(code|label)");
		Matcher m = p.matcher(value);

		int counter = 0;

		while (m.find())
			counter++;

		return counter;
	}

	/**
	 * Check dependencies in a recursive manner. This is actually the computation of
	 * the level of the tree of the dependencies. A column is dependent on the value
	 * of another column if it has in the field a formula with \columnName.code or
	 * \columnName.label
	 */
	private void evalDependencies() {

		Cell cell = new Cell(row.getSchema().getSheetName(), column.getId(), fieldHeader);
		Integer cacheDep = dependenciesCache.get(cell);

		// use cache if possible
		if (cacheDep != null) {
			this.dependenciesCount = cacheDep;
			return;
		}

		int dependencies = 0;

		// Check columns dependencies
		for (TableColumn col : row.getSchema()) {

			// evaluate the dependency just for different columns this avoid recursive
			// definitions
			if (!col.equals(column)) {

				int numOfDep = isDependentBy(col, formula);

				// add number of occurrences as dependencies
				dependencies = dependencies + numOfDep;

				// if we have dependencies (i.e. we found a %column...) also evaluate the column
				// to check if there are nested dependencies
				if (numOfDep > 0) {

					// evaluate column dependencies recursively
					Formula child = new Formula(row, col, fieldHeader, daoService);

					// also add the column dependencies to the current number of dependencies
					dependencies = dependencies + child.getDependenciesCount();
				}
			}
		}

		this.dependenciesCount = dependencies;

		// save cache
		dependenciesCache.put(cell, dependencies);

	}

	private class Cell {
		private String tableName;
		private String columnId;
		private String columnHeader;

		public Cell(String tableName, String columnId, String columnHeader) {
			this.tableName = tableName;
			this.columnId = columnId;
			this.columnHeader = columnHeader;
		}

		@Override
		public boolean equals(Object obj) {

			Cell cell = (Cell) obj;

			return tableName.equals(cell.tableName) && columnId.equals(cell.columnId)
					&& columnHeader.equals(cell.columnHeader);
		}
	}
}
