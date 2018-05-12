/*
public class Metadata {
    private ArrayList<Metafile> metafiles; 
    
    public Metadata() {
        this.metafiles = new ArrayList<Metafile>;
    } 

    public addFile(string fileName) {
    }

    public void append(String fileName, long GUID) {
        
    }
} */
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;;
public class Metadata {

	//private String name;
	private ArrayList<MetaFile> metafiles; 
	
/*	public Metadata(String name, ArrayList<MetaFile> metafiles) {
		this.name = name;
		this.metafiles = metafiles;
	}*/
	/**
	 * Constructor for the Metadata class
	 */
	public Metadata() {
		metafiles = new ArrayList<MetaFile>();	
	}

	/**
	 * Sets the metafile's name to a new one
	 * @param oldName
	 * @param newName
	 */
	public void changeName(String oldName, String newName)
	{
		for(int i = 0; i < metafiles.size(); i++)
		{
			if(metafiles.get(i).getName().equals(oldName)) {
				metafiles.get(i).setName(newName);
			}
				
		}
	}
	/**
	 * Returns a list of all of the file's names
	 * @return
	 */
	public String getFileNames()
	{
		String fileNames = "";
		for(int i = 0; i < metafiles.size(); i++)
		{
			fileNames = fileNames + metafiles.get(i).getName();
		}
		return fileNames;
	}
	/**
	 * Creates a file and adds it to the metafile
	 * @param fileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void createFile(String fileName) throws FileNotFoundException, IOException {
		
		MetaFile metafile = new MetaFile(fileName, 0, 0, 0, new ArrayList<Page>());
		metafiles.add(metafile);
	}
	/**
	 * Returns a specified file
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public MetaFile getFile(String fileName) throws Exception {
		for(int i = 0; i < metafiles.size(); i++)
		{
			if(metafiles.get(i).getName().equals(fileName))
				return metafiles.get(i);
		}
		throw new Exception("A file with that name does not exist!");
	}
	/**
	 * Deletes a file from metadata
	 * @param fileName
	 */
	public void delete(String fileName)
	{
		for(int i = 0; i < metafiles.size(); i++)
		{
			if(metafiles.get(i).getName().equals(fileName))
				 metafiles.remove(i);
		}
	}
	/**
	 * Appends a file to the metadata
	 * @param name
	 * @param localFile
	 */
	public void append(String name, String localFile) {
		
	}
	/**
	 * Converts a string to json
	 */
	public void toJson() {

	}
	/**
	 * converts a json to a string
	 */
	public void readfromJSON() {
	
	}
}
