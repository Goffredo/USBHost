package src;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import de.ailis.usb4java.libusb.DeviceHandle;
import de.ailis.usb4java.libusb.LibUsb;

public class USBReader implements Runnable{

	private USBStateListener sL;
	private boolean fermati = false;
	private DeviceHandle sTMhandle;
	private USBLIstener listener;

	public USBReader(USBStateListener sL, DeviceHandle sTMhandle, USBLIstener listener) {
		this.sL = sL;
		this.sTMhandle = sTMhandle;
		this.listener = listener;
	}

	public void stop() {
		fermati = true;
	}
	
	@Override
	public void run() {
		ByteBuffer data = ByteBuffer.allocateDirect(512);
		data.order(ByteOrder.LITTLE_ENDIAN);
		IntBuffer transferred = IntBuffer.allocate(1);
		int result = 0;
		int lastSeq = -1;
		int[] packetNumber = new int[6];
		long lastTime = System.nanoTime();
		int diff=0;
		
		while (result == 0 && !fermati  ) {
			//System.out.println(i);
			if(System.nanoTime()-lastTime>=1000000000 && listener != null){
				System.out.print("Numero pacchetti:");
				for(int j = 0; j<packetNumber.length; j++){
					System.out.print(" "+packetNumber[j]);
					packetNumber[j] = 0;
				}
				System.out.println();

				System.out.println("differenti "+diff);
				diff = 0;
				lastTime = System.nanoTime();

			}
			result = LibUsb.bulkTransfer(sTMhandle, 0x81, data, transferred, 0);
			
			if(result!=0){
				fermati = true;
				sL.usbError(result);
				break;
			}
			
			int transferredBytes = transferred.get();
			//System.out.println("Trasnferred bytes: "+ transferredBytes);
			//System.out.println(LibUsb.errorName(result));
			while(data.position()<transferredBytes){
				int currentSeq = data.getShort() & 0xFFFF;
				int packetType = data.getShort() & 0xFFFF;
				
				if (packetType == 3){//if STRING
					System.out.print("Letto da USB: ");
					
					byte c;
					while ( (c = data.get())!='\0' && data.hasRemaining())
						System.out.print((char)c);
					
					System.out.println();
					
				}
				
				if (packetType == 4){//if DCM
					float q[] = new float[4];
					q[0] = data.getFloat();
					q[1] = data.getFloat();
					q[2] = data.getFloat();
					q[3] = data.getFloat();
					if (listener!=null)
						listener.setDCM(q);
					
				}
				
				if (packetType == 5){//if ANGLE
					float ypr[] = new float[3];
					ypr[0] = data.getFloat();
					ypr[1] = data.getFloat();
					ypr[2] = data.getFloat();
					if (listener!=null)
						listener.setEulerianBypass(ypr);
					
				}
				
				if (listener!=null  && packetType >= 0 && packetType <= 2){
					short value1 = data.getShort();
					short value2 = data.getShort();
					short value3 = data.getShort();

				
					switch (packetType) {
					case 0:		
						listener.setRawGyroscope(value1, value2, value3);
						break;
					case 1:		
						listener.setRawAccelerometer(value1, value2, value3);
						break;
					case 2:		
						listener.setRawMagnetometer(value1, value2, value3);
						break;
					default:
						break;
					}
				}

				if(lastSeq!=-1){
					if(currentSeq-lastSeq!=1 && !(lastSeq == 65535 && currentSeq == 0)){
						System.err.println("Incorrect sequence number. Last: "+lastSeq+" Current: "+currentSeq);
						fermati = true;
						sL.usbError(1);
						break;
					}
				}
				packetNumber[packetType]++;
				lastSeq = currentSeq;
			}

			/*
			 * printEpReg(data); printEpReg(data); printEpReg(data);
			 */

			//data.toString();
			transferred.clear();
			data.clear();
		}		
	}

}
