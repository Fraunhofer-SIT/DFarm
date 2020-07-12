package de.fraunhofer.sit.beast.BEAST_API_Test;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;

import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ProgressWindow {

	private JFrame frmOperationInProgress;
	private JProgressBar progressBar;
	protected boolean cancelled;
	private JLabel lblProgress;

	/**
	 * Create the application.
	 */
	public ProgressWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmOperationInProgress = new JFrame();
		frmOperationInProgress.setTitle("Operation in progress");
		frmOperationInProgress.setBounds(100, 100, 450, 300);
		frmOperationInProgress.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {115};
		gridBagLayout.rowHeights = new int[]{14, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		frmOperationInProgress.getContentPane().setLayout(gridBagLayout);
		
		lblProgress = new JLabel("Please wait...");
		GridBagConstraints gbc_lblLblprogress = new GridBagConstraints();
		gbc_lblLblprogress.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblLblprogress.insets = new Insets(0, 0, 5, 0);
		gbc_lblLblprogress.gridx = 0;
		gbc_lblLblprogress.gridy = 0;
		frmOperationInProgress.getContentPane().add(lblProgress, gbc_lblLblprogress);
		
		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.anchor = GridBagConstraints.NORTH;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 1;
		frmOperationInProgress.getContentPane().add(progressBar, gbc_progressBar);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.EAST;
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 2;
		frmOperationInProgress.getContentPane().add(btnCancel, gbc_btnCancel);
	}

	@SuppressWarnings("deprecation")
	public void show() {
		frmOperationInProgress.show(true);
	}

	public void dispose() {
		frmOperationInProgress.dispose();
	}

	public void showProgress(String text, long bytesRead, long contentLength) {
		frmOperationInProgress.setTitle(text);
		progressBar.setMaximum((int) (contentLength / 1024));
		progressBar.setValue((int) (bytesRead / 1024));
		lblProgress.setText(String.format("In progress: %d %% (%s of %s)", (int)(100D / contentLength * bytesRead), FileUtils.byteCountToDisplaySize(bytesRead), FileUtils.byteCountToDisplaySize(contentLength)));
	}

}
