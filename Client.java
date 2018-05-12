/**
 * CECS327-01/02
 * MapReduce Project
 * Friday, May 11 2018
 * 
 * Eric Aguirre (009824605)
 * Austin Cheng ()
 * Juan David Lopez (013101593)
 * 
 * Client.java file
 */
import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.nio.file.*;
import com.google.gson.*;
import com.google.gson.Gson;
import java.util.Random;

public class Client {
    //Private class properties/instance variables
    private int port;
    private DFS distributedFileSystem;
    private UserInterface userInterface;

    /**
     * Constructor for the client class
     * @param port the port number of the Distributed File System you wish to join.
     * @throws Exception
     */
    public Client(int port) throws Exception {
        this.port = port;
        this.distributedFileSystem = new DFS(port);
        this.userInterface = new UserInterface(distributedFileSystem);
    } //end Client() constructor

    /**
     * Display a welcome message.
     */
    public void welcomeMessage() {
        this.userInterface.welcomeMessage();
    }

    /**
     * Get the user interface with all the options.
     */
    public void getUserInterface() throws Exception {
        this.userInterface.makingSelection();
    }

    /**
     * Main method to execute the MapReduce program
     * @param args command line arguments
     * @throws Exception
     */
    public static void main(String args[]) throws Exception, IllegalArgumentException, ArrayIndexOutOfBoundsException  {
        Random random = new Random();
        int randomPort = random.nextInt(5000) + 1026;

        /* 
        To compile and run the program, enter this in your command line (Linux). You may supply a custom port number at the end if you wish:
            javac -cp gson-2.8.2.jar Client.java Chord.java ChordMessageInterface.java DFS.java Metadata.java MetaFile.java Page.java UserInterface.java FileStream.java Mapper.java MapReduceInterface.java; java -classpath ".:gson-2.8.2.jar" Client
        */
        try {
            if (args.length >= 1 ) { 
                Client client = new Client(Integer.parseInt(args[0]));
                
                client.welcomeMessage();
                client.getUserInterface();

                System.exit(0);
            } //end if-statement

            else {
                Client client = new Client(randomPort);
                
                client.welcomeMessage();
                client.getUserInterface();

                System.exit(0);
            } //end else-statement

        } catch(IllegalArgumentException e) {
            System.out.println("\nPlease supply a port parameter: <port> upon next compilation and run. Defaulting to port #:\t" + randomPort);
            Client client = new Client(randomPort);
            
            client.welcomeMessage();
            client.getUserInterface();

            System.exit(0);

        } catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("\nPlease supply a port parameter: <port> upon next compilation and run. Defaulting to port #:\t" + randomPort);
            Client client = new Client(randomPort);
            
            client.welcomeMessage();
            client.getUserInterface();

            System.exit(0);
        } //end catch() block
     }  //end main() method
} //end Client class
