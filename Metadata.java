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
	private ArrayList<MetaFile> metafiles; 
	
	public Metadata() {
		metafiles = new ArrayList<MetaFile>();	
	}

	
	public void changeName(String oldName, String newName)
	{
		for(int i = 0; i < metafiles.size(); i++)
		{
			if(metafiles.get(i).getName().equals(oldName)) {
				metafiles.get(i).setName(newName);
			}
				
		}
	}
	public String getFileNames() {
		String fileNames = "\n\n\tNo files listed since there aren't any.";
		//if (metafiles.size() > 0) {
			for (int i = 0; i < metafiles.size(); i++) {
				fileNames = "";
				fileNames = "\n\t" + "#" + (i+1) + ":\t" + fileNames + metafiles.get(i).getName();
			} //end for-loop
		//} //end if-statement
		return fileNames;
	} //end getFileNames() 
	
	public void createFile(String fileName) throws FileNotFoundException, IOException {
		
		MetaFile metafile = new MetaFile(fileName, 0, 0, 0, new ArrayList<Page>());
		metafiles.add(metafile);
	}

	public MetaFile getFile(String fileName) throws Exception {
		for(int i = 0; i < metafiles.size(); i++)
		{
			if(metafiles.get(i).getName().equals(fileName))
				return metafiles.get(i);
		}
		throw new Exception("A file with that name does not exist!");
	}

	public void delete(String fileName)
	{
		for(int i = 0; i < metafiles.size(); i++)
		{
			if(metafiles.get(i).getName().equals(fileName))
				 metafiles.remove(i);
		}
	}

	public void append(String name, String localFile) {
		
	}

	public void toJson() {

	}

	public void readfromJSON() {
	
	}
}
