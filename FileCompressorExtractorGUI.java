import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.SwingWorker;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import java.util.zip.*;


public class FileCompressorExtractorGUI extends JFrame {

    private JTextField sourceFileField;
    private JTextField destinationFileField;
    private JTextArea statusArea;
    private JProgressBar progressBar;
    private JLabel timeElapsedLabel;
    private Timer timer;

    private JButton compressButton;
    private JButton extractButton;
   private JComboBox<String> compressionLevelComboBox;  // Declared at the class level
   private JButton cancelButton;

    // Add a JComboBox for selecting compression level


    
    private FileCompressor compressor;
    private FileExtractor extractor;
    private JButton infoButton;
    private JButton deleteButton;

    private long startTime;
    private long endTime;

    public FileCompressorExtractorGUI() {
        // Initialize compressor and extractor
        compressor = new FileCompressor();
        extractor = new FileExtractor();

        // Set up the frame
        setTitle("File Compressor and Extractor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);

        // Create panels
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JPanel statusPanel = new JPanel(new BorderLayout());

        // Buttons
        compressButton = new JButton("Compress");
        extractButton = new JButton("Extract");
         cancelButton = new JButton("Cancel");
         
         cancelButton.setEnabled(false);
        // Set custom fonts
        Font font = new Font("SansSerif", Font.BOLD, 14);
    statusArea = new JTextArea();
    statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    statusArea.setEditable(false);

        // Status area
        JScrollPane scrollPane = new JScrollPane(statusArea);

        // Info button
        infoButton = new JButton("Info");
        buttonPanel.add(infoButton);
        infoButton.addActionListener(e -> showFileInfo());
        
        // Initialize the Delete button
deleteButton = new JButton("Delete");

// Add Delete button to the button panel
buttonPanel.add(deleteButton);

// Add ActionListener for Delete button
deleteButton.addActionListener(e -> deleteFiles());

        
        // Set up progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        // Time elapsed label
        timeElapsedLabel = new JLabel("Time Elapsed: 0s");

        // Add buttons to button panel
        buttonPanel.add(compressButton);
     
        
       buttonPanel.add(cancelButton);
        buttonPanel.add(extractButton);
       String[] compressionLevels = { "Fastest", "Default", "Best Compression" };
        compressionLevelComboBox = new JComboBox<>(compressionLevels);
        buttonPanel.add(compressionLevelComboBox);
        
        

        // Add components to status panel
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(timeElapsedLabel, BorderLayout.EAST);

        statusPanel.add(new JLabel("Status:"), BorderLayout.NORTH);
        statusPanel.add(scrollPane, BorderLayout.CENTER);
        statusPanel.add(progressPanel, BorderLayout.SOUTH);

        // Add panels to frame
        add(buttonPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.CENTER);

        // Add button listeners
        compressButton.addActionListener(e -> showCompressOptions());
        extractButton.addActionListener(e -> showExtractOptions());
         cancelButton.addActionListener(e -> cancelOperation());
        setVisible(true);
    }
    
    private void deleteFiles() {
    // Open file chooser for selecting multiple files
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setMultiSelectionEnabled(true); // Allow multiple file selection
    int result = fileChooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
        File[] selectedFiles = fileChooser.getSelectedFiles();

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the selected file(s)?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            StringBuilder deletedFiles = new StringBuilder("Deleted Files:\n");
            for (File file : selectedFiles) {
                if (file.delete()) {
                    deletedFiles.append(file.getAbsolutePath()).append("\n");
                } else {
                    deletedFiles.append("Failed to delete: ").append(file.getAbsolutePath()).append("\n");
                }
            }

            // Display results in the status area
            statusArea.append(deletedFiles.toString() + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        }
    }
}

    
    private void showFileInfo() {
        // File chooser for selecting a file
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Gather file information
            String fileName = selectedFile.getName();
            String filePath = selectedFile.getAbsolutePath();
            long fileSize = selectedFile.length();
            String fileSizeFormatted = String.format("%.2f KB", fileSize / 1024.0);

            // Display file information
            statusArea.append("File Name: " + fileName + "\n");
            statusArea.append("File Path: " + filePath + "\n");
            statusArea.append("File Size: " + fileSizeFormatted + "\n\n");
        }
    }

