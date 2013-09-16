package src;

public interface USBLIstener {
	public void setRawAccelerometer(short x, short y, short z);
	public void setRawMagnetometer(short x, short y, short z);
	public void setRawGyroscope(short x, short y, short z);
	public void setDCM(float[] q);
	public void setEulerianBypass(float[] ypr);
	public void setPWM(long pwm);

}
