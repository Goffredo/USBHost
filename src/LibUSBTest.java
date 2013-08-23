package src;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbServices;

import de.ailis.usb4java.libusb.Device;
import de.ailis.usb4java.libusb.DeviceDescriptor;
import de.ailis.usb4java.libusb.DeviceHandle;
import de.ailis.usb4java.libusb.DeviceList;
import de.ailis.usb4java.libusb.LibUsb;

public class LibUSBTest implements Runnable {

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
	}
	
	public final AtomicBoolean fermati = new AtomicBoolean(false);

	private Device STM;

	private void retrieveSTM() {

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

	private void printEpReg(ByteBuffer data) {
		//data.getShort();
		String firstShort = (Integer
				.toBinaryString((data.get() & 0xFF) + 0x100)).substring(1);
		String secondShort = (Integer
				.toBinaryString((data.get() & 0xFF) + 0x100)).substring(1);

		String out = "|";
		out += firstShort.substring(0, 1);
		out += "|";
		out += firstShort.substring(1, 2);
		out += "|";
		out += firstShort.substring(2, 4);
		out += "|";
		out += firstShort.substring(4, 5);
		out += "|";
		out += firstShort.substring(5, 7);
		out += "|";
		out += firstShort.substring(7, 8);
		out += "|";
		out += secondShort.substring(0, 1);
		out += "|";
		out += secondShort.substring(1, 2);
		out += "|";
		out += secondShort.substring(2, 4);
		out += "|";
		out += secondShort.substring(4, 8);
		out += "|";
		System.out.println(out);
	}

	@Override
	public void run() {
		while (!fermati.get()) {
			retrieveSTM();

			if (STM != null) {
				DeviceHandle STMhandle = new DeviceHandle();
				LibUsb.open(STM, STMhandle);
				System.out.println(STMhandle);
				ByteBuffer data = ByteBuffer.allocateDirect(512);
				IntBuffer transferred = IntBuffer.allocate(1);
				long time, time2;
				time = System.nanoTime();
				int result = 0;
				while (result == 0 && !fermati.get() ) {
					//System.out.println(i);
					result = LibUsb.bulkTransfer(STMhandle, 0x81, data, transferred, 0);
					System.out.println("Trasnferred bytes: "+ transferred.get());
					/*
					 * printEpReg(data); printEpReg(data); printEpReg(data);
					 */

					data.toString();
					System.out.println(LibUsb.errorName(result));
					transferred.clear();
					data.clear();
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				LibUsb.close(STMhandle);
				
				time2 = System.nanoTime() - time;
				double timeS = time2 / 1000000000d;
				System.out.println(time2);
				System.out.println(8000 / timeS);
				System.out.println(data);
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

}