private void showCompressOptions() {
    // Show file chooser for selecting multiple files to compress
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setMultiSelectionEnabled(true); // Allow multiple file selection

    // Add file filter for common file types
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Supported Files (txt, jpg, png, pdf, mp3, mp4)", "txt", "jpg", "png", "pdf", "mp3", "mp4");
    fileChooser.setFileFilter(filter);

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        File[] selectedFiles = fileChooser.getSelectedFiles();

        // Validate selected files
        for (File file : selectedFiles) {
            String fileExtension = getFileExtension(file);
            if (!isValidCompressionExtension(fileExtension)) {
                JOptionPane.showMessageDialog(this, "Unsupported file type: " + file.getName(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;  // Stop further execution
            }
        }

        StringBuilder paths = new StringBuilder();
        for (File file : selectedFiles) {
            if (paths.length() > 0) {
                paths.append(", ");
            }
            paths.append(file.getAbsolutePath());
        }

        // Ask for destination file with a custom name
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int folderResult = folderChooser.showSaveDialog(this);
        if (folderResult == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderChooser.getSelectedFile();
            String fileName = JOptionPane.showInputDialog(this, "Enter the name for the compressed file:");
            if (fileName != null && !fileName.trim().isEmpty()) {
                String destination = selectedFolder.getAbsolutePath() + File.separator + fileName + ".zip";
                compressFile(paths.toString(), destination); // Call compress method
            } else {
                JOptionPane.showMessageDialog(this, "Invalid file name.");
            }
        }
    }
}

private String getFileExtension(File file) {
    String fileName = file.getName();
    int dotIndex = fileName.lastIndexOf(".");
    return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
}

private boolean isValidCompressionExtension(String extension) {
    String[] validExtensions = {"txt", "jpg", "png", "pdf", "mp3", "mp4"};
    for (String ext : validExtensions) {
        if (ext.equalsIgnoreCase(extension)) {
            return true;
        }
    }
    return false;
}



private void showExtractOptions() {
    // Show file chooser for selecting .zip files
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // Allow only files

    // Add file filter for ZIP files
    FileNameExtensionFilter zipFilter = new FileNameExtensionFilter("ZIP Files", "zip");
    fileChooser.setFileFilter(zipFilter);

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedZipFile = fileChooser.getSelectedFile();

        // Validate the selected file to make sure it's a .zip file
        if (!selectedZipFile.getName().toLowerCase().endsWith(".zip")) {
            JOptionPane.showMessageDialog(this, "Please select a valid .zip file.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;  // Stop further execution
        }

        String source = selectedZipFile.getAbsolutePath(); // Get the file path

        // Show folder chooser for selecting where to extract files
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int folderResult = folderChooser.showOpenDialog(this);
        if (folderResult == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderChooser.getSelectedFile();
            if (selectedFolder != null) {
                extractFile(source, selectedFolder.getAbsolutePath()); // Call extract method
            }
        }
    }
}




private void compressFile(String source, String destination) {
    statusArea.append("Compressing files...\n");

    cancelButton.setEnabled(true);  // Enable the Cancel button when the task starts

    // Get the selected compression level from the combo box
    int selectedLevel = compressionLevelComboBox.getSelectedIndex();
    int compressionLevel = Deflater.DEFAULT_COMPRESSION; // Default compression level

    switch (selectedLevel) {
        case 0: // "Fastest"
            compressionLevel = Deflater.BEST_SPEED;
            break;
        case 1: // "Default"
            compressionLevel = Deflater.DEFAULT_COMPRESSION;
            break;
        case 2: // "Best Compression"
            compressionLevel = Deflater.BEST_COMPRESSION;
            break;
    }

    // Split the source string into individual file paths
    String[] sourceFiles = source.split(",\\s*");

    // Create and execute the CompressionWorker with the selected compression level
    CompressionWorker worker = new CompressionWorker(sourceFiles, destination, compressionLevel);
    compressButton.putClientProperty("worker", worker);  // Store the worker in the button
    worker.execute();  // Start the compression task
}

    private void cancelOperation() {
    // Check if a compression task is running
    CompressionWorker compressionWorker = (CompressionWorker) compressButton.getClientProperty("worker");
    if (compressionWorker != null && !compressionWorker.isDone()) {
        compressionWorker.cancel(true);  // Cancel the compression task
    }

    // Check if an extraction task is running
    ExtractionWorker extractionWorker = (ExtractionWorker) extractButton.getClientProperty("worker");
    if (extractionWorker != null && !extractionWorker.isDone()) {
        extractionWorker.cancel(true);  // Cancel the extraction task
    }

    cancelButton.setEnabled(false);  // Disable the cancel button after the task is canceled
}


private class CompressionWorker extends SwingWorker<Void, Integer> {
    private String[] sourceFiles;
    private String destination;
    private int compressionLevel;

    // Updated constructor
    public CompressionWorker(String[] sourceFiles, String destination, int compressionLevel) {
        this.sourceFiles = sourceFiles;
        this.destination = destination;
        this.compressionLevel = compressionLevel;  // Store the compression level
    }

    @Override
    protected Void doInBackground() throws Exception {
        startTime = System.currentTimeMillis();
        timer = new Timer(1000, e -> updateTimeElapsed());
        timer.start();

        long totalSize = 0;
        for (String filePath : sourceFiles) {
            File file = new File(filePath);
            if (file.exists()) {
                totalSize += file.length();
            }
        }

        long processedBytes = 0;

        try (FileOutputStream fos = new FileOutputStream(destination);
             ZipOutputStream zipOS = new ZipOutputStream(fos)) {

            zipOS.setLevel(compressionLevel);

            byte[] buffer = new byte[1024];

            for (String filePath : sourceFiles) {
                if (isCancelled()) {
                    return null;  // Exit if the task is cancelled
                }

                File file = new File(filePath);
                if (file.exists()) {
                    // Update status area with current file name
                    SwingUtilities.invokeLater(() -> statusArea.append("Compressing: " + file.getName() + "\n"));

                    try (FileInputStream fis = new FileInputStream(file)) {
                        zipOS.putNextEntry(new ZipEntry(file.getName()));

                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zipOS.write(buffer, 0, length);
                            processedBytes += length;

                            int progress = (int) ((processedBytes * 100) / totalSize);
                            publish(progress); // Update progress

                            if (isCancelled()) {
                                return null;  // Exit if the task is cancelled
                            }
                        }
                        zipOS.closeEntry();
                    }
                }
            }
        } catch (IOException e) {
            statusArea.append("Error compressing files: " + e.getMessage() + "\n");
        }
        return null;
    }

@Override
protected void process(java.util.List<Integer> chunks) {
    int progress = chunks.get(chunks.size() - 1);
    progressBar.setValue(progress);
    statusArea.setCaretPosition(statusArea.getDocument().getLength());  // Keep status area scrolled to bottom
}

@Override
protected void done() {
    try {
        endTime = System.currentTimeMillis();
        timer.stop();
        long timeTaken = (endTime - startTime) / 1000;
        statusArea.append("Compression completed. Time taken: " + timeTaken + " seconds.\n");
    } catch (Exception e) {
        statusArea.append("Error during compression: " + e.getMessage() + "\n");
    }
    progressBar.setValue(0);  // Reset progress bar after completion
     cancelButton.setEnabled(false);
}



    private void updateTimeElapsed() {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        long seconds = elapsedMillis / 1000;
        timeElapsedLabel.setText("Time Elapsed: " + seconds + "s");
    }
}



