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

public class Client {
    //Private class properties/instance variables
    private int port;
    private DFS distributedFileSystem;
    private UserInterface userInterface;

    public Client(int port) throws Exception {
        distributedFileSystem = new DFS(port);
        userInterface = new UserInterface(distributedFileSystem);
    } //end Client() constructor

    public void welcomeMessage() {
        userInterface.welcomeMessage();
    }

    public void getUserInterface() throws Exception {
        userInterface.makingSelection();
    }

    public static void main(String args[]) throws Exception {
        if (args.length < 1 ) {
            throw new IllegalArgumentException("Please supply a port parameter: <port>");
        }
        /* 
        To compile and run the program, enter this in your command line (Linux):
            javac -cp gson-2.8.2.jar Client.java Chord.java ChordMessageInterface.java DFS.java Metadata.java MetaFile.java Page.java UserInterface.java FileStream.java Mapper.java MapReduceInterface.java; java -classpath ".:gson-2.8.2.jar" Client 3000
        */
        System.out.println(args[0]);
        Client client = new Client( Integer.parseInt(args[0]));
        client.welcomeMessage();
        client.getUserInterface();
        System.exit(0); 
     } 
}
