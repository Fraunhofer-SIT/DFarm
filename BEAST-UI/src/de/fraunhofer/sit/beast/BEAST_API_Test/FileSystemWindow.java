package de.fraunhofer.sit.beast.BEAST_API_Test;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.io.FileUtils;

import de.fraunhofer.sit.beast.client.api.FileSystemApi;
import de.fraunhofer.sit.beast.client.invoker.ApiException;
import de.fraunhofer.sit.beast.client.models.DeviceInformation;
import de.fraunhofer.sit.beast.client.models.FileOnDevice;

public class FileSystemWindow extends JFrame {

	private JPanel contentPane;
	private JTextField txtPath;
	private JTable tblFS;
	public List<FileOnDevice> files = Collections.emptyList();
	private FSModel model;
	private FileSystemApi fsAPI;
	private DeviceInformation device;
	private String currentPath;
	

	class FSModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Name";
			case 1:
				return "Size";
			case 2:
				return "Last modified";
			default:
				return null;
			}
		}

		public int getRowCount() {
			return files.size() + 1;
		}

		public Object getValueAt(int row, int col) {
			if (row == 0) {
				if (col == 0)
					return ".. (Parent directory)";
				return null;
			}
			FileOnDevice d = files.get(row - 1);
			switch (col) {
			case 0:
				return d.getName();
			case 1:
				if (d.getDirectory())
					return "Directory";
				return FileUtils.byteCountToDisplaySize(d.getSize());
			case 2:
				if (d.getLastModified() == null)
					return null;
				return d.getLastModified().toLocalDateTime().toString();
			}
			return null;
		}
		
	}


	public FileSystemWindow(DeviceInformation dev, FileSystemApi fsAPI) {
		setTitle("Explore File System of " + dev.getName());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFiles = new JMenu("Files");
		menuBar.add(mnFiles);
		
		JMenuItem mnUpload = new JMenuItem("Upload");
		mnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						uploadFile();
					}
					
				});
			}
		});
		mnFiles.add(mnUpload);
		
		JMenuItem mntmDownload = new JMenuItem("Download...");
		mntmDownload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		mntmDownload.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				downloadFile();
			}
		});
		mnFiles.add(mntmDownload);
		
		JMenuItem mntmRefresh = new JMenuItem("Refresh");
		mntmRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showDirectory(currentPath);
			}
		});
		mntmRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		mnFiles.add(mntmRefresh);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblPath = new JLabel("Path:");
		panel.add(lblPath, BorderLayout.WEST);
		
		txtPath = new JTextField();
		panel.add(txtPath, BorderLayout.CENTER);
		txtPath.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		model = new FSModel();
		tblFS = new JTable(model);
		scrollPane.setViewportView(tblFS);
		txtPath.setText("/");
		
		JButton btnGo = new JButton("Go");
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showDirectory(txtPath.getText());
			}
		});
		panel.add(btnGo, BorderLayout.EAST);
		SwingUtilities.getRootPane(btnGo).setDefaultButton(btnGo);
		this.fsAPI = fsAPI;
		this.device = dev;
		showDirectory(txtPath.getText());
		tblFS.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {
				
				
			}
			
			public void mousePressed(MouseEvent e) {
				
				
			}
			
			public void mouseExited(MouseEvent e) {
				
				
			}
			
			public void mouseEntered(MouseEvent e) {
				
				
			}
			
			public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 2 && tblFS.getSelectedRow() != -1) {
		        	if (tblFS.getSelectedRow() == 0) {
		        		//Parent dir
		        		if (!currentPath.equals("/"))
		        		{
		        			showDirectory(currentPath.substring(0, currentPath.lastIndexOf('/')));
		        		}
		        		return;
		        	}
		        	FileOnDevice el = files.get(tblFS.getSelectedRow() - 1);
		        	if (el.getFile())
		        		downloadFile();
		        	else {
	        			showDirectory(el.getFullPath());
		        	}
		        }
				
			}
		});
	}


	protected void uploadFile() {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify a file to upload");   
		fileChooser.setMultiSelectionEnabled(true);
		int userSelection = fileChooser.showOpenDialog(FileSystemWindow.this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			final ProgressWindow window = new ProgressWindow();
			long sz = 0;
			for (File i : fileChooser.getSelectedFiles()) {
				sz += i.length();
			}
			final File[] files = fileChooser.getSelectedFiles();
			final long l = sz;
			new Thread(new Runnable() {

				public void run() {
					window.show();
					int c = 0;
					final AtomicLong sentBytes = new AtomicLong();
					for (final File f : files) {
						try {
							final int cindex = c + 1;
							fsAPI.uploadAsync(device.getID(), currentPath, f, new OwnApiCallback<Void>() {
								
								
								@Override
								public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
									if (done) {
										sentBytes.addAndGet(bytesWritten);
										return;
									}
									window.showProgress(String.format("Uploading file %d of %d: %s", cindex, files.length, f.getName()), sentBytes.get() + bytesWritten, l);
								}

								public void onSuccess(Void result, int statusCode,
										Map<String, List<String>> responseHeaders) {
									
								}

							}).execute();
						} catch (Exception e) {
							DeviceChooser.handleError(e);
						}
						c++;
					}
				}
				
			}).start();
			
		}
	}


	protected void downloadFile() {
    	if (tblFS.getSelectedRow() == -1)
    	{
    		JOptionPane.showMessageDialog(this, "No file selected");
    		return;
    	}
    	final FileOnDevice el = files.get(tblFS.getSelectedRow() - 1);
    	if (!el.getFile())
    	{
    		JOptionPane.showMessageDialog(this, String.format("%s is not a file", el.getFullPath()));
    		return;
    	}
    	try {
			final ProgressWindow window = new ProgressWindow();
			
			window.show();
			fsAPI.downloadAsync(device.getID(), el.getFullPath(), new OwnApiCallback<File>() {
				
				
				@Override
				public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
					if (done) {
						window.dispose();
						return;
					}
					window.showProgress("Downloading " + el.getName(), bytesRead, contentLength);
				}

				public void onSuccess(final File result, int statusCode, Map<String, List<String>> responseHeaders) {
					SwingUtilities.invokeLater(new Runnable() {
						
						public void run() {
							JFileChooser fileChooser = new JFileChooser();
							fileChooser.setDialogTitle("Specify a file to save");   
							 
							int userSelection = fileChooser.showSaveDialog(FileSystemWindow.this);
							if (userSelection == JFileChooser.APPROVE_OPTION) {
							    File fileToSave = fileChooser.getSelectedFile();
							    try {
									FileUtils.moveFile(result, fileToSave);
								} catch (IOException e) {
									DeviceChooser.handleError(e);
								}
							    JOptionPane.showMessageDialog(null, String.format("Successfully downloaded to %s", fileToSave.getAbsolutePath()));
							}
							result.delete();
						}
					});
				}

			});
		} catch (ApiException e) {
			DeviceChooser.handleError(e);
		}
	}


	private void showDirectory(String path) {
		try {
			currentPath = path;
			if (currentPath.endsWith("/") && currentPath.length() != 1)
				currentPath = currentPath.substring(0, currentPath.length() - 1);
			txtPath.setText(currentPath);
			fsAPI.listFilesAsync(device.getID(), path, new OwnApiCallback<List<FileOnDevice>>() {
				
				
				public void onSuccess(List<FileOnDevice> result, int statusCode, Map<String, List<String>> responseHeaders) {
					files = result;
					model.fireTableDataChanged();
				}
			});
		} catch (ApiException e) {
			DeviceChooser.handleError(e);
		}
	}

}
