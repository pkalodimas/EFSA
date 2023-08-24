package catalogue;

import java.util.ArrayList;

public class DcfCataloguesList extends ArrayList<IDcfCatalogue> implements IDcfCataloguesList<IDcfCatalogue> {
	private static final long serialVersionUID = 944117596551368064L;

	@Override
	public IDcfCatalogue create() {
		return new DcfCatalogue();
	}
}
