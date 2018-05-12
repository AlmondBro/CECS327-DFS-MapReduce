import java.io.*;
import java.nio.*;

//only to transfer a file from local to remote
// use when you do the append
public class FileStream extends InputStream implements Serializable {

    private String path;
    private File file;
    private int currentPosition;
    private byte[] byteBuffer;
    private int size; //make public or make a getter to use to get the size of the file in 
    
    /**
     * Constructor for the FileStream class
     * @param pathName
     * @throws FileNotFoundException
     * @throws IOException
     */
    public FileStream(String pathName) throws FileNotFoundException, IOException {
      this.path = pathName;
      file = new File(pathName);
      this.size = (int)file.length();
      this.byteBuffer = new byte[size];
      
      FileInputStream fileInputStream = new FileInputStream(pathName);
      int i = 0;
      while (fileInputStream.available()> 0)
      {
	      byteBuffer[i++] = (byte)fileInputStream.read();
      } //have byte array pass into byte buffer
      
      fileInputStream.close();	
      currentPosition = 0;	  
    }
    /**
     * Alternate constructor
     * @param byteArray
     */
    public FileStream(Byte[] byteArray ) {

    }
    /**
     * Alternate constructor
     * @throws FileNotFoundException
     */
    public FileStream() throws FileNotFoundException    {
      currentPosition = 0;	  
    }
    /**
     * Reads a file from the filestream
     */
    public int read() throws IOException {
 	    if (currentPosition < size) {
        return (int)byteBuffer[currentPosition++];
      }
      return 0;
    }
 	/**
 	 * Returns the availble space remaining
 	 */
    public int available() throws IOException
    {
	    return size - currentPosition;
    }
    /**
     * Getter method for the size
     * @return
     */
    public int getSize() {
      return this.size;
    }
    /**
     * Getter method for the path
     * @return
     */
    public String getPath() {
      return this.path;
    }
    /**
     * Setter method for the path
     * @param newPath
     */
    public void setPath(String newPath) {
      this.path = newPath;
    }
    /**
     * Getter method for the file
     * @return
     */
    public File getFile() {
      return this.file;
    }

    /**
     * Setter method for the file
     */
    public void setFile(File newFile) {
      this.file = newFile;
    }
}