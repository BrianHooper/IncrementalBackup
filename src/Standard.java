/* Brian Hooper */
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Standard {

    // Location of configuration file
    public static final String configLocation = "/home/brian/Admin/scripts";

    // Log error output
    public static StringBuilder errorLog;


    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat(".MMMMM dd yyyy hh:mm aaa");
        log("Starting program: " + df.format(new Date()));
        new Standard().run();
    }

    /**
     * Writes a message to the log file
     * @param message String to write
     */
    public static void log(String message) {
        if(errorLog == null)
            errorLog = new StringBuilder();

        errorLog.append(message);
        errorLog.append("\n");
    }

    /**
     * Writes a message to the log file and
     * outputs the message to the console
     * @param message String to write
     */
    public static void logPrint(String message) {
        System.out.println(message);
        log(message);
    }

    /**
     * Writes the entire contents of the log
     * to a file located in the configuration directory
     */
    public static void writeLog() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(configLocation + "/logFile"));
            writer.write(errorLog.toString());
            writer.close();
        } catch(IOException e) {
            System.out.println("Error writing log file. ");
        }
    }

    /**
     * Runs the program
     */
    public void run() {
        // Get the default backup location from the config file
        HashMap<String, String> configuration = readConfig();

        // Ask the user for the directories
        Scanner scan = new Scanner(System.in);
        String sourceDirectory = clean(getSourceDirectory(scan));
        String destinDirectory = clean(getBackupDirectory(configuration, scan));

        // Append the name of the source folder to the path of the destination folder
        String[] sourceSplit = sourceDirectory.split("/");
        String backupFolder = sourceSplit[sourceSplit.length - 1];
        destinDirectory = destinDirectory + "/" + backupFolder;

        // If the backup location does not exist, attempt to create it.
        if(!checkDirectory(destinDirectory)) {
            Standard.logPrint("Error creating backup directory");
            return;
        }

        // Log the directories
        Standard.log("Source: " + sourceDirectory);
        Standard.log("Destination: " + destinDirectory);

        // Read the files from the source directory
        HashMap<String, String> sourceFiles = FileFinder.read(sourceDirectory);

        // Read files from the destination (backup) directory
        HashMap<String, String> destinFiles = FileFinder.read(destinDirectory);

        // Determine which files need to be copied
        LinkedList<String> changedFiles = compare(sourceFiles, destinFiles);
        Standard.logPrint(changedFiles.size() + " file(s) will be copied.");
        Standard.logPrint((sourceFiles.size() - changedFiles.size()) + " file(s) will not be copied.");

        // Copy the files that have changed
        int fileCount = copyFiles(changedFiles, sourceDirectory, destinDirectory);
        if(fileCount > 0) {
            Standard.logPrint("Copied " + fileCount + " file(s)");
        }
        Standard.writeLog();
    }

    /**
     * Copies files in the changedFiles list
     * @param changedFiles List of changed files
     * @param sourceDirectory path of source
     * @param destinDirectory path of destination
     * @return number of files copied
     */
    public int copyFiles(LinkedList<String> changedFiles, String sourceDirectory, String destinDirectory) {
        int fileCount = 0;
        String[] command = {"cp", "-rf", "", ""};

        for(String path : changedFiles) {
            command[2] = sourceDirectory + "/" + path;
            command[3] = destinDirectory + "/" + path;

            Standard.log("Copying: " + path);
            try {
                FileFinder.getCommandString(command);
                fileCount++;
            } catch(IOException | InterruptedException e) {
                Standard.logPrint("Error copying files:\n" + command);
            }
        }

        return fileCount;
    }

    /**
     * Checks if a location exists, and creates it if
     * it is not found
     * @param location String path of directory
     * @return true if created or found, false if unable to create
     */
    public boolean checkDirectory(String location) {
        File file = new File(location);
        if(file.exists()) {
            return true;
        } else {
            file.mkdirs();
            if(file.exists()) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Reads the configuration file
     * @return Hashmap containing settings and values
     */
    public HashMap<String, String> readConfig() {
        Scanner scan;
        HashMap<String, String> configList = new HashMap<>();
        try {
            scan = new Scanner(new File(configLocation + "/backupConfig"));
            String line;
            String[] splitLine;

            while(scan.hasNextLine()) {
                line = scan.nextLine();
                if(line.charAt(0) != '#') {
                    splitLine = line.split("=");
                    if(splitLine.length == 2) {
                        configList.put(splitLine[0], splitLine[1]);
                    }
                }
            }
            scan.close();
        } catch(FileNotFoundException e) {
            Standard.log("Config file does not exist");
        } finally {
            return configList;
        }
    }

    /**
     * Asks the user for the backup (destination) directory
     * or uses the default directory given by the config file
     * @param config HashMap configuration settings
     * @param scan Scanner for System.in
     * @return String path of backup directory
     */
    public String getBackupDirectory(HashMap<String, String> config, Scanner scan) {
        String backupLocation = "";
        if(config != null && config.containsKey("defaultDirectory")) {
            backupLocation = config.get("defaultDirectory");
        }

        String userInput = "n";

        if(backupLocation.length() > 0) {
            System.out.println("Backup directory: " + backupLocation);
            System.out.print("Use this directory? (Y/n)");
            userInput = scan.nextLine();
        } else {
            System.out.println("Default backup location not found.");
        }

        if(userInput.equals("n")) {
            System.out.println("Enter backup directory: ");
            backupLocation = scan.nextLine();
        }
        return backupLocation;
    }

    /**
     * Asks the user for the source directory
     * or uses the current working directory
     * @param scan Scanner for System.in
     * @return String path of source directory
     */
    public String getSourceDirectory(Scanner scan) {
        String sourceLocation = System.getProperty("user.dir");
        System.out.println("Source directory: " + sourceLocation);
        System.out.print("Use this directory? (Y/n)");
        String userInput = scan.nextLine();
        if(userInput.equals("n")) {
            System.out.println("Enter source directory: ");
            sourceLocation = scan.nextLine();
        }
        return sourceLocation;
    }

    /**
     * Checks that input paths do not have trailing slashes
     * @param path input path
     * @return modified path
     */
    public String clean(String path) {
        if(path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Creates a linked list of Strings containing paths to files
     * that are different between the source and directories
     * @param sourceFiles HashMap list of source files
     * @param destinFiles HashMap list of destination files
     * @return LinkedList of paths to modified objects
     */
    public LinkedList<String> compare(HashMap<String, String> sourceFiles, HashMap<String, String> destinFiles) {
        if(sourceFiles == null || destinFiles == null)
            return null;

        String filePath, sourceSize, destinSize;
        LinkedList<String> changedFiles = new LinkedList<>();

        for(Map.Entry<String, String> sourceFile : sourceFiles.entrySet()) {
            filePath = sourceFile.getKey();
            sourceSize = sourceFile.getValue();

            if(destinFiles.containsKey(filePath)) {
                destinSize = destinFiles.get(filePath);
                if(sourceSize.equals(destinSize)) {
                    // Source and destination files are identical, no need to back up
                    Standard.log("Matches existing file: " + filePath);

                } else {
                    // Destination file exists, but is different size. Backup.
                    Standard.log("Different than existing file: " + filePath + " source: " + sourceSize + " destination: " + destinSize);
                    changedFiles.add(filePath);
                }
            } else {
                // Destination file does not exist, backup.
                Standard.log("Not backed up: " + filePath + " size: " + sourceSize + " bytes.");
                changedFiles.add(filePath);
            }
        }

        return changedFiles;
    }
}
