package src;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbServices;

import de.ailis.usb4java.libusb.Device;
import de.ailis.usb4java.libusb.DeviceDescriptor;
import de.ailis.usb4java.libusb.DeviceHandle;
import de.ailis.usb4java.libusb.DeviceList;
import de.ailis.usb4java.libusb.LibUsb;

public class LibUSBTest implements Runnable, USBStateListener {

	public static void main(String[] args) {
		LibUSBTest test = new LibUSBTest();

		//start USB
		new Thread(test).start();

		//wait 20 sec
		try {
			Thread.sleep(20000); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//close it
		test.fermati.set(true);
		System.out.println("Chiudo programma");
	}

	public final AtomicBoolean fermati = new AtomicBoolean(false);

	private Device STM;

	AtomicInteger[] valore = new AtomicInteger[3];

	private USBLIstener listener;

	private USBReader reader;

	private USBWriter writer;

	private boolean everythingOK = false;

	public LibUSBTest(){
		for(int i = 0; i<valore.length; i++){
			valore[i] = new AtomicInteger();
		}
	}

	private void retrieveSTM() {

		STM = null;

		try {
			// do not remove, hidden inizialization here!
			UsbServices services = UsbHostManager.getUsbServices();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DeviceList list = new DeviceList();
		LibUsb.getDeviceList(null, list);
		for (Device d : list) {
			DeviceDescriptor descriptor = new DeviceDescriptor();
			LibUsb.getDeviceDescriptor(d, descriptor);
			// System.out.println(descriptor.dump());
			if (descriptor.idProduct() == 0x5710
					&& descriptor.idVendor() == 0x0483) {
				System.out.println("Found ");
				System.out.println(descriptor.dump());
				STM = d;
			}
		}

	}

	@Override
	public void run() {
		while (!fermati.get()) {
			retrieveSTM();
			if (STM != null) {
				DeviceHandle STMhandle = new DeviceHandle();
				System.out.println(LibUsb.errorName(LibUsb.open(STM, STMhandle)));
				System.out.println(STMhandle);
				everythingOK = true;
				
				reader = new USBReader(this, STMhandle, listener);
				writer = new USBWriter(this, STMhandle);
				
				new Thread(reader).start();
				new Thread(writer).start();
				
				while(everythingOK){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				LibUsb.close(STMhandle);
				
			} else {
				System.out.println("STM non trovata");
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int get(int i){
		return valore[i].get();
	}

	public void setListener(USBLIstener usbReader) {
		this.listener = usbReader;
	}

	@Override
	public synchronized void usbError(int errorType) {
		everythingOK = false;
		reader.stop();
		//writer.stop();
	}

}
