import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;

import java.nio.file.*;
import java.math.BigInteger;
import java.security.*;
// import a json package
import com.google.gson.Gson;


/* JSON Format

 { //use method put in chord to send to the cloud 
    // use md5 of localfile for GUID for page 
    // delete pages then delete the file as well
    //page is an actually physical file in the cloud
    //use put() from Chord to upload to the network
    "metadata" :
    {
        file :
        {
            name  : "File1"
            numberOfPages : "3"
            pageSize : "1024" //dont worry about this
            size : "2291" 
            page :
            {
                number : "1" //dont worry about number, since JSON keeps track this.
                guid   : "22412"
                size   : "1024"
            }
            page :
            {
                number : "2"
                guid   : "46312"
                size   : "1024"
            }
            page :
            {
                number : "3"
                guid   : "93719"
                size   : "243"
            }
        }
    }
}
 */
public class DFS implements Serializable {
    int port;
    Chord chord;
    
    private long guid;
    
    private Gson gson;
    private String json;
    private FileStream filestream;
    private String listOfFiles;
    private Map <Long,String>  tempReduce ;
    private File metafile_physicalFile;

    /**
     * Constructor for the DFS class. Takes
     * an integer as the port number.
     * @param port
     * @throws Exception
     */
    public DFS(int port) throws Exception
    {
        gson = new Gson();            
        
        this.port = port;
        long guid = md5("" + port);
        setGuid(guid);
//        System.out.println(port + " | " + guid);
        chord = new Chord(port, guid);
        Files.createDirectories(Paths.get(guid+"/repository/"));

    }
    public void setGuid(long Guid)
    {
        this.guid = Guid;
    }
    public long getGUID() {
        return this.guid;
    }

    public String getStringOfFiles() {
        return this.listOfFiles;
    }

    public void setStringOfFiles(String stringToConcatenate) {
        this.listOfFiles.concat(stringToConcatenate);
    }   

