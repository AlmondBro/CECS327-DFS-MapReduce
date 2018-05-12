public class Page {
	private int number;
	private long guid;
	private long size;
	
	/**
	 * Constructor for the page class
	 * @param number
	 * @param guid
	 * @param size
	 */
	public Page(int number, long guid, long size)
	{
		this.number = number;
		this.guid = guid;
		this.size = size;
	}
	/**
	 * Retuns the index of this page
	 * @return
	 */
	public int getNumberofPage()
	{
		return this.number;
	}
	/**
	 * Returns the last page in the file
	 * @return
	 */
	public int getLastPage()
	{
		return 0;
	}
	/**
	 * Getter method for the guid
	 * @return
	 */
	public long getGUID() {
		return this.guid;
	}
	/**
	 * Getter method for the size
	 * @return
	 */
	public long getSize() {
		return this.getSize();
	}
	/**
	 * Setter method for the page
	 * @param number
	 */
	public void setPage(int number) {
		this.number = number;
	}
}
