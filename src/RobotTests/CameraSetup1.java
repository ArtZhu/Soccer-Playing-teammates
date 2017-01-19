package RobotTests;


import java.io.*;
import java.util.*;

import Threads.Camera.*;
import lejos.hardware.*;
import lejos.hardware.device.NXTCam;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.geometry.Rectangle2D;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Waypoint;
import lejos.utility.Delay;
	
public class CameraSetup1 {
		/*******************
		 *  debug
		 */	
			//DEBUG FLAG -------------------------------------------------------------------------------------------------------------
			static boolean Debug = true;
			static boolean PRINT = true;
			static boolean network = true;
			
			//PORT AND IP  -----------------------------------------------------------------------------------------------------------
			static final String IPAddress = "10.200.11.166";
			static final int port = 6983;
			
			//Camera
			static NXTCam camera = new NXTCam(SensorPort.S2);
			static CameraData data1 = new CameraData();
			static int camera_delay = 100;
			
			//FileWriting
			static File outFile = new File("thefilename.txt");
			static FileWriter _fw = null;
			static PrintWriter fw = null;
				
		/*******************
		 * Robot setups
		 */	
			static Brick brick = BrickFinder.getDefault();
			//Robot Statistics -------------------------------------------------------------------------------------------------------
			
			//turning
			static final double turnConst = 0.83;  // 0.86 0.95
				
			static final double robotLength = 15.0;
			static final double wheelDiameter = 5.4;
			static final double wheelCircumference = wheelDiameter * Math.PI;
			static final double robotTrack = 14.5 * turnConst;  //14.5	

				
			//Robot Controller -------------------------------------------------------------------------------------------------------

			//Pilot & PoseProvider & Navigator ---------------------------------------------------------------------------------------
			@SuppressWarnings("deprecation")
			static DifferentialPilot pilot = new DifferentialPilot(wheelDiameter, robotTrack, Motor.A, Motor.B);
			static Navigator nav = new Navigator(pilot);
			static PoseProvider poseProvider = nav.getPoseProvider();
			static int stdSpeed = 15;
			static int stdRotateSpeed = 45;
			static NXTRegulatedMotor arm = Motor.C;
			
		
		public static void main(String[] args){	
			try {
				_fw = new FileWriter(outFile);
				fw = new PrintWriter(_fw);
			} catch (IOException e) {}
			
			

			
			
			Thread t = new Thread(new CameraThread(camera, data1, camera_delay));
			t.setDaemon(true);
			t.start();

			try {
	/*		//Center Test
			centerTest("Center");
			centerTest("Left30");
			centerTest("Left60");
			centerTest("Right30");
			centerTest("Right60");
	*/		
			distanceTest("15cm");
			distanceTest("30cm");
			distanceTest("45cm");
			distanceTest("60cm");
	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			System.exit(0);
		}
		
		private static void distanceTest(String s) throws IOException{
			LCD.clear();
			LCD.drawString("Test " + s, 0, 0);
			Button.waitForAnyPress();
			
			LCD.drawString("Distance " + s, 0, 0);
			List<Double> widthS = new ArrayList<>();
			List<Double> heightS = new ArrayList<>();
			
			for(int i=0; i<10; i++){
				LCD.clear();
				LCD.drawString("Ball " + s + " " + String.valueOf(i), 0, 0);
				
				Button.waitForAnyPress();
				
				Rectangle2D rec = data1.getData();
				while(rec == null){
					Delay.msDelay(camera_delay);
					rec = data1.getData();
				}
				
				widthS.add(rec.getWidth());
				heightS.add(rec.getHeight());
				
				
				Delay.msDelay(camera_delay);
			}
			
			double width = 0; double height = 0;
			for(Double x: widthS)
				width += x;
			for(Double y: heightS)
				height += y;
			width = width / widthS.size();
			height = height / heightS.size();
			
			System.out.println(String.format("Ball at "+ s + " away, width = %f\n", width));
			System.out.println(String.format("Ball at "+ s + " away, height = %f\n", height));
		}
		
		
		private static void centerTest(String s) throws IOException{
			LCD.clear();
			LCD.drawString("Test " + s, 0, 0);
			Button.waitForAnyPress();
			
			LCD.drawString("Ball " + s, 0, 0);
			List<Double> centerXs = new ArrayList<>();
			List<Double> centerYs = new ArrayList<>();
			
			for(int i=0; i<10; i++){
				LCD.clear();
				LCD.drawString("Ball " + s + " " + String.valueOf(i), 0, 0);
				
				Button.waitForAnyPress();
				
				Rectangle2D rec = data1.getData();
				while(rec == null){
					Delay.msDelay(camera_delay);
					rec = data1.getData();
				}
				
				centerXs.add(rec.getCenterX());
				centerXs.add(rec.getCenterX());
				
				
				Delay.msDelay(camera_delay);
			}
			
			double centerX = 0; double centerY = 0;
			for(Double x: centerXs)
				centerX += x;
			for(Double y: centerYs)
				centerY += y;
			centerX = centerX / centerXs.size();
			centerY = centerY / centerYs.size();
			
			System.out.println(String.format(s + "centerX = %f\n", centerX));
			System.out.println(String.format(s + "centerY = %f\n", centerY));
		}

	}