    /**
     * Runs the md5 algorithm on an object and
     * returns its guid
     * @param objectName is a String and the only
     * input
     * @return a guid in the form of a long
     */
    private long md5(String objectName) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(objectName.getBytes());
            BigInteger bigInt = new BigInteger(1,m.digest());
            return Math.abs(bigInt.longValue());
        } catch(NoSuchAlgorithmException e) {
                //e.printStackTrace();
                System.out.println("Caught error in md5() method");
        }
        return 0;
    } //end md5() method

    /** Method to join a process to a Chord network
     * @param ip IP address of the machine's network you wish to join
     * @param port of the network
     * @throws Exception 
     */
    public void join(String ip, int port) throws Exception {
        chord.joinRing(ip, port);
        //chord.Print();
    }
    


  public Metadata readMetaData() throws Exception, RemoteException {
    Metadata metadata = null;
      
      try {
       
        long guid = md5("Metadata"); 
        
        ChordMessageInterface peer = chord.locateSuccessor(guid);
         //locate the processor that has the meatadata

    

        FileStream metadataraw = peer.get(guid);  //locates the file  
        File peerFile = metadataraw.getFile(); //gets the file

        
        String fileName =  peer.getId() + "/repository/" + guid;

        System.out.println(fileName); //check the filepath for debugging
    
       
        File newFile = new File(fileName);
        FileOutputStream output = new FileOutputStream(peerFile);
 
        while (metadataraw.available() > 0)  {
            output.write(metadataraw.read());
        }   
        output.close();

 
        FileReader fileReader = new FileReader(new File(fileName));
         metadata =  this.getGsonObject().fromJson(fileReader, Metadata.class); //Reads metadata

    }catch(RemoteException e)
    {
    //   System.out.println("1"); debugging print
       return  metadata = new Metadata();
    
    }catch(FileNotFoundException e) {
      //  System.out.println("2"); debugging print
        return metadata = new Metadata();
    
    }
     
    
      return metadata;
  }

    public void writeMetaData(Metadata metadata) throws Exception {
        try  {
            long guid = md5("Metadata"); //which process has that file
            ChordMessageInterface process = chord.locateSuccessor(guid); //I know which node has the metadata
            
           //Following block is to write to localFile
          
            String tempFile = process.getId() + "/repository/"+guid;
            File tempFile_file  = new File(tempFile);

           //felt we need update the existing local Metadata but writer will override 
           //the existing the Metadata with just he new file 
           if(tempFile_file.exists())
           {
             System.out.println("\nFile: exist");
             FileWriter writer = new FileWriter(tempFile_file);
             gson.toJson(metadata, writer);
             writer.close();
           } else {
             FileWriter writer = new FileWriter(tempFile_file);
             gson.toJson(metadata, writer);
             writer.close();
           }
        
           //chords put doesn't seem to put back into the cloud? how to fix
        
            process.put(guid, new FileStream(tempFile));
            System.out.println("\n\tFile was succesfully written to FileSystem Metadata.");
        }  catch (FileNotFoundException e) {
            System.out.println("File: Does not exist (DFS.writeMedata())\n");
            e.printStackTrace();
        }
    }

    public Gson getGsonObject() {
        return this.gson;
    }
    
     /**
     * Creates a new file with the inputted name
     * into the metadata object.
     * @param fileName
     * @throws Exception
     */
    public void touch(String fileName) throws Exception {
         // TODO: Create the file fileName by adding a new entry to the Metadata
         // Write Metadata
        Metadata metadata = readMetaData(); //always read first when creating
        //this.getGsonObject().toJson(metadata);
        metadata.createFile(fileName);
        this.writeMetaData(metadata);
    }

     /**
     * Returns a list of the files in the
     * metadata in the form of a string.
     * @return
     * @throws Exception
     */
    public String ls() throws Exception {
        Metadata metadata = readMetaData(); //always read first when creating

       // TODO: returns all the files in the Metadata
        String listOfFiles = "";
    	listOfFiles = metadata.getFileNames();
        return listOfFiles;

    }
    
     /**
     * Renames the metadata object.
     * @param oldName
     * @param newName
     * @throws Exception
     */
    public void mv(String oldName, String newName) throws Exception {
        Metadata metadata = readMetaData(); //always read first when creating
        metadata.changeName(oldName, newName);
        writeMetaData(metadata);
     }

    /**
     * Deletes a file and all of its pages
     * from the metadata.
     * @param fileName
     * @throws Exception
     */
    public void delete(String fileName) throws Exception {

            Metadata metadata = readMetaData(); //always read first when creating
           metadata.delete(fileName);
           writeMetaData(metadata);
        // Write Metadata    	
    }
    
      /**
     * Returns a page from a file from the metadata
     * in the form of a FileStream.
     * @param fileName
     * @param pageNumber
     * @return
     * @throws Exception
     */
    public FileStream read(String fileName, int pageNumber) throws Exception {
        // TODO: read pageNumber from fileName
        Metadata metadata = readMetaData(); //always read first when creating
        Page page = metadata.getFile(fileName).getPage(pageNumber-1);
   //     System.out.println("Page number: " +page.getNumberofPage()); debugging prints
   //     System.out.println("Read the file's page " + page.getGUID()); debugging pints
        ChordMessageInterface peer = chord.locateSuccessor(page.getGUID());
        return peer.get(page.getGUID());
    	
    }
    
    
     /**
     * Returns the last page of a file in the metadata
     * in the form of a Filestream.
     * @param fileName
     * @return Filestream
     * @throws Exception
     */
    public FileStream tail(String fileName) throws Exception
    {
        Metadata metadata = readMetaData(); //always read first when creating
        
        Page page = metadata.getFile(fileName).getLastPage();
        System.out.println("Read the file's page " +page.getGUID());
        ChordMessageInterface peer = chord.locateSuccessor(page.getGUID());
        System.out.println("Yes");
        return peer.get(page.getGUID());
    }

     /**
     * Returns the first page of the file in the
     * metadata.
     * @param fileName
     * @return
     * @throws Exception
     */
    public FileStream head(String fileName) throws Exception
    {
        return read(fileName, 1);
    }

     /**
     * Adds a new file to the end of the array of 
     * files in the metadata.
     * @param filename
     * @param filepath
     * @throws Exception
     */
    public void append(String filename, String localFile) throws Exception
    {
        Metadata metadata = readMetaData(); //always read first when creating
      
        File local_file  = new File(localFile);
        if(local_file.exists())
        {
          System.out.println("Reading correctly "+ localFile);
         
         guid = md5(localFile);
         Page page = new Page(0, guid, 0);
         metadata.getFile(filename).addPage(page);
         ChordMessageInterface peer = chord.locateSuccessor(guid);
         peer.put(guid, new FileStream(localFile));
        writeMetaData(metadata);

        } else {
            System.out.println(localFile +" doesn't exist");
        }
    }
    public void runMapReduce(String filename) throws Exception
    {
     
  
        System.out.println("Starting Map Reduce");
        String name = filename;
        MapReduceInterface mapreduce = new Mapper();
        Metadata metadata = readMetaData();
        int size = metadata.getFile(filename).getNumOfPage();
        System.out.println("Number of pages in file:" + size);
        ChordMessageInterface peer = null;
    
        for(int i = 0; i < size; i++)
        {
            Page page = metadata.getFile(filename).getPage(i);
            peer = chord.locateSuccessor(page.getGUID());
       //     for each page in metafile.file
  
           
            /*chord is responsible ditatctig what other processes will do
            */
           peer.mapContext(page.getGUID(), mapreduce, chord);        
        }   
        if(peer.isPhaseCompleted() == true)
        {
            Thread.sleep(1000);  //need to sleep so teh peer set isn't empty when map is called  
        }
        chord.successor.reduceContext(chord.getId(), mapreduce, chord);
 
      

   
     while(chord.isPhaseCompleted()) 
     {
         Thread.sleep(1000);
     }  //wait until context.hasCompleted() = true



        System.out.println("All reduce has finished");
        createPage(chord, this, name);

     
    }

   public void createPage(ChordMessageInterface context, DFS DFS, String name) throws Exception
   {
        String fileName = name + "_Reduce";
        DFS.touch(fileName);
        System.out.println(fileName + " was created");
        tempReduce = context.getBReduce();
        DFS.append2(fileName, tempReduce); //create file, write the cotents of mapReduce to it, add to metadata as normal
        context.emptyReduce();

   }
   public void append2(String filename, Map <Long, String> tempReduce) throws Exception
    {
        Metadata metadata = readMetaData(); //always read first when creating

        // do the steps I listed in the comments
        File fake = new File("fake.txt"); //fake used to just store 
        if(fake == null)
        {
            System.out.println("Creating a file");
            fake.createNewFile();
        }
 
        System.out.println("Fake file created");
        PrintWriter writer = new PrintWriter(fake.getAbsolutePath());                                                              //the contents then GUID will be generated MD5 from it
        for(Long key : tempReduce.keySet())
        {
            writer.println(key + "; " + tempReduce.get(key));
        } 
         guid = md5(fake.getAbsolutePath());
         File localFile = new File(guid + ".txt");
         Page page = new Page(0, guid, 0);
         metadata.getFile(filename).addPage(page);
         ChordMessageInterface peer = chord.locateSuccessor(guid);
         peer.put(guid, new FileStream(fake.getAbsolutePath()));
        writeMetaData(metadata);


    }
}
