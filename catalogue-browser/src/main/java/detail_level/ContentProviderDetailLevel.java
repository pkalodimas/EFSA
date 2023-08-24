package detail_level;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ContentProviderDetailLevel implements IStructuredContentProvider {

	public void dispose ( ) {
	}

	public void inputChanged ( Viewer arg0 , Object arg1 , Object arg2 ) {}

	@SuppressWarnings("unchecked")
	public Object[] getElements ( Object list ) {
		ArrayList< DetailLevelGraphics > l = (ArrayList< DetailLevelGraphics >) list;
		return l.toArray();
	}
}
