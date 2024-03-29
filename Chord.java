import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.*;

public class Chord extends java.rmi.server.UnicastRemoteObject implements ChordMessageInterface {
    public static final int M = 2;
    private DFS distributedFileSystem;

    //DFS
    Registry registry;    // rmi registry for lookup the remote objects.
    ChordMessageInterface successor;
    ChordMessageInterface predecessor;
    ChordMessageInterface[] finger;
    int nextFinger;
    long guid;   		// GUID (i)

    //Map-Reduce
    private Long numberOfRecords;
    private Set<Long> set;
    private Map <Long,List<String>> BMap;
    private Map <Long,String> BReduce;
    
    /**
     * Returns true if the key is determined to be
     * in the interval, false otherwise.
     * @param key
     * @param key1
     * @param key2
     * @return
     */
    public Boolean isKeyInSemiCloseInterval(long key, long key1, long key2)
    {
       if (key1 < key2)
           return (key > key1 && key <= key2);
      else
          return (key > key1 || key <= key2);
    }
    /**
     * Hashes a string object.
     * @param objectName
     * @return
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
    /**
     * Returns true if the key is determined to be
     * in the open interval.
     * @param key
     * @param key1
     * @param key2
     * @return
     */
    public Boolean isKeyInOpenInterval(long key, long key1, long key2)
    {
      if (key1 < key2)
          return (key > key1 && key < key2);
      else
          return (key > key1 || key < key2);
    }
    
    /**
     * Posts a file to the file system
     */
    public void put(long guidObject, FileStream stream) throws RemoteException {
      try {
          String fileName = guid+"/repository/" + guidObject;
       //   System.out.println("Path: " + fileName); debugging print
          FileOutputStream output = new FileOutputStream(fileName);
          while (stream.available() > 0)
              output.write(stream.read());
          output.close();
      }
      catch (IOException e) {
          System.out.println("File does not exist (chord.put())");
      }
    }
    
