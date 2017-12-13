/* Brian Hooper */
import java.io.*;
import java.util.HashMap;

public class FileFinder {

    /**
     * Reads the contents of a directory, including subdirectories
     * and stores the path of each file, as well as the file size
     * file size is -1 for directories
     * @param path String path of the directory to search
     * @return HashMap(String, String) with path as key and size as value
     */
    public static HashMap<String, String> read(String path) {
        // Don't allow empty paths
        if(path.length() == 0) {
            return null;
        }

        // Don't include the full source path in the hashmap
        int skip = path.length() + 1;

        HashMap<String, String> fileList = new HashMap<>();;

        try {
            // List files in the directory
            BufferedReader outputReader = getCommandOutput(new String[]{"find", path});

            String line, fileType, fileSize, shortPath;

            // For each file in the source directory, determine
            // if it is a directory, or get its size
            while((line = outputReader.readLine()) != null) {
                // Get the "type" flag using Unix "file" command
                fileType = getCommandOutput(new String[]{"file", line}).readLine();
                fileType = fileType.substring(line.length() + 2, fileType.length());

                if(line.length() != path.length()) { // Skip the source directory itself

                    // Don't include the full source path in the hashmap
                    shortPath = line.substring(skip, line.length());

                    // if the file is a directory, set size to -1
                    if (fileType.equals("directory")) {
                        fileList.put(shortPath, "-1");
                    } else {
                        // Otherwise, use the Unix command "stat" to get the size in bytes
                        fileSize = getCommandOutput(new String[]{"stat", "--printf=\"%s\"", line}).readLine();

                        // Remove quotations surrounding size
                        fileSize = fileSize.substring(1, fileSize.length() - 1);

                        fileList.put(shortPath, fileSize);
                    }
                }
            }
        } catch(IOException | InterruptedException e) {
            Standard.log("Error reading files from: " + path);
        }

        return fileList;
    }

    /*
     * Runs a bash command and returns its output as a BufferedReader
     */
    public static BufferedReader getCommandOutput(String[] command) throws IOException, InterruptedException {
        Process p = runCommand(command);
        if(p != null) {
            return new BufferedReader(new InputStreamReader(p.getInputStream()));
        } else {
            return null;
        }
    }

    /*
    Runs a bash command and returns the Process object
     */
    public static Process runCommand(String[] command) throws IOException, InterruptedException {
        Process p;
        p = Runtime.getRuntime().exec(command);
        p.waitFor();
        return p;
    }

    /*
    Runs a bash command and returns a String output
     */
    public static String getCommandString(String[] command) throws IOException, InterruptedException {
        BufferedReader commandReader = getCommandOutput(command);
        return commandReader.readLine();
    }
}
