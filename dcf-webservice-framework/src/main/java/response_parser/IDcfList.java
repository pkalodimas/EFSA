package response_parser;

import java.util.Collection;

public interface IDcfList<T> extends Collection<T> {

	/**
	 * Add a new element to the list
	 * @param dataset
	 */
	@Override
	public boolean add(T elem);
	
	/**
	 * Create a new instance of an element
	 */
	public T create();
}
