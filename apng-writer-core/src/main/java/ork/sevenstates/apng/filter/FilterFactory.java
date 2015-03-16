package ork.sevenstates.apng.filter;


public class FilterFactory {
	/**
	 * 
	 * @see None#INDEX
	 * @see Sub#INDEX
	 * @see Up#INDEX
	 * @see Average#INDEX
	 * @see Paeth#INDEX
	 * 
	 * @param type
	 *            <ul>
	 *            <li>0 - None (as is)
	 *            <li>1 - Sub
	 *            <li>2 - Up
	 *            <li>3 - Average
	 *            <li>4 - Paeth
	 *            <li>-1 - LocalMinimum:Diff
	 *            <li>-2 - LocalMinimum:Deflatter
	 *            </ul>
	 * 
	 * @return filter instance based on the code.
	 */
	public static Filter getFilter(int type) {
		switch (type) {
		case None.INDEX:
			return new None();
		case Sub.INDEX:
			return new Sub();
		case Up.INDEX:
			return new Up();
		case Average.INDEX:
			return new Average();
		case Paeth.INDEX:
			return new Paeth();
		case DiffLocalMinimum.INDEX:
			return new DiffLocalMinimum();
		case DeflaterLocalMinimum.INDEX:
			return new DeflaterLocalMinimum();
		default:
			throw new IllegalArgumentException(type + " is unknown filter type");
		}
	}
}
