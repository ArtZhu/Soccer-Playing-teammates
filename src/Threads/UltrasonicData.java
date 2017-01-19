package Threads;

import lejos.hardware.lcd.LCD;

public class UltrasonicData {
	//protected int distance;
	protected int sonicValue;

	public UltrasonicData() {
		sonicValue = 0;
	}

	public synchronized int getValue() {
		return sonicValue;
	}
	
	public synchronized void setSonicValue(int value) {
		this.sonicValue = value;
	}
}
