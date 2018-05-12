import java.util.ArrayList;
public class MetaFile {
	private String name;
	private int numberOfPages;
	private int pageSize;
	private int size;
	private ArrayList<Page> page;

	/**
	 * Constructor for the Metafile class
	 * @param name
	 * @param numberOfPages
	 * @param pageSize
	 * @param size
	 * @param pages
	 */
	public MetaFile(String name, int numberOfPages, int pageSize, int size, ArrayList<Page> pages)
	{
		this.name = name;
		this.numberOfPages = numberOfPages;
		this.pageSize = pageSize;
		this.size = size;
		this.page = pages;

	}
	/**
	 * Alternate constructor for the metafile class
	 */
	public MetaFile()
	{
		this.page = new ArrayList<Page>();
	}
	/**
	 * Getter method for the name
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}
	/**
	 * Setter method for the name
	 * @param newName
	 */
	public void setName(String newName) {
		this.name = newName;
	}
	/**
	 * Getter method for the page
	 */
	public Page getPage(int pageNum) throws Exception
	{
		if(pageNum > page.size() + 1)
			throw new Exception("Page number does not exist!");
		else
			return page.get(pageNum);
	}
	/**
	 * Returns the last page in the metafile
	 * @return
	 */
	public Page getLastPage()
	{
		int size  = page.size() -1;
		System.out.println("" + size);
		return page.get(size);
	}
	/**
	 * Appends a page to a file
	 * @param pageObject
	 */
	public void addPage(Page pageObject)
	{
		
		  page.add(pageObject);
		  System.out.println("Number of pages: "+ page);
		if(page.size() > 1)
		{
		   int number = page.size();
		   pageObject.setPage(number);
		} else
		{
			pageObject.setPage(1);
		}
	}
	/**
	 * Returns the first page in the file
	 * @return
	 */
	public Page getFirstPage()
	{
		return page.get(0);
	}
	/**
	 * Returns the index of a page
	 * @return
	 */
	public int getNumOfPage()
	{
		int numOfPage = page.size();
		return numOfPage;
	}
}
