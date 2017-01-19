package Network.ClientServerMgr_client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.navigation.Pose;
import lejos.utility.Delay;
import Network.Util.Message;
import Network.Util.Res;
import Network.Util.Tools;
import RobotMain.Param;
import RobotMain.SP2_v2;
import Threads.RobotHeadingThread_v2;

public class ListenThread_client implements Runnable{
	public static Integer negotiating_size = -1;
	
	private static boolean opponent_arrived = false;
	public synchronized static void setOpponentArrived() { 
		opponent_arrived = true; 
		if(i_arrived){
			Sound.beep();
			while(SP2_v2.getState() != SP2_v2.STATE_waiting) {}
			Sound.beep();
			passTo(_info);
			Delay.msDelay(20000);
			System.exit(0);
		}
	}
	public synchronized static boolean getOpponentArrived() { return opponent_arrived; }
	private static boolean i_arrived = false;
	public synchronized static void setIArrived() {
		i_arrived = true;
		if(opponent_arrived){
			Sound.beep();
			while(SP2_v2.getState() != SP2_v2.STATE_waiting) {}
			Sound.beep();
			passTo(_info);
			Delay.msDelay(20000);
			System.exit(0);
		}
	}
	public synchronized static boolean getIArrived() { return i_arrived; }
	
	private static String _info = "";
	
	//public static Thread confirmation_thread = null;

	private static List<InetAddress>ack_count = new ArrayList<InetAddress>();
	@Override
	public void run() {
		String receive;
		String request;
		String addr_string, port_string;
		
		
			
		
		while(true){
			Object o = SP2_v2.network_mgr.receive();
			if(o == null)
				continue;
			
			Message r = (Message) o;
			receive = r.message;	
			
			request = Tools.takeBefore(receive);
			final String info = Tools.takeAfter(receive);
			
			//ignore all messages that are not sent to me.
			if(!(r.dest_addr == null || SP2_v2.local_addr.equals(r.dest_addr)))
				continue;
				
			switch(request)
			{
			//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//
			case Res.request_to_go_string:
				synchronized(negotiating_size){
					try {
						// if my size is larger than received size, I'm requesting
						if(negotiating_size > Integer.parseInt(info)){/*
							SP2_v2.network_mgr.send(
									Res.request_to_go_string + Res.separator + negotiating_size,
									InetAddress.getByName(SP2_v2.server_addr), SP2_v2.server_port);
						*/}
						// otherwise I let the other guy go
						else{
							//SP2_v2.setState(SP2_v2.stop);
							
							if(!SP2_v2.local_addr.equals(r.source_addr)){
								LCD.drawString("sent ack", 0, 4);
								SP2_v2.network_mgr.send(
										new Message(
												SP2_v2.local_addr, SP2_v2.local_port,
												r.source_addr, r.source_port,
										
										Res.ack_to_go_string + Res.separator
										),
										InetAddress.getByName(SP2_v2.server_addr), SP2_v2.server_port);
								SP2_v2.setState(SP2_v2.STATE_moving);
								LCD.drawString("sent ack", 0, 4);
							}
						}	
					} catch (UnknownHostException e) {
						e.printStackTrace();
						//SP2_v2.pilot.stop();
					}				
				}
				break;
				
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//				
			case Res.shooter_confirm_string:
				LCD.clear(4);
				LCD.drawString("I'm shooter", 0, 4);
				final float goX = SP2_v2.poseProvider.getPose().getX();
				final float goY = (SP2_v2.poseProvider.getPose().getY() + Param.courtY)/2;
				
				SP2_v2.nav.goTo(goX, goY);
				
				//Thread to remember informing the passer that I arrive at position;
				new Thread(new Runnable(){
					@Override
					public void run() {
						Pose p = SP2_v2.poseProvider.getPose();
						while(Math.abs(p.getX() - goX) + Math.abs(p.getY() - goY)
								> Param.tolerant_difference){
							p = SP2_v2.poseProvider.getPose();
							Delay.msDelay(20);
						}
						try {
							SP2_v2.network_mgr.send(
									new Message(
											SP2_v2.local_addr, SP2_v2.local_port,
											null, -1,
									Res.shooter_arrival_string + Res.separator
									+ p.getX() + Res.separator + p.getY()
									),
									InetAddress.getByName(SP2_v2.server_addr), SP2_v2.server_port);
							//RobotHeadingThread.SHOOTING = true;

						} catch (UnknownHostException e) {
							e.printStackTrace();
						}
						
					}
				}).start();
				
				
				//
				break;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
			//If others let me go.
			// "from_whom_id" + ":" + "ack" + ":" + "please_go_id"	
			case Res.ack_to_go_string:
				///////////////////////////////////////////////////////////////////////////////////////////////////
				// DEAL WITH info!
				// then 
				// ack_count ++;
				LCD.drawString("received ack" ,0, 4);
				LCD.drawString(String.format("ackcount = %d", ack_count.size()), 0, 5);
				LCD.drawString(String.format("ackcount include this ack" + ack_count.contains(r.source_addr)), 0, 6);
				if(!ack_count.contains(r.source_addr))
					ack_count.add(r.source_addr);
				LCD.drawString(String.format("ackcount = %d", ack_count.size()), 0, 5);
				//
				//if(!info.equals(SP2_v2.net_id.toString()))
				//	ack_count.clear();
				
				//can never be bigger than yourself
				if(ack_count.size() == Param.team_size - 1){
					// should start negotiating phase, but trivial for now.
					try {
						SP2_v2.network_mgr.send(new Message(
												SP2_v2.local_addr, SP2_v2.local_port,
												 r.source_addr, r.source_port,
										Res.shooter_confirm_string + Res.separator
										),
										InetAddress.getByName(SP2_v2.server_addr), SP2_v2.server_port);
						LCD.clear(6);
						LCD.drawString("sent shooter_confirm!", 0, 6);
					} catch (UnknownHostException e) {
						LCD.clear(6);
						LCD.drawString("shooter_confirm not sent!", 0, 6);
					}
					
					SP2_v2.setState(SP2_v2.STATE_tracking);
					ack_count = new ArrayList<InetAddress>();
				}
				break;
			//Communication for shooting 
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Res.shooter_arrival_string + Res.separator + "xpos"+ Res.separator + ypos"
			case Res.shooter_arrival_string:
				_info = info;
				setOpponentArrived();
				break;

			case Res.request_shooter_facing_string:

				float x = Float.parseFloat(Tools.takeBefore(info));
				float y = Float.parseFloat(Tools.takeAfter(info));
				
				SP2_v2.face(x, y);
				
				//TrackThreadFactory.start();
				//SP2_v2.setState(SP2_v2.STATE_tracking);
				SP2_v2.setShooter();
				break;
			}
			
			try {
				Thread.sleep(SP2_v2.camera_delay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
		}
	
	}
	
	private static void passTo(String msg){
		Pose p = SP2_v2.poseProvider.getPose();
		try {
			SP2_v2.network_mgr.send(
					new Message(
							SP2_v2.local_addr, SP2_v2.local_port,
							null, -1,
	
					Res.request_shooter_facing_string + Res.separator +
					p.getX() + Res.separator + p.getY()
					),
					InetAddress.getByName(SP2_v2.server_addr), SP2_v2.server_port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		float xpos = Float.parseFloat(Tools.takeBefore(msg));
		float ypos = Float.parseFloat(Tools.takeAfter(msg));
		
		SP2_v2.face(xpos, ypos);
		SP2_v2.kick();
		//SP2_v2.setState(SP2_v2.STATE_passing);
	}

}