private void extractFile(String source, String destination) {
    statusArea.append("Extracting files...\n");

    cancelButton.setEnabled(true);  // Enable the Cancel button when the task starts

    ExtractionWorker worker = new ExtractionWorker(source, destination);
    extractButton.putClientProperty("worker", worker);  // Store the worker in the button
    worker.execute();  // Start the extraction task
}


private class ExtractionWorker extends SwingWorker<Void, Integer> {
    private String source;
    private String destination;

    public ExtractionWorker(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    protected Void doInBackground() throws Exception {
        startTime = System.currentTimeMillis();
        timer = new Timer(1000, e -> updateTimeElapsed());
        timer.start();

        long totalSize = new File(source).length();
        long processedBytes = 0;

        try (FileInputStream fis = new FileInputStream(source);
             ZipInputStream zipIS = new ZipInputStream(fis)) {

            ZipEntry entry;
            byte[] buffer = new byte[1024];

            while ((entry = zipIS.getNextEntry()) != null) {
                if (isCancelled()) {
                    return null;  // Exit if the task is cancelled
                }

                // Update status area with current file being extracted
                final ZipEntry currentEntry = entry;  // Make 'entry' effectively final
                SwingUtilities.invokeLater(() -> statusArea.append("Extracting: " + currentEntry.getName() + "\n"));

                File outFile = new File(destination, currentEntry.getName());
                outFile.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    int length;
                    while ((length = zipIS.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                        processedBytes += length;

                        int progress = (int) ((processedBytes * 100) / totalSize);
                        publish(progress); // Update progress

                        if (isCancelled()) {
                            return null;  // Exit if the task is cancelled
                        }
                    }
                }
            }
        } catch (IOException e) {
            statusArea.append("Error extracting file: " + e.getMessage() + "\n");
        }
        return null;
    }

    @Override
    protected void process(java.util.List<Integer> chunks) {
        int progress = chunks.get(chunks.size() - 1);
        progressBar.setValue(progress);
        statusArea.setCaretPosition(statusArea.getDocument().getLength());  // Keep status area scrolled to bottom
    }

    @Override
    protected void done() {
        try {
            endTime = System.currentTimeMillis();
            timer.stop();
            long timeTaken = (endTime - startTime) / 1000;
            statusArea.append("Extraction completed. Time taken: " + timeTaken + " seconds.\n");
        } catch (Exception e) {
            statusArea.append("Error during extraction: " + e.getMessage() + "\n");
        }
        progressBar.setValue(0);  // Reset progress bar after completion
        cancelButton.setEnabled(false);
    }

    private void updateTimeElapsed() {
        long elapsedMillis = System.currentTimeMillis() - startTime;
        long seconds = elapsedMillis / 1000;
        timeElapsedLabel.setText("Time Elapsed: " + seconds + "s");
    }
}

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(FileCompressorExtractorGUI::new);
    }
}
