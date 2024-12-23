// Program: CommandLineInterpreter.java
// Description:
// Author: 20221101 20221012 20220294 20210804
// Date: 30-10-2024
// Version: 1.1
/* File run command: javac CommandLineInterpreter.java; java CommandLineInterpreter */
// ----------------------------------------------------------------------------------------------------------------------------- //

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
// import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
// import java.util.Deque;
import java.util.Scanner;
import java.util.stream.Stream;
import java.nio.file.*;

public class CommandLineInterpreter {
    public static void main(String[] args) {
        maestro();
        // ls(); //tested
        // lsr(); //tested
        // System.out.println(pwd()); // tested
    }

    
    // helper function
    public static boolean isCommand(String txt){
        switch (txt.toLowerCase()) {
            case "cd":
            case "pwd":
            case "ls":
            case "ls -a":
            case "ls -r":
            case ">>":
            case ">":
            case "rm":
            case "mv":
            case "rmdir":
            case "mkdir":
            case "|":
            case "touch":
            case "cat":
            case "help":
            case "exit":
                return true;
                    
            default:
                return false;
        }

    }

    // variable to simulate the current Directory. it Creates a path object pointing
    // to the current directory then matching the
    // absolute path to the current and removing redundunt symbols , Shared(static)
    // var to maintain only one session (for each instance) of the commandline
    public static Path currentDirectory = Paths.get(".").toAbsolutePath().normalize();

    // dispatcher method that determines which command should be invoked cd, ls, mv...
    public static void maestro() {

        // taking input form the user
        Scanner scanner = new Scanner(System.in);
        String command, commandOp;
        String[] slicedCommand;

        while (true) {

            System.out.println(">>> ");
            command = scanner.nextLine();

            // checks if the user want to terminate the command line
            if (command.equals("exit") || command.equals("EXIT")) { // == checks for refrence not value use .equals to compare strings content
                
                break;
           
            }

            // list available commands using help command
            else if (command.equals("help") || command.equals("HELP")) {
                help();
                continue;

            }

            /* input special cases */
            // seperating the command operator and command operands using split
            // handling appending >> operator
            if (command.contains(" >> ")) { // 0 1 2
                commandOp = ">>";
                slicedCommand = command.split(" >> ");
            }

            // handling > operator
            else if (command.contains(" > ")) { // 0 1 2
                commandOp = ">";
                slicedCommand = command.split(" > ");
            }

            // handling | operator
            else if (command.contains(" | ")) { // 0 1 2
                commandOp = "|";
                slicedCommand = command.split(" | ");
            }

            else {

                slicedCommand = command.split(" ");
                commandOp = slicedCommand[0];
            }

            // Handling Variations of ls command
            if (slicedCommand.length > 1 && commandOp.equals("ls") || commandOp.equals("LS")) {
                if (slicedCommand[1].equals("-r") || slicedCommand[1].equals("-a")) {
                    commandOp += ' ' + slicedCommand[1];

                }

            }

            // dispatching
            switch (commandOp) {
                case "cd":
                case "CD":
                    // handling user invalid input case: nothing after cd
                    if (slicedCommand.length < 2) {
                        System.out.println("please enter the path to navigate after cd!");
                    }
                    else {
                        cd(slicedCommand[1]); // passing the target path from the sliced command parts
                    }
                    break;
                case "pwd":
                case "PWD":

                    System.out.println(pwd());

                    break;

                case "mv":
                case "MV":

                    if (slicedCommand.length < 3) {
                        System.out.println("missing paramaters for mv command!");
                        break;
                    }
                    mv(slicedCommand[1], slicedCommand[2]);
                    break;


                case "ls":
                case "LS":

                    ls();

                    break;

                case "ls -r":
                case "LS -R":

                    String[] lsrReturn = lsr();
                    for (String string : lsrReturn) {
                        System.out.println(string);

                    }
                    break;

                case "ls -a":
                case "LS -A":

                    String[] lsAReturn = lsA();
                    for (String string : lsAReturn) {
                        System.out.println(string);
                    }
                    break;

                case "mkdir":
                case "MKDIR":
                    mkdir(slicedCommand[1]);

                    break;

                case "rm":
                case "RM":
                    rm(slicedCommand[1]);
                    break;

                case "rmdir":
                case "RMDIR":
                    // casting the input string into a file type
                    File dirToRemove = new File(slicedCommand[1]);
                    rmdir(dirToRemove);
                    break;

                case "touch":
                case "Touch":

                    touch(slicedCommand[1]);

                    break;

                case "cat":
                case "CAT":


                    cat(slicedCommand[1]);
                    break;

                case ">>": 

                    Path f = Paths.get(slicedCommand[1]); // casting the Sring filename into type path
                    System.out.println(f);
                    appendToFile(slicedCommand[0], f);

                    break;

                case ">": 

                    try {
                        redirectToFile(slicedCommand[1], slicedCommand[0]);
                    } catch (IOException e) {
                        System.out.println("IOException Happened");
                    }

                    break;
                case "|":
                    pipe(slicedCommand[0],slicedCommand[2]);
                    break;

                default:
                    System.err.println("Invalid Command!");
                    break;
            }

        }

        scanner.close();
    }

