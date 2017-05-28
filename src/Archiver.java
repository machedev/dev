import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JProgressBar;

import java.io.File; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.util.zip.ZipEntry; 
import java.util.zip.ZipOutputStream;

public class Archiver {

	private JFrame frame;
	private JTextField displayFolder;
	static JProgressBar progressBar;
	
	JFileChooser chooser;
	String folderToArchive;
	static int fileCount = 0;
	static int filesZipped = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Archiver window = new Archiver();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}		
		});
	}

	/**
	 * Create the application.
	 */
	public Archiver() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton chooseFolderButton = new JButton("Choose folder");
		chooseFolderButton.setToolTipText("Choose the folder you would like to zip");
		chooseFolderButton.setBounds(12, 36, 171, 25);
		chooseFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Choose the folder you would like to archive");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    chooser.setApproveButtonText("Choose folder");
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			        fileCount = 0;
			        filesZipped = 0;
			        folderToArchive = chooser.getSelectedFile().toString();
			        long folderSize = folderSize(chooser.getSelectedFile());
			        displayFolder.setText("Folder : " + folderToArchive + " Size on disk : " + humanReadableByteCount(folderSize, false));
			    }
			    else {
			        	System.out.println("No Selection made");
			    }
			}
		});
		
		JButton archiveButton = new JButton("Archive");
		archiveButton.setToolTipText("Archive the specified folder");
		archiveButton.setBounds(213, 36, 171, 25);
		archiveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String nameOfArchive = folderToArchive.substring(folderToArchive.lastIndexOf('\\')+1, folderToArchive.length()) + ".zip";
				FileOutputStream fos;
				ZipOutputStream zos;
				
				progressBar.setVisible(true);
				progressBar.setMinimum(0);
			    progressBar.setMaximum(fileCount);
				
				try {
					fos = new FileOutputStream(System.getProperty("user.home") + "\\Desktop\\" + nameOfArchive);
					zos = new ZipOutputStream(fos); 
					
					addDirToZipArchive(zos, new File(folderToArchive), null); 
			        zos.flush(); 
			        fos.flush(); 
			        zos.close(); 
			        fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		frame.getContentPane().setLayout(null);
		frame.getContentPane().add(chooseFolderButton);
		frame.getContentPane().add(archiveButton);
		
		displayFolder = new JTextField();
		displayFolder.setBounds(12, 106, 372, 22);
		frame.getContentPane().add(displayFolder);
		displayFolder.setColumns(10);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(12, 178, 372, 34);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		frame.getContentPane().add(progressBar);
	}
	
	public static void addDirToZipArchive(ZipOutputStream zos, File fileToZip, String parrentDirectoryName) throws Exception { 
        if (fileToZip == null || !fileToZip.exists()) { 
            return; 
        } 

        String zipEntryName = fileToZip.getName(); 
        if (parrentDirectoryName!=null && !parrentDirectoryName.isEmpty()) { 
            zipEntryName = parrentDirectoryName + "/" + fileToZip.getName(); 
        } 

        if (fileToZip.isDirectory()) { 
            for (File file : fileToZip.listFiles()) { 
                addDirToZipArchive(zos, file, zipEntryName); 
            } 
        } else { 
        	filesZipped += 1;
    		//progressBar.setString("Files zipped " + filesZipped);
    		
    		SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setValue(filesZipped);
            }
    		});
    		
            byte[] buffer = new byte[1024]; 
            FileInputStream fis = new FileInputStream(fileToZip); 
            zos.putNextEntry(new ZipEntry(zipEntryName)); 
            int length; 
            while ((length = fis.read(buffer)) > 0) { 
                zos.write(buffer, 0, length); 
            } 
            zos.closeEntry(); 
            fis.close(); 
        }
    }
	
	public static long folderSize(File directory) {
	    long length = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isFile()) {
	            length += file.length();
	        	fileCount += 1;
	        }
	        else
	            length += folderSize(file);
	    }
	    return length;
	}
	
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
