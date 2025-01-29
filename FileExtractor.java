import java.io.*;
import java.util.zip.*;

public class FileExtractor {

    public void extractZipFile(String source, File destinationDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File extractedFile = new File(destinationDir, entry.getName());
                if (entry.isDirectory()) {
                    extractedFile.mkdirs();
                } else {
                    try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    public void extractGzipFile(String source, File destinationDir) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(source));
             FileOutputStream fos = new FileOutputStream(new File(destinationDir, new File(source).getName().replace(".gz", "")))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
    }
}