    // ------------------------------------------------------- Help ------------------------------------------------------------ //

    public static void help() {
        System.out.println("The Following is a List of Commands Available ");
        System.out.println(); // prints newline \n
        System.out.println("pwd     \tPrints the current working directory");
        System.out.println("cd      \tChanges the current directory to the specified directory");
        System.out.println("ls      \tLists the contents of the current directory");
        System.out.println("ls -a   \tLists all contents, including hidden files and directories");
        System.out.println("ls -r   \tLists the contents of the directory in reverse order");
        System.out.println("mkdir   \tCreates a new directory with the specified name");
        System.out.println("touch   \tCreates a new empty file or updates the timestamp of an existing file");
        System.out.println("mv        \tMoves or renames a file or directory");
        System.out.println("rm      \tRemoves the specified file");
        System.out.println("cat     \tDisplays the contents of the specified file.");
        System.out.println(">       \tRedirects the output of a command to a file, overwriting the file if it exists");
        System.out.println(">>      \tRedirects the output of a command to a file, appending to the file if it exists");
        System.out.println("|       \tPipes the output of one command as input to another command");
        System.out.println("rmdir    \tdeletes the specified directory");
        System.out.println("exit    \tExits the shell or program");

    }

    // --------------------------------------------------- ls command ---------------------------------------------------------- //

    public static String [] ls() {
        /* the dot  works in linux-unix systems */
        try (Stream<Path> stream = Files.list(currentDirectory)) {
            /* works on all systems */
            // Collect the stream into a list first
            // String[] list = stream
            // .filter(path -> {
            //     try {
            //         return !Files.isHidden(path); // Check hidden status, catching exceptions
            //     } catch (IOException e) {
            //         System.err.println("Could not determine if file is hidden: " + path);
            //         return false; // Skip the file if there's an exception
            //     }
            // })
            // .map(path -> path.getFileName().toString())
            // .toArray(String[]::new);
        
            /* linux unix systems */    // since we are simulating linux terminal , So....
            String[] list = stream
            .filter(path -> !path.getFileName().toString().startsWith(".")) // Filter out hidden files
            .map(path -> path.getFileName().toString()) // Convert Path to String
            .toArray(String[]::new); // Collect into a String array

            for(String s : list) {System.out.println(s);}

            return list;
            
        } catch (Exception e) {
            System.err.println("Exception occurred! Check directory syntax and try again.");
        }
        return new String[0]; // Return an empty array if an exception occurs
    }


    // --------------------------------------------------- ls -r command ------------------------------------------------------- //

    public static String[] lsr() {
        File[] list = new File(currentDirectory.toString()).listFiles();
        if (list != null) {
            Arrays.sort(list, Collections.reverseOrder());
            String[] fileNames = new String[list.length];
            int index = 0;

            /* Skips hidden files on unix-linux systems */
            for (File file : list) {
                // Skip hidden files 3shan de esmha msh elmafrood yzhr
                if (file.getName().charAt(0) != '.') {
                    fileNames[index++] = file.getName();
                }
            }
            // Trim the array to remove any unused slots
            return Arrays.copyOf(fileNames, index);
        }
        return new String[0]; // Return an empty array if the directory is empty or null
    }

    // --------------------------------------------------- ls -a command ---------------------------------------------------------- //

    public static String[] lsA() {
        File[] list = new File(currentDirectory.toString()).listFiles();

        if (list != null) {
            // Sort files in natural order
            Arrays.sort(list); // This will sort the files in their natural order (alphabetically)

            String[] fileNames = new String[list.length];
            int index = 0;

            for (File file : list) {
                fileNames[index++] = file.getName();
            }

            // Trim the array to remove any unused slots
            return Arrays.copyOf(fileNames, index);
        }
        return new String[0]; // Return an empty array if the directory is empty or null
    }
    // --------------------------------------------------- pwd command ---------------------------------------------------------- //

    public static Path pwd() {
        /* not ideal the path is fixed along the program manipulations */
        // Path currentPath = Paths.get(".").toAbsolutePath(); // create a path object
        // pointing to the current directory then matching the absolute path to the
        // current
        // return currentPath.getParent(); // get parent to exclude the . in the
        // absolute path

        return currentDirectory;

    }

