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
    
    
    public Boolean isKeyInSemiCloseInterval(long key, long key1, long key2)
    {
       if (key1 < key2)
           return (key > key1 && key <= key2);
      else
          return (key > key1 || key <= key2);
    }
    //chord
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

    public Boolean isKeyInOpenInterval(long key, long key1, long key2)
    {
      if (key1 < key2)
          return (key > key1 && key < key2);
      else
          return (key > key1 || key < key2);
    }
    
    
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
    
    public void delete(long guidObject) throws RemoteException {
        File file = new File("./"+guid+"/repository/" + guidObject);
        file.delete();
    }
    
    public long getId() throws RemoteException {
        return guid;
    }
    public boolean isAlive() throws RemoteException {
	    return true;
    }
    
    public ChordMessageInterface getPredecessor() throws RemoteException {
	    return predecessor;
    }
    
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
       
    public Chord(int port, long guid) throws RemoteException {
        int j;
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
    //MapReduce	
    @Override
    public void setWorkingPeer(Long page) throws RemoteException{

            set.add(page);

    }
  public void completePeer(Long page, Long n) throws RemoteException{
            this.numberOfRecords += n;
            set.remove(page);
    }
    public Boolean isPhaseCompleted() throws IOException{
    
            return set.isEmpty();
    }
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

	 		public void run();
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
 
 	public void createReduceFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException
 	{
 		PrintWriter writer = new PrintWriter(fileName + "_reduce", "UTF-8");
	 	
	 	for(Long key : BReduce.keySet())
	 	{
	 		writer.println(key + "; " + BReduce.get(key));
	 	}
	 		
	 	writer.close();
 	}
 
 
 
    public void mapContext(Long page, MapReduceInterface mapper,
    ChordMessageInterface context) throws RemoteException, IOException, Exception 
    {

         FileStream in = this.get(page);
         File File = in.getFile();

         Scanner parse = new Scanner(File);
         System.out.println("Parseing you file");
         String line;
         String key;
         String value;
         int count = 0;
         long key2;
        context.setWorkingPeer(page);
         //reads each line of the file
         class MyThread extends Thread {

            public MyThread() 
            {
                  
            }
        public void run();
        {
         //start 
         try {
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
                    
                    mapper.map(Long.toLong(key), value, context);
                    break;
                //    System.out.print("The key is : " + key2 + "  and the value is: ");
                //    System.out.println(value);
                 
                 }
            } // ends for loop
        } //ends while loop
              //read each line, break it at a colon. Just testing if I can do that with a single file
        //give to emitMap
         //   System.out.println("Counted " + count + " this many ;");
         context.completePeer(page, count);
        } 
        catch (Exception e) 
        {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
    }

         //
        MyThread thread = new MyThread();
         thread.run();
    }
        } // ends method

    

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
