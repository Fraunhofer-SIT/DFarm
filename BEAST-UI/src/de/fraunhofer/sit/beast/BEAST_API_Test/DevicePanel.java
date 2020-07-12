package de.fraunhofer.sit.beast.BEAST_API_Test;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.Timer;

import de.fraunhofer.sit.beast.client.api.DevicesApi;
import de.fraunhofer.sit.beast.client.api.InputApi;
import de.fraunhofer.sit.beast.client.invoker.ApiException;
import de.fraunhofer.sit.beast.client.models.DeviceInformation;

public class DevicePanel extends JComponent implements KeyListener {

	private DevicesApi devAPI;
	private DeviceInformation device;
	private Image imgDevice;
	private InputApi inputAPI;
	private Rectangle rect;

	/**
	 * Create the panel.
	 */
	public DevicePanel() {
		setFocusable(true);
		Timer tmr = new Timer(100, new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (device != null) {
					try {
						File f = devAPI.getScreenshot(device.getID());
						BufferedImage imgDevice = ImageIO.read(f);

					    byte[] pixels = ((DataBufferByte) imgDevice.getRaster().getDataBuffer()).getData();
					    boolean onlyBlack = true;
					    for (int i : pixels) {
					    	if (i != -1 && i!= 0)
					    	{
					    		onlyBlack = false;
					    		break;
					    	}
					    }
						/*Loop:
						for (int x = 0; x < imgDevice.getWidth(); x += 2) {
							for (int y = 0; y < imgDevice.getHeight(); y += 2) {
								int r = imgDevice.getRGB(x, y);
								if (r != -16777216) {
									onlyBlack = false;
									break Loop;
								}
							}
							
						}*/
					    DevicePanel.this.imgDevice = imgDevice;
						repaint();
						inputAPI.keyTyped(device.getID(), "KEYCODE_WAKEUP");
						f.delete();
					} catch (ApiException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		tmr.start();
		this.addKeyListener(this);
		this.addMouseListener(new MouseListener() {
			Point swipeStart;
			long swipeStartTime;
			
			public void mouseReleased(MouseEvent e) {
				if (swipeStart != null) {
					Point start = calcOnScreen(swipeStart);
					Point stop = calcOnScreen(e.getPoint());
					if (start != null && stop != null) {
						try {
							int durationMs = (int) (System.currentTimeMillis() - swipeStartTime);
							inputAPI.swipe(device.getID(), start.x, start.y, stop.x, stop.y, durationMs);
						} catch (ApiException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
			
			public void mousePressed(MouseEvent e) {
				swipeStart = e.getPoint();
				swipeStartTime = System.currentTimeMillis();
			}
			
			private Point calcOnScreen(Point e) {
				if (rect == null)
					return null;
				int x = (int) e.getX();
				int y = (int) e.getY();
				
				x -= rect.x;
				y -= rect.y;
				int devW = imgDevice.getWidth(null);
				int devH = imgDevice.getHeight(null);
				if (x > rect.width || y > rect.height || x < 0 || y < 0)
					return null;
				x = (int) ((double)x / rect.width * devW);
				y = (int) ((double)y / rect.height * devH);
				return new Point(x, y);
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseClicked(MouseEvent e) {
				Point p = calcOnScreen(e.getPoint());
				if (device != null && p != null)
				{
					try {
						inputAPI.tap(device.getID(), p.x, p.y);
					} catch (ApiException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}
	
	public void setDevice(DevicesApi devAPI, InputApi inputAPI, DeviceInformation device) {
		this.devAPI = devAPI;
		this.inputAPI = inputAPI;
		this.device = device;
		this.imgDevice = null;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (imgDevice != null) 
		{
			rect = ImageUtils.drawImageScaled(imgDevice, this, g);
		}
	}

	public void keyPressed(KeyEvent e) {
		try {
			if (e.getKeyChar() == '\b') {
//				inputAPI.keyTyped(device.getID(), "KEYCODE_BACK");
				inputAPI.keyTyped(device.getID(), "KEYCODE_DEL");
			}
			if (e.getKeyChar() != '\0') {
				String s = String.valueOf(e.getKeyChar());
				if (!s.isEmpty())
					inputAPI.typeText(device.getID(), s);
			}
			System.out.println(e.toString());
			if (device != null)
			{
//				inputAPI.keyTyped(device.getID(), String.valueOf(e.getKeyCode()));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent e) {

	}

}
