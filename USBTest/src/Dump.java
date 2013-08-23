package src;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;
import javax.usb.UsbStringDescriptor;

import de.ailis.usb4java.libusb.DeviceHandle;
import de.ailis.usb4java.libusb.LibUsb;

public class Dump
{
    private static boolean trovata = false;
	private static UsbDevice STM;

	private static void dump(UsbDevice device)
    {
        UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
        try {
			UsbStringDescriptor vendorString = device.getUsbStringDescriptor((byte) 1);
			UsbStringDescriptor prodString = device.getUsbStringDescriptor((byte) 2);
			if(vendorString.getString().equals("STMicroelectronics")&&prodString.getString().equals("STM32 Joystick")){				
				trovata  = true;
				STM = device;
			}
		} catch (UsbDisconnectedException | UsbException e) {
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (device.isUsbHub())
        {
            UsbHub hub = (UsbHub) device;
            for (UsbDevice child : (List<UsbDevice>) hub.getAttachedUsbDevices())
            {
                dump(child);
            }
        }
    }

    public static void main(String[] args) throws UsbException
    {
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub rootHub = services.getRootUsbHub();
        dump(rootHub);
        if(trovata){
        	System.out.println("Trovata STM");
        	STM.getUsbConfiguration((byte) 1).getUsbInterface((byte) 0).claim();
        	UsbEndpoint ep = (UsbEndpoint) STM.getUsbConfiguration((byte) 1).getUsbInterface((byte) 0).getUsbEndpoints().get(0);
        	System.out.println(ep.toString());
        	UsbPipe pipe = ep.getUsbPipe();
        	pipe.open();
        	UsbIrp readIrp = pipe.createUsbIrp();
        	byte[] data = new byte[4096];
        	readIrp.setData(data);
        	readIrp.setAcceptShortPacket(true);
        	long time, time2;
        	time = System.nanoTime();
        	pipe.syncSubmit(readIrp);
        	time2 = System.nanoTime()-time;
        	double timeS = time2/1000000000d;
        	System.out.println(time2);
        	System.out.println(4096/timeS);
        }else{
        	System.out.println("STM non collegata");
        }
    }
}