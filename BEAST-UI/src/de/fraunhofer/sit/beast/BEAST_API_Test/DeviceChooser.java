package de.fraunhofer.sit.beast.BEAST_API_Test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import de.fraunhofer.sit.beast.client.api.AppsApi;
import de.fraunhofer.sit.beast.client.api.DeviceEnvironmentsApi;
import de.fraunhofer.sit.beast.client.api.DevicesApi;
import de.fraunhofer.sit.beast.client.api.FileSystemApi;
import de.fraunhofer.sit.beast.client.api.InputApi;
import de.fraunhofer.sit.beast.client.invoker.ApiClient;
import de.fraunhofer.sit.beast.client.invoker.ApiException;
import de.fraunhofer.sit.beast.client.invoker.Configuration;
import de.fraunhofer.sit.beast.client.invoker.auth.ApiKeyAuth;
import de.fraunhofer.sit.beast.client.models.DeviceInformation;
import de.fraunhofer.sit.beast.client.models.DeviceRequirements;

public class DeviceChooser {

	private JFrame frmDeviceChooser;
	private JTextField txtServer;
	private JTable tblDevices;
	private List<DeviceInformation> devices;
	private DeviceModel deviceModel;
	private DevicePanel devPanel;
	private DevicesApi devAPI;
	private InputApi inputAPI;
	private FileSystemApi fsAPI;
	private AppsApi appsAPI;
	private DeviceEnvironmentsApi envAPI;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DeviceChooser window = new DeviceChooser();
					window.frmDeviceChooser.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	class DeviceModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "ID";
			case 1:
				return "Name";
			case 2:
				return "Reserved by";
			default:
				return null;
			}
		}

		public int getRowCount() {
			return devices.size();
		}

		public Object getValueAt(int row, int col) {
			DeviceInformation d = devices.get(row);
			switch (col) {
			case 0:
				return d.getID();
			case 1:
				return d.getName();
			case 2:
				return d.getReservedBy();
			}
			return null;
		}
		
	}

	/**
	 * Create the application.
	 */
	public DeviceChooser() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDeviceChooser = new JFrame();
		frmDeviceChooser.setTitle("Device Chooser");
		frmDeviceChooser.setBounds(100, 100, 450, 300);
		frmDeviceChooser.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frmDeviceChooser.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblServer = new JLabel("Server:");
		panel.add(lblServer, BorderLayout.WEST);
		
		txtServer = new JTextField();
		panel.add(txtServer, BorderLayout.CENTER);
		txtServer.setColumns(10);
		

		JButton btnUse = new JButton("Use");
		btnUse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				load(txtServer.getText());
			}
		});
		panel.add(btnUse, BorderLayout.EAST);
		SwingUtilities.getRootPane(btnUse).setDefaultButton(btnUse);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.4);
		frmDeviceChooser.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		
		deviceModel = new DeviceModel();
		tblDevices = new JTable(deviceModel);
		tblDevices.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				DeviceInformation d = getCurrentDevice();
				devPanel.setDevice(devAPI, inputAPI, d);
			}
		});
		scrollPane.setViewportView(tblDevices);
		
		devPanel = new DevicePanel();
		btnUse.addKeyListener(devPanel);
		splitPane.addKeyListener((KeyListener) devPanel);
		frmDeviceChooser.addKeyListener((KeyListener) devPanel);
		tblDevices.addKeyListener((KeyListener) devPanel);
		splitPane.setRightComponent(devPanel);
		txtServer.setText("http://pc-sse-handycontroller.sit.fraunhofer.de:5080");
		
		JMenuBar menuBar = new JMenuBar();
		frmDeviceChooser.setJMenuBar(menuBar);
		
		JMenu mnMore = new JMenu("More");
		menuBar.add(mnMore);
		
		JMenuItem mntmExploreFileSystem = new JMenuItem("Explore file system");
		mntmExploreFileSystem.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent arg0) {
				DeviceInformation dev = getCurrentDevice();
				if (dev != null) {
					FileSystemWindow window = new FileSystemWindow(dev, fsAPI);
					window.show(true);
				}
			}
		});
		mnMore.add(mntmExploreFileSystem);
		
		JMenuItem mntmApplications = new JMenuItem("Applications");
		mntmApplications.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent arg0) {
				DeviceInformation dev = getCurrentDevice();
				if (dev != null) {
					ApplicationWindow window = new ApplicationWindow(dev, appsAPI);
					window.show();
				}
			}
		});
		mnMore.add(mntmApplications);
		load("http://pc-sse-handycontroller.sit.fraunhofer.de:5080");
	}

	protected DeviceInformation getCurrentDevice() {
		if (tblDevices.getSelectedRow() == -1)
		{
			return null;
		}
		return devices.get(tblDevices.getSelectedRow());
	}

	protected void load(String url) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(url);

        ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
        APIKey.setApiKey("YOUR API KEY");

        defaultClient.getHttpClient().setReadTimeout(1, TimeUnit.DAYS);
         
        devAPI = new DevicesApi(defaultClient);
        appsAPI = new AppsApi(defaultClient);
        envAPI = new DeviceEnvironmentsApi(defaultClient);
        fsAPI = new FileSystemApi(defaultClient);
        inputAPI = new InputApi(defaultClient);
        
        
		try {
	        List<DeviceInformation> devices = devAPI.getDevices(new DeviceRequirements().minBatteryLevel(0));
	        this.devices = devices;
	        deviceModel.fireTableDataChanged();
		} catch (ApiException e) {
			handleError(e);
		}

	}

	public static void handleError(Exception e) {
		JOptionPane.showMessageDialog(null, e.getMessage());
	}

}
