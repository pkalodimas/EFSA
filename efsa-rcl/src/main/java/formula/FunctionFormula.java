package formula;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app_config.BooleanValue;
import table_skeleton.TableRow;

/**
 * Manage and solve all the function that uses the syntax:
 * functionName(op1,op2,op3,...)
 * @author avonva
 * @author shahaal
 *
 */
public class FunctionFormula implements IFormula {

	private static final Logger LOGGER = LogManager.getLogger(FunctionFormula.class);
	
	public static final String ZERO_PADDING = "ZERO_PADDING";
	public static final String RIGHT_TRIM = "RIGHT_TRIM";
	public static final String LEFT_TRIM = "LEFT_TRIM";
	public static final String IF = "IF";
	public static final String IF_NOT_NULL = "IF_NOT_NULL";
	public static final String SUM = "SUM";
	public static final String HASH = "HASH";
	public static final String AND = "AND";
	public static final String OR = "OR";
	public static final String NEXT = "NEXT";
	
	private String formula;
	private String solvedFormula;
	private String functionName;
	private List<String> operands;
	
	public FunctionFormula(String formula) throws FormulaException {
		this.formula = formula;
		this.operands = new ArrayList<>();
		compile();
	}
	
	@Override
	public String getUnsolvedFormula() {
		return formula;
	}

	public String getSolvedFormula() {
		return solvedFormula;
	}
	
	@Override
	public void compile() throws FormulaException {
		
		// split on the first bracket to get the function name
		String split[] = formula.split("\\(");
		
		if (split.length != 2) {
			throw new FormulaException("Wrong function formula: " + formula);
		}

		// get the function name
		this.functionName = split[0];

		// get the operands
		String operandsSplit[] = split[1].split(",");
		
		// for each operand
		for (int i = 0; i < operandsSplit.length; ++i) {
			
			String operand = operandsSplit[i];
			
			// if last, remove right bracket
			if (i == operandsSplit.length - 1) {
				operand = operand.replace(")", "");
			}
			
			// save the operand in the list
			operands.add(operand);
		}
	}

	@Override
	public String solve() throws FormulaException {
		
		String solvedFormula = null;
		
		switch(this.functionName.toUpperCase()) {
		case ZERO_PADDING:
			solvedFormula = solvePadding();
			break;
		case RIGHT_TRIM:
			solvedFormula = solveRightTrim();
			break;
		case LEFT_TRIM:
			solvedFormula = solveLeftTrim();
			break;
		case IF:
			solvedFormula = solveIf();
			break;
		case IF_NOT_NULL:
			solvedFormula = solveIfNotNull();
			break;
		case SUM:
			solvedFormula = solveSum();
			break;
		case HASH:
			solvedFormula = solveHash();
			break;
		case AND:
			solvedFormula = solveAnd();
			break;
		case OR:
			solvedFormula = solveOr();
			break;
		case NEXT:
			solvedFormula = solveNext();
			break;
		default:
			throw new FormulaException("Function not supported: " + formula);
		}
		
		this.solvedFormula = solvedFormula;
		
		return solvedFormula;
	}
	
	@Override
	public String solve(TableRow row) throws FormulaException {
		throw new FormulaException("Not supported.");
	}
	
	/**
	 * Solve the padding
	 * @return
	 * @throws FormulaException
	 */
	private String solvePadding() throws FormulaException {
		
		if (operands.size() != 2) {
			throw new FormulaException("Wrong number of parameters " + formula 
					+ ". Expected 2, found " + operands.size());
		}
		
		// text which needs to be padded
		String text = operands.get(0);

		// try getting the number of characters needed
		int requiredPadding;
		try {
			requiredPadding = Integer.valueOf(operands.get(1));
		}
		catch (NumberFormatException e) {
			throw new FormulaException("Wrong parameters for " + formula);
		}

		// make zero padding if necessary up to the number of characters required
		if (requiredPadding > text.length()) {
			
			int paddingCount = requiredPadding - text.length();
			for(int i = 0; i < paddingCount; ++i) {
				text = "0" + text;
			}
		}
		
		return text;
	}
	
	/**
	 * Solve right trim
	 * @return
	 * @throws FormulaException
	 */
	private String solveRightTrim() throws FormulaException {
		
		if (operands.size() != 2) {
			throw new FormulaException("Wrong number of parameters " + formula 
					+ ". Expected 2, found " + operands.size());
		}
		
		// get the text to be trimmed
		String text = operands.get(0);
		
		// get number of characters to trim
		int charNum;
		try {
			charNum = Integer.valueOf(operands.get(1));
		}
		catch (NumberFormatException e) {
			throw new FormulaException("Wrong parameters for " + formula);
		}
		
		// error check
		if (charNum > text.length()) {
			charNum = text.length();
		}
		
		// trim the text
		String trimmedText = text.substring(text.length() - charNum, text.length());
		
		return trimmedText;
	}
	
