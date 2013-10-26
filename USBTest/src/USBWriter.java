package src;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.ailis.usb4java.libusb.DeviceHandle;
import de.ailis.usb4java.libusb.LibUsb;

public class USBWriter implements Runnable{

	private USBStateListener sL;
	private DeviceHandle sTMhandle;
	private ByteBuffer data = ByteBuffer.allocateDirect(5*4);
	private IntBuffer transferred = IntBuffer.allocate(1);
	
	JTextField t1 = new JTextField("5");
	JTextField t2 = new JTextField("0");;
	JTextField t3 = new JTextField("0");;
	JTextField t4 = new JTextField("1000");
	private boolean funziona = true;
	private JFrame f;
	
	public USBWriter(USBStateListener stateListener, DeviceHandle sTMhandle) {
		data.order(ByteOrder.LITTLE_ENDIAN);
		this.sL = stateListener;
		this.sTMhandle = sTMhandle;
		SwingUtilities.invokeLater(new Runnable() {			

			@Override
			public void run() {
				f = new JFrame();
				JButton b1 = new JButton();
				JButton b2 = new JButton();
				JButton b3 = new JButton();
				JButton b4 = new JButton();
				f.setLayout(new GridLayout(4,1));
				f.add(t1);
				f.add(b1);
				f.add(t2);
				f.add(b2);
				f.add(t3);
				f.add(b3);
				f.add(t4);
				f.add(b4);
				
				b1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						try{
							float p = Float.parseFloat(t1.getText());
							setValue('P', p);
						} catch (NumberFormatException  e){
							System.err.println("ERRRR");
						}
					}
				});
				b2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						try{
							float p = Float.parseFloat(t2.getText());
							setValue('I', p);
						} catch (NumberFormatException  e){
							System.err.println("ERRRR");
						}
					}
				});
				b3.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						try{
							float p = Float.parseFloat(t3.getText());
							setValue('D', p);
						} catch (NumberFormatException  e){
							System.err.println("ERRRR");
						}
					}
				});
				b4.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						try{
							float p = Float.parseFloat(t4.getText());
							setValue('E', p);
						} catch (NumberFormatException  e){
							System.err.println("ERRRR");
						}
					}
				});
				f.pack();
				f.setVisible(true);
			}
		});
	}

	public void setValue(char indice, float valore){
		synchronized (data) {
			data.clear();
			data.put((byte)indice);
			data.putFloat(valore);
			System.err.println("Stampato");
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(funziona){
			int result = 0;
			synchronized (data) {
				result = LibUsb.bulkTransfer(sTMhandle, 0x02, data, transferred, 0);				
			}
			System.err.println("Bytes written: "+transferred.get());
			transferred.clear();
			if(result!=0){
				System.err.println("Error writing: "+LibUsb.errorName(result));
				sL.usbError(result);
			}
		}
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				f.dispose();
			}
		});
	}

	public void stop() {
		funziona = false;
	}

}
