import java.rmi.*;
import java.util.List;
import java.io.*;

public interface ChordMessageInterface extends Remote
{
    //sets of methods to override DFS
    public ChordMessageInterface getPredecessor()  throws RemoteException;
    ChordMessageInterface locateSuccessor(long key) throws RemoteException;
    ChordMessageInterface closestPrecedingNode(long key) throws RemoteException;
    public void joinRing(String Ip, int port)  throws RemoteException;
    public void notify(ChordMessageInterface j) throws RemoteException;
    public boolean isAlive() throws RemoteException;
    public long getId() throws RemoteException;
    
    
    public void put(long guidObject, FileStream inputStream) throws IOException, RemoteException;
    public FileStream get(long guidObject) throws IOException, RemoteException;
    public void delete(long guidObject) throws IOException, RemoteException;

    //sets of methods to override Map-Reduce
   public void setWorkingPeer() throws IOException;
   public void completePeer(Long page, Long n) throws RemoteException;
   public Boolean isPhaseCompleted() throws IOException;
   public void reduceContext(Long source, MapReduceInterface reducer,
    ChordMessageInterface context) throws RemoteException;
    public void mapContext(Long page, MapReduceInterface mapper,ChordMessageInterface context) throws RemoteException, IOException,Exception ;
    public void emitMap(Long key, String value) throws RemoteException;
 /*   public void emitReduce(Long page, List<String> value) throws RemoteException;
*/
}
