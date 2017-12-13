/* Brian Hooper */
import java.util.HashMap;
import java.util.Map;

public class Standard {

    public static void main(String[] args) {

        // Read the files from the source directory
        HashMap<String, String> sourceFiles = FileFinder.read("/home/brian/Documents/backupSandbox/new");

        // Read files from the destination (backup) directory
        HashMap<String, String> destinFiles = FileFinder.read("/home/brian/Documents/backupSandbox/old");

        String filePath, sourceSize, destinSize;

        // Compare the sizes of each file
        if(sourceFiles != null) {
            for(Map.Entry<String, String> sourceFile : sourceFiles.entrySet()) {
                filePath = sourceFile.getKey();
                sourceSize = sourceFile.getValue();

                if(destinFiles.containsKey(filePath)) {
                    destinSize = destinFiles.get(filePath);
                    if(sourceSize.equals(destinSize)) {
                        // Source and destination files are identical, no need to back up
                        System.out.println("Matches existing file: " + filePath);
                    } else {
                        // Destination file exists, but is different size. Backup.
                        System.out.println("Different than existing file: " + filePath + " source: " + sourceSize + " destination: " + destinSize);
                    }
                } else {
                    // Destination file does not exist, backup.
                    System.out.println("Not backed up: " + filePath + " size: " + sourceSize + " bytes.");
                }
            }
        }
    }
}
