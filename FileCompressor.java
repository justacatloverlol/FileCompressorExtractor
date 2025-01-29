import java.io.*;
import java.util.zip.*;

public class FileCompressor {
    
    public void compressFile(String sourceFile, String destinationFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destinationFile);
             GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
             
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                gzipOS.write(buffer, 0, length);
            }
        }
        System.out.println("File compressed successfully: " + destinationFile);
    }
}
