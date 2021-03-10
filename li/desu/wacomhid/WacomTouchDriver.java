package li.desu.wacomhid;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.util.Arrays;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;

public class WacomTouchDriver {
	
	private static HidDevice tablet = null;
	private static Robot rob = null;
	
	private static int width = 0;
	private static int height = 0;

	public static void main(String[] args) {
		
		try {
			rob = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		HidServicesSpecification hss = new HidServicesSpecification();
		hss.setAutoStart(false);
		HidServices svc = HidManager.getHidServices(hss);
		svc.start();
		
		// Search for the tablet
		for(HidDevice dv : svc.getAttachedHidDevices()) {
			System.out.println(dv);
			
			if(dv.getManufacturer() != null) {
				if(dv.getManufacturer().startsWith("Wacom Co.,Ltd.") && dv.getUsagePage() == 0xffffff00) {
					System.out.println("Tablet found!");
					
					tablet = dv;
					break;
				}
			}
		}
		
		if(tablet != null) {
			calculateScreen();
			while(true) {
				
				// Parse the input of the tablet
				process(tablet);
				
			}
		}else {
			System.out.println("Tablet (Wacom CTH-480) could not be found.");
		}
		
	}
	
	public static void process(HidDevice tablet) {
		
		if(!tablet.isOpen()) {
			System.out.println("Opened tablet");
			tablet.open();
		}
		
		// for debug purposes
		// System.out.println(Arrays.toString(tablet.read(64)));
		
		Byte[] raw = null;
		
		try {
			raw = tablet.read();
			
			if(raw[2] != -127 && raw[2] != -128) { // data is touch position data
				
				// calculate the touch position to absolute mouse position
				int rawPosX = raw[4];
				int rawPosY = raw[5];
				
				if(rawPosX < 0) {
					rawPosX = rawPosX + 256;
				}
				if(rawPosY < 0) {
					rawPosY = rawPosY + 256;
				}
				
				int x = (int) (width / 255.000000000000000 * rawPosX);
				int y = (int) (height / 255.000000000000000 * rawPosY);
				
				System.out.println("POSITION - X : " + rawPosX + " - Y : " + rawPosY + " // x : " + x + " - y : " + y + " // " + Arrays.toString(raw));
				
				// Move the mouse
				updateMouse(x, y);
				
			}
			
		}catch(StackOverflowError err) {err.printStackTrace();}catch(NullPointerException e) {e.printStackTrace();}
	}
	
	// Get the main screen resolution
	public static void calculateScreen() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		width = gd.getDisplayMode().getWidth();
		height = gd.getDisplayMode().getHeight();
		System.out.println("Screen resolution: " + width + "x" + height);
	}
	
	public static void updateMouse(int x, int y) {
		rob.mouseMove(x, y);
	}

}
