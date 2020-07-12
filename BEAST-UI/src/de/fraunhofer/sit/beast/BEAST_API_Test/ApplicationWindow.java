package de.fraunhofer.sit.beast.BEAST_API_Test;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.fraunhofer.sit.beast.client.api.AppsApi;
import de.fraunhofer.sit.beast.client.invoker.ApiException;
import de.fraunhofer.sit.beast.client.models.AbstractApp;
import de.fraunhofer.sit.beast.client.models.DeviceInformation;

public class ApplicationWindow {

	private JFrame frmApplications;
	private DeviceInformation device;
	private AppsApi appsAPI;
	private JTable table;
	protected List<AbstractApp> apps = Collections.emptyList();
	private AppsModel model = new AppsModel();
	

	class AppsModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		public int getColumnCount() {
			return 1;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Name";
			default:
				return null;
			}
		}

		public int getRowCount() {
			return apps.size();
		}

		public Object getValueAt(int row, int col) {
			AbstractApp d = apps.get(row);
			switch (col) {
			case 0:
				return d.getId();
			}
			return null;
		}
		
	}
	
	/**
	 * Create the application.
	 * @param appsAPI 
	 * @param dev 
	 */
	public ApplicationWindow(DeviceInformation dev, AppsApi appsAPI) {
		this.device = dev;
		this.appsAPI = appsAPI;
		initialize();
		refresh();
	}

	private void refresh() {
		try {
			appsAPI.getInstalledAppsAsync(device.getID(), new OwnApiCallback<List<AbstractApp>>() {

				public void onSuccess(List<AbstractApp> result, int statusCode, Map<String, List<String>> responseHeaders) {
					apps = result;
					model.fireTableDataChanged();
				}
				
			});
		} catch (ApiException e) {
			DeviceChooser.handleError(e);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmApplications = new JFrame();
		frmApplications.setTitle("Applications of " + device.getName());
		frmApplications.setBounds(100, 100, 450, 300);
		frmApplications.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frmApplications.getContentPane().add(panel, BorderLayout.EAST);
		
		JButton btnInstallApp = new JButton("Install app...");
		btnInstallApp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Specify a app to install");   
				fileChooser.setMultiSelectionEnabled(true);
				int userSelection = fileChooser.showOpenDialog(frmApplications);
				if (userSelection == JFileChooser.APPROVE_OPTION) {
					final ProgressWindow window = new ProgressWindow();
					try {
						appsAPI.installApplicationAsync(device.getID(), fileChooser.getSelectedFile(), new OwnApiCallback<AbstractApp>() {

							public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
								window.showProgress(String.format("Installing app: %s", fileChooser.getSelectedFile().getName()), bytesWritten, contentLength);
							}
							
							public void onSuccess(AbstractApp result, int statusCode,
									Map<String, List<String>> responseHeaders) {
								window.dispose();
							}
						});
					} catch (ApiException e1) {
						DeviceChooser.handleError(e1);
					}
				}
			}
		});
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(btnInstallApp);
		
		JButton btnUninstall = new JButton("Uninstall");
		btnUninstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(null, "No app selected");
					return;
				}
				AbstractApp app = apps.get(table.getSelectedRow());
				try {
					appsAPI.uninstallApplication(device.getID(), app.getId());
				} catch (ApiException e1) {
					DeviceChooser.handleError(e1);
				}
			}
		});
		panel.add(btnUninstall);
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		panel.add(btnRefresh);
		
		JScrollPane scrollPane = new JScrollPane();
		frmApplications.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable(model);
		scrollPane.setViewportView(table);
	}

	@SuppressWarnings("deprecation")
	public void show() {
		frmApplications.show(true);
	}

}