    /**
     * Returns a file posted in the file system.
     */
    public FileStream get(long guidObject) throws RemoteException{
        FileStream file = null;
        try {
            //May have to use a different. You pass a fileStream into Chord.put();
             file = new FileStream(guid+"/repository/" + guidObject);
             //file = new FileStream("./"+guid+"/repository");
        } catch (IOException e)
        {
           
            throw(new RemoteException("File does not exists (chord.get())"));
       
        }
     
        return file;
    }
    /**
     * Deletes a file posted in the file system
     */
    public void delete(long guidObject) throws RemoteException {
        File file = new File("./"+guid+"/repository/" + guidObject);
        file.delete();
    }
    /**
     * Getter method for guid
     */
    public long getId() throws RemoteException {
        return guid;
    }
    /**
     * Returns true if the process is still executing
     */
    public boolean isAlive() throws RemoteException {
	    return true;
    }
    /**
     * Getter method for ChordmessageInterface
     */
    public ChordMessageInterface getPredecessor() throws RemoteException {
	    return predecessor;
    }
    /**
     * Returns the peer closest to the client
     */
    public ChordMessageInterface locateSuccessor(long key) throws RemoteException {
	    if (key == guid)
            throw new IllegalArgumentException("Key must be distinct that  " + guid);
	    if (successor.getId() != guid)
	    {
	      if (isKeyInSemiCloseInterval(key, guid, successor.getId()))
	        return successor;
	      ChordMessageInterface j = closestPrecedingNode(key);
	      
          if (j == null)
	        return null;
	      return j.locateSuccessor(key);
        }
        return successor;
    }
    /**
     * Returns the node closest to the client
     */
    public ChordMessageInterface closestPrecedingNode(long key) throws RemoteException {
        // todo
        if(key != guid) {
            int i = M - 1;
            while (i >= 0) {
                try{
       
                    if(isKeyInSemiCloseInterval(finger[i].getId(), guid, key)) {
                        if(finger[i].getId() != key)
                            return finger[i];
                        else {
                            return successor;
                        }
                    }
                }
                catch(Exception e)
                {
                    // Skip ;
                }
                i--;
            }
        }
        return successor;
    }
    /**
     * Creates a connection to the ring in the 
     * distributed file system
     */
    public void joinRing(String ip, int port)  throws RemoteException {
        try {
            System.out.println("Get Registry to joining ring");
            Registry registry = LocateRegistry.getRegistry(ip, port);
            ChordMessageInterface chord = (ChordMessageInterface)(registry.lookup("Chord"));
            predecessor = null;
            successor = chord.locateSuccessor(this.getId());
            System.out.println("Joining ring\n\n");
        }
        catch(RemoteException | NotBoundException e){
            System.out.println("Client did not join");
            successor = this;
        }   
    }
    /**
     * Returns the successor that is next in the ring
     */
    public void findingNextSuccessor()
    {
        int i;
        successor = this;
        for (i = 0;  i< M; i++)
        {
            try {
                if (finger[i].isAlive()) {
                    successor = finger[i];
                }
            } catch(RemoteException | NullPointerException e) {
                finger[i] = null;
            }
        }
    }
    /**
     * Stabilizes the connection
     */
    public void stabilize() {
      try {
          if (successor != null)
          {
              ChordMessageInterface x = successor.getPredecessor();
	   
              if (x != null && x.getId() != this.getId() && isKeyInOpenInterval(x.getId(), this.getId(), successor.getId()))
              {
                  successor = x;
              }
              if (successor.getId() != getId())
              {
                  successor.notify(this);
              }
          }
      } catch(RemoteException | NullPointerException e1) {
          findingNextSuccessor();

      }
    }
    /**
     * Sends a message to all of the other peers
     * in the network
     */
    public void notify(ChordMessageInterface j) throws RemoteException {
         if (predecessor == null || (predecessor != null
                    && isKeyInOpenInterval(j.getId(), predecessor.getId(), guid)))
             predecessor = j;
            try {
                File folder = new File("./"+guid+"/repository/");
                File[] files = folder.listFiles();
                for (File file : files) {
                    long guidObject = Long.valueOf(file.getName());
                    if(guidObject < predecessor.getId() && predecessor.getId() < guid) {
                        predecessor.put(guidObject, new FileStream(file.getPath()));
                        file.delete();
                    }
                }
                } catch (ArrayIndexOutOfBoundsException e) {
                //happens sometimes when a new file is added during foreach loop
            } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * Stabilizes the fingers
     */
    public void fixFingers() {
        long id = guid;
        try {
            long nextId = this.getId() + 1<< (nextFinger+1);
            finger[nextFinger] = locateSuccessor(nextId);
	    
            if (finger[nextFinger].getId() == guid)
                finger[nextFinger] = null;
            else
                nextFinger = (nextFinger + 1) % M;
        }
        catch(RemoteException | NullPointerException e){
            e.printStackTrace();
        }
    }
    /**
     * Checks if there is a predecessor preceding
     */
    public void checkPredecessor() { 	
      try {
          if (predecessor != null && !predecessor.isAlive())
              predecessor = null;
      } 
      catch(RemoteException e)  {
          predecessor = null;
          //e.printStackTrace();
      }
    }
    /**
     * Getter method for BReduce
     */
    public Map <Long, String> getBReduce()
    {
        System.out.println("Retrieving the reduced map");
        return  BReduce;
    }
    /**
     * Clears the reduce method
     */
    public void emptyReduce()
    {
        BReduce.clear();
    }
    /**
     * Constructor for the Chord class
     * @param port
     * @param guid
     * @throws RemoteException
     */
    public Chord(int port, long guid) throws RemoteException {
        int j;
        set = new TreeSet<Long>();
        BMap = new TreeMap<Long, List<String>>();
        BReduce = new TreeMap<Long, String>();
        finger = new ChordMessageInterface[M];
        for (j=0;j<M; j++) {
	       finger[j] = null;
     	}
        this.guid = guid;
	
        predecessor = null;
        successor = this;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
	    @Override
	    public void run() {
            stabilize();
            fixFingers();
            checkPredecessor();
            }
        }, 500, 500);
        try{
            // create the registry and bind the name and object.
            System.out.println(guid + " is starting RMI at port="+port);
            registry = LocateRegistry.createRegistry( port );
            registry.rebind("Chord", this);
        }
        catch(RemoteException e){
	       throw e;
        } 
    }
    /**
     * Prints out various messages to the console
     */
    void Print() {   
        int i;
        try {
            if (successor != null)
                System.out.println("successor "+ successor.getId());
            if (predecessor != null)
                System.out.println("predecessor "+ predecessor.getId());
            for (i=0; i<M; i++)
            {
                try {
                    if (finger != null)
                        System.out.println("Finger "+ i + " " + finger[i].getId());
                } catch(NullPointerException e)
                {
                    finger[i] = null;
                }
            }
        }
        catch(RemoteException e){
	       System.out.println("Cannot retrive id");
        }
    }
    /**
     * Setter method for the set
     */
    @Override
    public void setWorkingPeer(Long page) throws RemoteException{

            set.add(page);

    }
    /**
     * Completes the peer connection
     */
  public void completePeer(Long page, Long n) throws RemoteException{
            this.numberOfRecords += n;
            set.remove(page);
    }
  /**
   * Returns true if the phase is completed
   */
    public Boolean isPhaseCompleted() throws IOException{
    
            return set.isEmpty();
    }
    /**
     * Part of the reduce phase of MapReduce,
     * reduces the context
     */
 public void reduceContext(Long source, MapReduceInterface reducer, ChordMessageInterface context) throws RemoteException
    {
        //TODO
        //creates and stores local  page
        //add page, make a refereence file distributed sytem
        /*
         * If source =/= guid, call
			successor.reduceContext(source, reducer, context). Then, create a new
			thread to avoid blocking in which you have to read in order BMap, and
			execute reducer.reduce(key, value, context).
			Note: It must exist a metafile called ”fileName_reduce” where fileName
			is the original logical file that you are sorting with n pages. Each
			peer creates a page (guid) with the data in BReduce and insert into
			”fileName_reduce”.
         */
	 	if (guid != source)
	 	{
	 		successor.reduceContext(source, reducer, context);
	 	}
	 	
	 	class MyThread extends Thread {

	 		public MyThread() 
	 		{
	 			  
	 		}

	 		public void run()
	 		{
	 			
	 			for(Long key : BMap.keySet())
	 			{
		 			try 
		 			{
                        reducer.reduce(key, BMap.get(key), context);

					} 
		 			catch (IOException e) 
		 			{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 			}
	 		}
	 	}
	 	
	 	MyThread thread = new MyThread();
	 	thread.run();

     }
 
 	/*public void createReduceFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException
 	{
 		PrintWriter writer = new PrintWriter(fileName + "_reduce", "UTF-8");
	 	
	 	for(Long key : BReduce.keySet())
	 	{
	 		writer.println(key + "; " + BReduce.get(key));
	 	}
	 		
	 	writer.close();
 	}*/
 
 
 	/**
 	 * Executes the map phase of MapReduce
 	 */
    public void mapContext(Long page, MapReduceInterface mapper,
    ChordMessageInterface context) throws RemoteException, IOException, Exception 
    {

         FileStream in = this.get(page);
         File File = in.getFile();

         Scanner parse = new Scanner(File);
         System.out.println("Parseing you file");
         
         
         
         long key2;
         context.setWorkingPeer(page);
         //reads each line of the file
         class Thread2 extends Thread 
         {
        	 public Thread2() 
        	 {
                  
        	 }
        	 
        	 public void run()
        	 {
        		 int count = 0;
        		 String line;
        		 String key;
                 String value;
		         //start 
		         try 
		         {
			         while(parse.hasNextLine())
			         {
			            // gets one line of file
			        	 
			            line = parse.nextLine();
			            
			            for(int i = 0; i < line.length(); i++)
			            {
			                //parse the one line
			                if(line.charAt(i) == ';')
			                {
			                    count = count + 1;
			                    value = line.substring(i +1);
			                    key = line.substring(0,i);
			                    System.out.println(key);
			                    
			                    mapper.map(Long.parseLong(key), value, context);
			                    break;
			                //    System.out.print("The key is : " + key2 + "  and the value is: ");
			                //    System.out.println(value);
			                 
			                 }
			            } // ends for loop
			        } //ends while loop
			         //read each line, break it at a colon. Just testing if I can do that with a single file
			         //give to emitMap
			         //   System.out.println("Counted " + count + " this many ;");
			         context.completePeer(page, Long.parseLong(Integer.toString(count)));
		         } 
		         catch (Exception e) 
		         {
		        	 // TODO Auto-generated catch block
		        	 e.printStackTrace();
		         }
        	 }
         }
         //
         Thread2 myThread = new Thread2();
         myThread.run();
     } // ends method

    
    /**
     * Updates the BMap with the results of the 
     * mapping phase
     */
public void emitMap(Long key, String value) throws RemoteException
    { 
        if (isKeyInOpenInterval(key, predecessor.getId(), successor.getId()))
        {
        // insert in the BMap. Allows repetition
            if (!BMap.containsKey(key))
            {
            List<String> list = new ArrayList<String>();
            BMap.put(key,list);
            }
            BMap.get(key).add(value);
        }
        else
            {
                ChordMessageInterface peer = this.locateSuccessor(key);
                peer.emitMap(key, value);
            }
    }
    /**
     * Updates the peers with the results of the
     * reduce phase
     */
    public void emitReduce(Long key, String value) throws RemoteException
    {
        if (isKeyInOpenInterval(key, predecessor.getId(), successor.getId()))
        {
            // insert in the BReduce
        	BReduce.put(key, value);
        }else
            {
             ChordMessageInterface peer = this.locateSuccessor(key);
             peer.emitReduce(key, value);
         }
     }


}
