import java.util.Scanner;
import java.util.InputMismatchException;
import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.Path;
public class UserInterface {

    private Scanner userInput;
    private int userSelectionValue;
    private String IP_Address;
    private int port;
    private DFS distributedFileSystem;
    
    /**
     * Constructor for the UserInterface class.
     * @param distributedFileSystem
     */
    public UserInterface(DFS distributedFileSystem) {
        userInput = new Scanner(System.in);
        this.distributedFileSystem = distributedFileSystem;
    } //end UserInterface() constructor
    /**
     * Getter method for scanner object
     * @return
     */
    public Scanner getScanner() {
        return this.userInput;
    }
    /**
     * Getter method for userSelectionValue
     * @return
     */
    public int getUserSelectionValue() {
        return this.userSelectionValue;
    }
    /**
     * Getter method for ip_address
     * @return
     */
    public String getIPAddress() {
        return this.IP_Address;
    }
    /**
     * Getter method for port
     * @return
     */
    public int getPort() {
        return this.port;
    }
    /**
     * Getter method for DFS
     * @return this.distributedFileSystem
     */
    public DFS getDFS() {
        return this.distributedFileSystem;
    }
    /**
     * Setter method for ip address
     * @param newIPAddress
     */
    public void setIPAddress(String newIPAddress) {
       this.IP_Address = newIPAddress; 
    }
    /**
     * Setter method for the port
     * @param newPort
     */
    public void setPort(int newPort) {
        this.port = newPort;
    }
    /**
     * Setter method for the userSelectionValue
     * @param newUserSelectionValue
     */
    public void setUserSelectionValue(int newUserSelectionValue) {
        this.userSelectionValue = newUserSelectionValue;
    }
    /**
     * Prints out the welcome message at the start of
     * the program
     */
    public void welcomeMessage() {
          //Use InputStream classes instead 
          System.out.println("\nWelcome");
          //System.out.println("To join the distributed file system.");
    }
    /**
     * Connects the client to the distributed file system
     * @throws InputMismatchException
     * @throws Exception
     */
    public void connectToDFS() throws InputMismatchException, Exception {
        Scanner user_input = new Scanner(System.in);

        if (this.getUserSelectionValue() == 0) {
            System.out.println("\nPlease enter an IP Address:\t");
            String IP_address = user_input.nextLine();
            this.setIPAddress(IP_address);

            System.out.println("Please enter a port number");
            int port = user_input.nextInt();
            this.setPort(port);

            System.out.println("Connecting you to the distributed file system.");
            this.getDFS().join(this.getIPAddress(), this.getPort());            
        } //end if-statement
    } //end 

    /**
     * Prints out the user interface of options to the console
     */
    public void getCommandLineInterface() {
        System.out.println("\n-----------------------------");
        System.out.println("\nPlease make a selection.");
        System.out.println("\n#0:\tTo join the DFS, enter 0:\t");
        System.out.println("#1:\t To ls (list the files), enter 1:\t");
        System.out.println("#2:\t To touch (create a file), enter 2:\t");
        System.out.println("#3:\t To delete a file, enter 3:\t");
        System.out.println("#4:\t To read (a page from a file), enter 4:\t");
        System.out.println("#5:\t To tail (read last page of a file), enter 5:\t");
        System.out.println("#6:\t To append (add a page to a file), enter 6:\t");
        System.out.println("#7:\t To move (rename a file), enter 7:\t");
        System.out.println("#8:\t To head (read first page of a file), enter 8:\t");
        System.out.println("#9:\t Run mapReduce on a file with page(s) 9:");
        System.out.println("#10:\t To quit, enter 10:\t");
        System.out.println("\n-----------------------------");
        System.out.print("Selection # (press enter after entering number):\t");
    } //end getCommandLineInterface()

