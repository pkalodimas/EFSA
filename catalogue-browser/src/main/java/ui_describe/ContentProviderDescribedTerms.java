package ui_describe;
import java.util.ArrayList;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import already_described_terms.DescribedTerm;

public class ContentProviderDescribedTerms implements IStructuredContentProvider {

	public void dispose ( ) {
	}

	public void inputChanged ( Viewer arg0 , Object arg1 , Object arg2 ) {}

	@SuppressWarnings("unchecked")
	public Object[] getElements ( Object fullCodes ) {
		
		ArrayList< DescribedTerm > l = (ArrayList< DescribedTerm >) fullCodes;

		return l.toArray();
	}
}