    // --------------------------------------------------- cd command ---------------------------------------------------------- //
    public static Path cd(String targetPath) {
        // return to the parent case
        if (targetPath.equals("../")) {
            currentDirectory = currentDirectory.getParent();
            return currentDirectory;

        }

        // moving to subdirectory cas
        Path newPath = currentDirectory.resolve(targetPath).normalize();
        if (Files.exists(newPath) && Files.isDirectory(newPath)) {

            currentDirectory = newPath;
            return currentDirectory;

        } else {
            System.out.println("No such a file or Directory of :" + newPath);
        }

        return null;

    }

    // --------------------------------------------------- >> command ---------------------------------------------------------- //

    public static void appendToFile(String text, Path file) {
        /* append to an existing file, fail if the file does not exist */
        byte[] dataToWrite = new byte[0];   // bytes to write

        // matching the absolute path of the Current directory to the entered file name
        // to access the file correctly
        file = currentDirectory.resolve(file).normalize();

        // handling commands
        if (isCommand(text)) {
            String [] CommReturnArr;
            String CommReturnstr = "";
            switch (text.toLowerCase()) {   // handling different cases

                case "ls":

                    CommReturnArr = ls();
                    for (String cr : CommReturnArr) {
                        CommReturnstr += '\n' + cr;
                    }

                    dataToWrite = CommReturnstr.getBytes();
                    
                    break;
                
                case "ls -r":

                    CommReturnArr = lsr();
                    for (String cr : CommReturnArr) {
                        CommReturnstr += '\n' + cr;
                    }

                    dataToWrite = CommReturnstr.getBytes();
                    
                    break;
                case "ls -a":

                    CommReturnArr = lsA();
                    for (String cr : CommReturnArr) {
                        CommReturnstr += '\n' + cr;
                    }

                    dataToWrite = CommReturnstr.getBytes();
            
                    break;
                default:
                    break;
            }
            
        }

        else{
            
            // convert the string to byte array to be compatiable with write function
            dataToWrite = text.getBytes();
        }


        // write to a file by using the newOutputStream(Path, OpenOption...) method.
        // Using a BufferedOutputStream is a best practice in Java I/O operations,
        // as it significantly boosts performance and provides better control over data
        // writing
        if (Files.exists(file)) {
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.APPEND))) {
                out.write(dataToWrite, 0, dataToWrite.length);

            }

            catch (IOException e) {
                System.out.println("IOException Happend!");

            }

        }

        else {

            System.out.println("No Such a file Exist : " + file);

        }

    }

    // --------------------------------------------------- Touch ---------------------------------------------------------- //
    public static void touch(String filename) {
        String dir = pwd().toString();
        File file = new File(dir, filename);

        try {
            if (file.exists()) {
                // Update the timestamp of the existing file
                boolean updated = file.setLastModified(System.currentTimeMillis());
                if (updated) {
                    System.out.println("Timestamp updated for: " + filename);
                } else {
                    System.out.println("Failed to update timestamp for: " + filename);
                }
            } else {
                // Create a new file
                boolean created = file.createNewFile();
                if (created) {
                    System.out.println("File created: " + filename);
                } else {
                    System.out.println("Failed to create file: " + filename);
                }
            }
        } catch (IOException e) {
            System.out.println("An I/O error occurred: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("Permission denied: Unable to modify or create the file " + filename);
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    // --------------------------------------------------- > command ---------------------------------------------------------- //
    public static void redirectToFile(String fileName, String content) throws IOException {
        // handle Commands
        if (isCommand(content)) {
            String [] CommReturnArr;
            String CommReturnstr = "";
            switch (content.toLowerCase()) {   // handling different cases

                case "ls":

                    CommReturnArr = ls();
                    for (String cr : CommReturnArr) {
                        CommReturnstr += '\n' + cr;
                    }

                    // dataToWrite = CommReturnstr.getBytes();
                    content = "";
                    content = CommReturnstr;
                    
                    break;
                case "ls -r":

                    CommReturnArr = lsr();
                    for (String cr : CommReturnArr) {
                        CommReturnstr += '\n' + cr;
                    }

                    // dataToWrite = CommReturnstr.getBytes();
                    content = "";
                    content = CommReturnstr;
                    
                    break;
                case "ls -a":

                    CommReturnArr = lsA();
                    for (String cr : CommReturnArr) {
                        CommReturnstr += '\n' + cr;
                    }

                    // dataToWrite = CommReturnstr.getBytes();
                    content = "";
                    content = CommReturnstr;
            
                    break;
                default:
                    break;
            }
            
        }


        // Open the file in write mode which will overwrite existing content
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content);
            System.out.println("Content written to file = " + fileName);
        } catch (IOException e) {
            System.out.println("Can't write to " + fileName);
        }
    }

    // --------------------------------------------------- mv command ---------------------------------------------------------- //
    public static void mv(String sourcePath, String destinationPath) {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destinationPath);

        // Check if the source file exists
        if (!sourceFile.exists()) {
            System.out.print("Error: Source file does not exist.");
            return ;
        }

        // If the destination is a directory, construct the destination file path
        if (destFile.isDirectory()) {
            destFile = new File(destFile, sourceFile.getName());
        }

        // Attempt to rename or move the file
        boolean success = sourceFile.renameTo(destFile);

        if (success) {
            System.out.println("Successfully moved/renamed to: " + destFile.getPath());
        } else {
            System.out.println("Error: Failed to move/rename the file.");
        }
    }

    // --------------------------------------------------- mkdir command ---------------------------------------------------------- //
    public static void mkdir(String name) {
        Path path = currentDirectory.resolve(name).normalize();

        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            System.out.println("Error");
        }
    }

    // --------------------------------------------------- rm command ---------------------------------------------------------- //

    public static void rm(String name) {
        try {
            Path filePath = currentDirectory.resolve(name);
            if (Files.isDirectory(filePath) && Files.list(filePath).findAny().isPresent()) {
                System.out.println("Error: Directory is not empty.");
            } else {
                Files.delete(filePath);
                System.out.println("Deleted: " + filePath.toString());
            }
        } catch (NoSuchFileException e) {
            System.out.println("Error: File or directory not found.");
        } catch (IOException e) {
            System.out.println("Error: Unable to delete. " + e.getMessage());
        }
    }

    //TJIS FUNTION IS USED FOR CLEANING A STRING FROM ITS EXTENTION TO PASS TO PIPE SECOND COMMAND
    public static String copyUntilChar(String input, char stopChar) {
        int index = input.indexOf(stopChar);
        if (index != -1) {
            return input.substring(0, index);
        } else {
            return input; // Return the whole string if the character is not found
        }
    }

    // --------------------------------------------------- | command ---------------------------------------------------------- //
    // Simulate pipe between commands
    public static String pipe(String command1, String command2) {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Execute the first command and capture output
            switch (command1) {
                case "ls":
                    ls();  // Assuming ls() is implemented elsewhere
                    break;
                case "pwd":
                    System.out.println(pwd());  // Assuming pwd() is implemented elsewhere
                    break;
                default:
                    System.out.println("Unsupported first command for pipe.");
                    return "";
            }






            // Capture the output of the first command
            System.setOut(originalOut);  // Reset to standard output
            String output = outContent.toString().trim();
            String decoy = copyUntilChar(output, '.');

            // Execute the second command with the output from command1 as input
            switch (command2) {
                case "cat":
                    Path catPath = currentDirectory.resolve(pwd());
                    cat(String.valueOf(catPath)); // Mimics `cat` by displaying the output of command1
                    break;

                case "touch":
                {
                    touch(output);
                    break;
                }
//                case "grep":
//                    // Prompt user for search term only if `grep` is requested
//                    try (Scanner scanner = new Scanner(System.in)) {
//                        System.out.print("Enter text to search: ");
//                        String searchText = scanner.nextLine();
//
//                        // Filter lines that contain the search text
//                        Arrays.stream(output.split("\n"))
//                                .filter(line -> line.contains(searchText))
//                                .forEach(System.out::println);
//                    }
//                    break;
                default:
                    System.out.println("Unsupported second command for pipe.");
                    return "";
            }

            return output;
        } finally {
            System.setOut(originalOut);  // Ensure output is reset
        }
    }

    // --------------------------------------------------- cat command ---------------------------------------------------------- //

    // Reads the contents of files and prints them
    public static void cat(String filePath) {
        File file = new File(filePath);

        // Check if the path is a file and readable
        if (!file.isFile()) {
            System.err.println("Error: " + filePath + " is not a file or cannot be read.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath + ": " + e.getMessage());
        }
    }

    // --------------------------------------------------- rmdir command ---------------------------------------------------------- //

    // Check if a directory is empty <Helper>
    private static boolean isDirectoryEmpty(File directory) {
        String[] files = directory.list();
        return files == null || files.length == 0;
    }

    // Deletes an empty directory
    public static void rmdir(File directory) {
        if (directory.isDirectory()) {
            if (isDirectoryEmpty(directory)) {
                if (directory.delete()) {
                    System.out.println("Deleted directory " + directory.getName() + " successfully.");
                } else {
                    System.err.println("Error deleting directory " + directory.getName());
                }
            } else {
                System.out.println("Directory " + directory.getName() + " is not empty.");
            }
        } else {
            System.err.println(directory.getPath() + " is not a directory.");
        }
    }

}