	/**
	 * Solve left trim
	 * @return
	 * @throws FormulaException
	 */
	private String solveLeftTrim() throws FormulaException {
		
		if (operands.size() != 2) {
			throw new FormulaException("Wrong number of parameters " + formula 
					+ ". Expected 2, found " + operands.size());
		}
		
		// get the text to be trimmed
		String text = operands.get(0);
		
		// get number of characters to trim
		int charNum;
		try {
			charNum = Integer.valueOf(operands.get(1));
		}
		catch (NumberFormatException e) {
			throw new FormulaException("Wrong parameters for " + formula);
		}
		
		// error check
		if (charNum > text.length()) {
			charNum = text.length();
		}
		
		// trim the text
		String trimmedText = text.substring(0, charNum);
		
		return trimmedText;
	}
	
	/**
	 * Solve if
	 * @return
	 * @throws FormulaException
	 */
	private String solveIf() throws FormulaException {
		
		if (operands.size() != 3) {
			throw new FormulaException("Wrong number of parameters " + formula 
					+ ". Expected 3, found " + operands.size());
		}
		
		// get operands
		String condition = operands.get(0);
		String trueCond = operands.get(1);
		String falseCond = operands.get(2);
		
		// check if condition is true
		return BooleanValue.isTrue(condition) ? trueCond : falseCond;
	}
	
	/**
	 * Solve if with null conditions
	 * @return
	 * @throws FormulaException
	 */
	private String solveIfNotNull() throws FormulaException {
		
		if (operands.size() != 3) {
			throw new FormulaException("Wrong number of parameters " + formula 
					+ ". Expected 3, found " + operands.size());
		}
		
		// get operands
		String condition = operands.get(0);
		String trueCond = operands.get(1);
		String falseCond = operands.get(2);
		
		// check if condition is empty (i.e. null)
		return !condition.isEmpty() ? trueCond : falseCond;
	}
	
	/**
	 * Solve a sum of elements
	 * @return
	 * @throws FormulaException
	 */
	private String solveSum() throws FormulaException {
		
		if (operands.size() < 2) {
			throw new FormulaException("Wrong number of parameters " + formula 
					+ ". Expected at least 2, found " + operands.size());
		}
		
		double totalSum = 0;
		for (String operand : operands) {
			
			// return empty value if empty operand
			if (operand.isEmpty()) {
				LOGGER.info("Warning: An operand of the SUM function is empty. Operands: " + operands + ". Returning empty value");
				return "";
			}
			
			try {
				double number = Double.valueOf(operand);
				
				totalSum = totalSum + number;
			}
			catch (NumberFormatException e) {
				throw new FormulaException("Wrong parameters for " + formula);
			}
		}
		
		String result = null;
		
		// cast to integer if it is an integer (avoid the .0 notation)
		if ((totalSum == Math.floor(totalSum)) && !Double.isInfinite(totalSum)) {
			// convert result to string
			result = String.valueOf((int)totalSum);
		}
		else {
			result = String.valueOf(totalSum);
		}
		
		// return the total sum
		return result;
	}
	
	/**
	 * Solve hash functions
	 * @return
	 * @throws FormulaException
	 */
	private String solveHash() throws FormulaException {
		
		if (operands.size() != 2) {
			throw new FormulaException("Wrong number of parameters " + formula 
					+ ". Expected 2, found " + operands.size());
		}
		
		String hashAlgorithm = operands.get(0);
		String value = operands.get(1);
		
		LOGGER.debug("Computing HASH function of=" + value);
		
		byte[] byteArray = value.getBytes();
		byte[] digest;
		try {
			digest = MessageDigest.getInstance(hashAlgorithm).digest(byteArray);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new FormulaException(e);
		}
		
		String hash = DatatypeConverter.printHexBinary(digest);
		
		return hash;
	}
	
	/**
	 * Solve and functions
	 * @return
	 * @throws FormulaException
	 */
	private String solveAnd() throws FormulaException {
		
		if (operands.size() < 2) {
			throw new FormulaException("Wrong number of parameters " + formula 
					+ ". Expected at least 2, found " + operands.size());
		}
		
		boolean result = true;
		for (String op: operands) {
			result = result && BooleanValue.isTrue(op);
		}
		
		String stringComp = result ? BooleanValue.getTrueValue() : BooleanValue.getFalseValue();
		
		return stringComp;
	}
	
	/**
	 * Solve or functions
	 * @return
	 * @throws FormulaException
	 */
	private String solveOr() throws FormulaException {
		
		if (operands.size() < 2) {
			throw new FormulaException("Wrong number of parameters " + formula 
					+ ". Expected at least 2, found " + operands.size());
		}
		
		boolean result = false;
		for (String op: operands) {
			result = result || BooleanValue.isTrue(op);
		}
		
		String stringComp = result ? BooleanValue.getTrueValue() : BooleanValue.getFalseValue();
		
		return stringComp;
	}
	
	/**
	 * shahaal 
	 * return UUID (Universally Unique Identifier)
	 * @return
	 * @throws FormulaException
	 */
	private String solveNext() throws FormulaException {
		return UUID.randomUUID().toString();
	}
	
	@Override
	public String toString() {
		try {
			return this.getUnsolvedFormula() + " => " + this.solve() + " with operands " + operands;
		} catch (FormulaException e) {
			e.printStackTrace();
		}
		
		return "ERROR";
	}
	
	public static void main(String[] args) throws FormulaException {
		FunctionFormula f = new FunctionFormula("AND(Yes,Yes,No)");
		System.out.println(f.solve());
		f = new FunctionFormula("AND(no,no)|AND(no,yes)");
		System.out.println(f.solve());
	}
}
