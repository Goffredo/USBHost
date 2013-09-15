package src;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import de.ailis.usb4java.libusb.DeviceHandle;
import de.ailis.usb4java.libusb.LibUsb;

public class USBWriter implements Runnable{

	private USBStateListener sL;
	private DeviceHandle sTMhandle;

	public USBWriter(USBStateListener stateListener, DeviceHandle sTMhandle) {
		this.sL = stateListener;
		this.sTMhandle = sTMhandle;
	}

	@Override
	public void run() {
		/*implement a way to fill data in a loop and the write routine is done*/
		while(true/*or some condition is met*/){
			ByteBuffer data = ByteBuffer.allocateDirect(512);
			/*now data should be filled with data to send to usb device*/
			data.order(ByteOrder.LITTLE_ENDIAN);
			IntBuffer transferred = IntBuffer.allocate(1);
			int result = 0;

			result = LibUsb.bulkTransfer(sTMhandle, 0x02, data, transferred, 0);

			if(result!=0){
				sL.usbError(result);
			}
		}
	}


}
