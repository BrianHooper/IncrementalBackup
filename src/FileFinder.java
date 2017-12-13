/* Brian Hooper */
import java.io.*;
import java.util.HashMap;

public class FileFinder {
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
            BufferedReader outputReader = runCommand(new String[]{"find", path});
            BufferedReader fileReader;

            String line, fileType, fileSize, shortPath;

            // For each file in the source directory, determine
            // if it is a directory, or get its size
            while((line = outputReader.readLine()) != null) {
                // Get the "type" flag using Unix "file" command
                fileType = runCommand(new String[]{"file", line}).readLine();
                fileType = fileType.substring(line.length() + 2, fileType.length());

                if(line.length() != path.length()) { // Skip the source directory itself

                    // Don't include the full source path in the hashmap
                    shortPath = line.substring(skip, line.length());

                    // if the file is a directory, set size to -1
                    if (fileType.equals("directory")) {
                        fileList.put(shortPath, "-1");
                    } else {
                        // Otherwise, use the Unix command "stat" to get the size in bytes
                        fileSize = runCommand(new String[]{"stat", "--printf=\"%s\"", line}).readLine();

                        // Remove quotations surrounding size
                        fileSize = fileSize.substring(1, fileSize.length() - 1);

                        fileList.put(shortPath, fileSize);
                    }
                }
            }

            return fileList;
        } catch(IOException e) {
            System.out.println("Error reading files.");
            return null;
        }
    }

    public static BufferedReader runCommand(String[] command) {
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            return new BufferedReader(new InputStreamReader(p.getInputStream()));
        } catch (IOException | InterruptedException e) {
            System.out.println("Error running bash command.");
            return null;
        }
    }
}