    /**
     * Reads the user input and executes various actions
     * @throws Exception
     */
    public void makingSelection() throws Exception, NumberFormatException, InputMismatchException, NullPointerException  {
        Scanner user_input = new Scanner(System.in);
        boolean flag = true;
        String filePath;
        String fileName;

        while(flag = true) {
            try {
                this.getCommandLineInterface();
                String choice = user_input.nextLine();
                int userChoice = Integer.parseInt(choice); 
                setUserSelectionValue(userChoice);
    
                switch(this.getUserSelectionValue()) {
                    case 0:
                        this.connectToDFS();
                    case 1:
                        String fileList = getDFS().ls();
                        System.out.println("The list of files are:\t"+ fileList);
                        //user_input.close();
                        break;
                        
                    case 2:
                        System.out.println("Please enter the file name:\t");
                        fileName = user_input.nextLine();
                        this.getDFS().touch(fileName);
                        //user_input.close();
                        break;
    
                    case 3:
                        System.out.println("Please enter the file name");
                        fileName = user_input.nextLine();
                        
                        System.out.println("You entered the file name:" + fileName);
                        this.getDFS().delete(fileName);
                        //user_input.close();
                        break;
    
                    case 4: 
                        System.out.println("Please enter the file name");
                        fileName = user_input.nextLine();
    
                        System.out.println("You entered the file name:" + fileName);
                        System.out.println("Please enter the page number");
                        
                        int pageNum = user_input.nextInt();
                        System.out.println("You enter the page number: "+ pageNum);
                        FileStream in = this.getDFS().read(fileName, pageNum);
                        File File = in.getFile();   
                        File.getAbsolutePath(); 
                        System.out.println("Files path: " + File.getAbsolutePath());
                        String text = "" +      File.getAbsolutePath(); 
                        Path path = Paths.get(text);       
                        byte[] bytes = Files.readAllBytes(path);
                        String  text2 = new String(bytes, StandardCharsets.UTF_8);
                        System.out.println("Files contents: " + text2);
                          
                          in.close();
                       
                          user_input = new Scanner(System.in);
                          break;
    
                    case 5:
                        System.out.println("Please enter the file name");
                        fileName = user_input.nextLine();
                        System.out.println("You enter the file name:" + fileName);
                        FileStream in2 = this.getDFS().tail(fileName);
                        File File2 = in2.getFile();   
                        File2.getAbsolutePath(); 
                        System.out.println("Files path: " + File2.getAbsolutePath());
                        String text3 = "" +      File2.getAbsolutePath(); 
                        Path path2 = Paths.get(text3);       
                        byte[] bytes2 = Files.readAllBytes(path2);
                        String  text4 = new String(bytes2, StandardCharsets.UTF_8);
                        System.out.println("Files contents: " + text4);
                        in2.close();
                     
                        break;
    
                    case 6:
                        System.out.println("Please enter the file name you wish to append a page to:\t");
                        fileName =  user_input.nextLine();
                        System.out.println("\nEnter the name of your metadata file:" + fileName);
                        System.out.println("Enter the relative file path of the physical file you wish to append as a page to your selected metadata file:\t");
                        filePath = user_input.nextLine();
                        System.out.println("You entered the local file path:"+ filePath);
                        this.getDFS().append(fileName, filePath );
                        break;
              
                    case 7:
                        System.out.println("Please enter the file name");
                        fileName =  user_input.nextLine();
                        System.out.println("You enter the file name:" + fileName);
                        System.out.println("Please enter the new file name");
                        String fileName2 = user_input.nextLine();
                        System.out.println("You enter the new file name" + fileName2);
                        this.getDFS().mv(fileName, fileName2);
                        //user_input.close();
                        break;
                    case 8:  
                        System.out.println("Please enter the file name");
                        fileName = user_input.nextLine();
                        System.out.println("You enter the file name:" + fileName);
                        FileStream in3 = this.getDFS().head(fileName);
                        File File3 = in3.getFile();   
                        File3.getAbsolutePath(); 
                        System.out.println("Files path: " + File3.getAbsolutePath());
                        String text5 = "" +      File3.getAbsolutePath(); 
                        Path path3 = Paths.get(text5);       
                        byte[] bytes3 = Files.readAllBytes(path3);
                        String  text6 = new String(bytes3, StandardCharsets.UTF_8);
                        System.out.println("Files contents: " + text6);
                        in3.close();
                    break;
                    case 9:
                        System.out.println("Enter file name");
                        fileName = user_input.nextLine();
                        System.out.println("You enter the file name:" + fileName);
                        this.getDFS().runMapReduce(fileName);
                        break;
                    case 10:
                        System.out.println("\nExiting...see you later alligator!");
                        flag = false;
                        break;
    
                    default:
                        break;
                } //ends switch statement
    
                if (flag == false) {
                    return;
                } //end if-statement 
            } catch (NumberFormatException e) {
                System.out.println("\nPlease enter a number from 0 - 9. Do not enter words or strings.");
            } catch (InputMismatchException e) { 
                System.out.println("\nPlease enter a number from 0 - 9. Do not enter words or strings.");
            } catch(NullPointerException e) {
                System.out.println("A file or page you are trying to reach is null or does not exist! Try to recreate it using one of the options above.");
            }//end catch-block 
        } //ends while-loop
    } //end makingSelection() method
  
} //end UserInterface() class