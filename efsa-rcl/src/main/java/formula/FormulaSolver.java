package formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import providers.ITableDaoService;
import providers.TableDaoService;
import table_database.TableDao;
import table_skeleton.TableColumn;
import table_skeleton.TableRow;
import xlsx_reader.TableHeaders.XlsxHeader;
import xlsx_reader.TableSchema;

/**
 * Class which solves the formulas included in the columns schema
 * default values. In particular, the formulas are solved such that the dependencies
 * across different columns are managed.
 * @author avonva
 * @author shahaal
 *
 */
public class FormulaSolver {

	private TableRow row;
	private ITableDaoService daoService;
	
	/**
	 * Initialise the solver.
	 * @param row row that contains the values for solving the formulas
	 */
	public FormulaSolver(TableRow row) {
		this(row, new TableDaoService(new TableDao())); // TODO to be removed
	}
	
	public FormulaSolver(TableRow row, ITableDaoService daoService) {
		this.row = row;
		this.daoService = daoService;
	}
	
	/**
	 * Solve a single formula of a specific column of the row using the row values. The formula
	 * that is used it the one contained in the chosen {@code fieldHeader}
	 * header of the column properties (as, Id, defaultValue, defaultCode...)
	 * @throws FormulaException 
	 */
	public Formula solve(TableColumn column, String fieldHeader) throws FormulaException {

		// get all the formulas
		FormulaParser parser = new FormulaParser(row, daoService);
		Formula formula = parser.parse(column, fieldHeader);
		formula.solve();
		
		return formula;
	}
	
	/**
	 * Solve all the formulas related to a single column property
	 * ({@link XlsxHeader}) for all the row fields specified
	 * in the {@link TableSchema}. This method manages also the
	 * interdependency of the formulas, guaranteeing that the 
	 * values are correct.
	 * @param fieldHeader
	 * @return
	 * @throws FormulaException 
	 */
	public ArrayList<Formula> solveAll(String fieldHeader) throws FormulaException {

		ArrayList<Formula> solvedFormulas = new ArrayList<>();
		
		// get all the formulas
		FormulaParser parser = new FormulaParser(row, daoService);
		
		ArrayList<Formula> formulas = parser.parse(fieldHeader);

		// sort the formulas based on their number of dependencies
		Collections.sort(formulas, new DependenciesSorter());
		
		// solve all the formulas starting from the
		// formulas with 0 dependencies
		for (Formula formula : formulas) {

			// solve the formula and get the resolved text
			String solvedFormula = formula.solve();
			
			// skip if no value is found
			if (solvedFormula == null || solvedFormula.isEmpty())
				continue;

			row.update(formula.getColumn(), solvedFormula, fieldHeader);
			
			// save solved formula
			solvedFormulas.add(formula);
		}
		
		return solvedFormulas;
	}

	/**
	 * Sorter that sort the formulas based on their number of dependencies
	 * @author avonva
	 *
	 */
	private class DependenciesSorter implements Comparator<Formula> {

		@Override
		public int compare(Formula arg0, Formula arg1) {
			
			int dep0 = arg0.getDependenciesCount();
			int dep1 = arg1.getDependenciesCount();
			
			if (dep0 == dep1)
				return 0;
			else if (dep0 < dep1)
				return -1;
			else
				return 1;
		}
	}
}
