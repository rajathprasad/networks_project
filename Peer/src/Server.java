/*Server.java*/

/* ------------------
   Server
   usage: java Server [RTSP listening port]
   ---------------------- */


import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.Timer;

public class Server extends JFrame implements ActionListener, Runnable {

  //RTP variables:
  //----------------
  DatagramSocket RTPsocket; //socket to be used to send and receive UDP packets
  DatagramPacket senddp; //UDP packet containing the video frames

  InetAddress ClientIPAddr; //Client IP address
  int RTP_dest_port = 0; //destination port for RTP packets  (given by the RTSP Client)

  //GUI:
  //----------------
  JLabel label;

  //Video variables:
  //----------------
  int imagenb = 0; //image nb of the image currently transmitted
  VideoStream video; //VideoStream object used to access video frames
  static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
  static int FRAME_PERIOD = 100; //Frame period of the video to stream, in ms
  static int VIDEO_LENGTH = 250; //length of the video in frames
  static int flag=1;//i=video, 0=buffer

  static int RTSPport;
  
  Timer timer; //timer used to send the images at the video frame rate
  byte[] buf; //buffer used to store the images to send to the client 
  public static ArrayList<RTPpacket> RTPPacketBufferServer= new ArrayList<RTPpacket>();

  //RTSP variables
  //----------------
  //rtsp states
  final static int INIT = 0;
  final static int READY = 1;
  final static int PLAYING = 2;
  //rtsp message types
  final static int SETUP = 3;
  final static int PLAY = 4;
  final static int PAUSE = 5;
  final static int TEARDOWN = 6;

  static ServerSocket listenSocket;
  static int state; //RTSP Server state == INIT or READY or PLAY
  Socket RTSPsocket; //socket used to send/receive RTSP messages
  //input and output stream filters
  static BufferedReader RTSPBufferedReader;
  static BufferedWriter RTSPBufferedWriter;
  static String VideoFileName; //video file requested from the client
  static int RTSP_ID = 123456; //ID of the RTSP session
  int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
  
  final static String CRLF = "\r\n";

  static BufferedReader metaInputBuffer;
  static BufferedWriter metaOutputBuffer;
  static Socket serverSocket;
  InetAddress ServerIPAddr;
  
  
  //--------------------------------
  //Constructor
  //--------------------------------
  public Server(int port){
	  
    //init Frame
    super("Server");
    RTSPport = port;
    //init Timer
    timer = new Timer(FRAME_PERIOD, this);
    timer.setInitialDelay(0);
    timer.setCoalesce(true);

    //allocate memory for the sending buffer
    buf = new byte[15000]; 

    //Handler to close the main window
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	//stop the timer and exit
	timer.stop();
	System.exit(0);
      }});

    //GUI:
    label = new JLabel("Send frame #        ", JLabel.CENTER);
    getContentPane().add(label, BorderLayout.CENTER);
  }
          
  //------------------------------------
  //main
  //------------------------------------
  public void run() 
  {
	  while(true)
	  {
    //create a Server object
   System.out.println("yay in new thread!");
    //show GUI:
    pack();
    setVisible(true);

    //get RTSP socket port from the command line
    
   
    //Initiate TCP connection with the client for the RTSP session
    
	try {
		listenSocket = new ServerSocket(RTSPport);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    try {
		RTSPsocket = listenSocket.accept();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    try {
		listenSocket.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    //Get Client IP address
    ClientIPAddr = RTSPsocket.getInetAddress();

    //Initiate RTSPstate
    state = INIT;

    //Set input and output stream filters:
    try {
		RTSPBufferedReader = new BufferedReader(new InputStreamReader(RTSPsocket.getInputStream()) );
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    try {
		RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(RTSPsocket.getOutputStream()) );
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    //Wait for the SETUP message from the client
    int request_type;
    boolean done = false;
    while(!done)
      {
	request_type = parse_RTSP_request(); //blocking
	
	if (request_type == SETUP)
	  {
	    done = true;

	    //update RTSP state
	    state = READY;
	    System.out.println("New RTSP state: READY");
   
	    //Send response
	    send_RTSP_response();
   
	    //init the VideoStream object:
	    try {
			video = new VideoStream(VideoFileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    //init RTP socket
	    try {
			RTPsocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
      }

     //loop to handle RTSP requests
    while(true)
      {
	//parse the request
	request_type = parse_RTSP_request(); //blocking
	    
	if ((request_type == PLAY) && (state == READY))
	  {
	    //send back response
	    send_RTSP_response();
	    //start timer
	    timer.start();
	    //update state
	    state = PLAYING;
	    System.out.println("New RTSP state: PLAYING");
	  }
	else if ((request_type == PAUSE) && (state == PLAYING))
	  {
	    //send back response
	    send_RTSP_response();
	    //stop timer
	    timer.stop();
	    //update state
	    state = READY;
	    System.out.println("New RTSP state: READY");
	  }
	else if (request_type == TEARDOWN)
	  {
	    //send back response
	    send_RTSP_response();
	    //stop timer
	    timer.stop();
	    //close sockets
	    try {
			RTSPsocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    RTPsocket.close();
	    break;

	   // System.exit(0);
	   }
      }
	}
  }


  //------------------------
  //Handler for timer
  //------------------------
  public void actionPerformed(ActionEvent e) {
	  RTPpacket rtp_packet;
	  	    
    //if the current image nb is less than the length of the video
    if (imagenb < VIDEO_LENGTH && state == PLAYING)
      {
	//update current imagenb
	imagenb++;
	       
	try {
	if(flag==0 && imagenb<buf.length){//serving from buffer
			
	   rtp_packet=RTPPacketBufferServer.get(imagenb);
	  //get next frame to send from the video, as well as its size
	  
		
		}
		
	else {//serving from file
		
			
			  int image_length = video.getnextframe(buf);

			  //Builds an RTPpacket object containing the frame
			  rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb, imagenb*FRAME_PERIOD, buf, image_length);
		}
	  //get to total length of the full rtp packet to send
	  int packet_length = rtp_packet.getlength();

	  //retrieve the packet bitstream and store it in an array of bytes
	  byte[] packet_bits = new byte[packet_length];
	  rtp_packet.getpacket(packet_bits);

	  //send the packet as a DatagramPacket over the UDP socket 
	  senddp = new DatagramPacket(packet_bits, packet_length, ClientIPAddr, RTP_dest_port);
	  RTPsocket.send(senddp);

	  //System.out.println("Send frame #"+imagenb);
	  //print the header bitstream
	  rtp_packet.printheader();

	  //update GUI
	  label.setText("Send frame #" + imagenb);
		
	}
	catch(Exception ex)
	  {
	  //  System.out.println("Exception caught 1: "+ex);
	    //System.exit(0);
	  }
      }
    else
      {
	//if we have reached the end of the video file, stop the timer
	timer.stop();
	// send complete message to metadata server
	
	
		 String ServerHost = "192.168.1.12";//argv[0];
		 try {
			ServerIPAddr= InetAddress.getByName(ServerHost);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

//Establish a TCP connection with the server to exchange RTSP messages
//------------------
		try {
			serverSocket = new Socket(ServerIPAddr, 3003);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		 System.out.println("In complete");
		//Set input and output stream filters:
		
		try {
			metaOutputBuffer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()) );
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}


		//System.out.println("client"+fileNames);

       	   try {
				metaOutputBuffer.write("COMPLETE " + VideoFileName +  Constants.CRLF);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
       	    try {
					metaOutputBuffer.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
       	    System.out.println("Sent Complete message to metadataserver");
	
	
	
       	    imagenb=0;
      }
  }

  //------------------------------------
  //Parse RTSP Request
  //------------------------------------
  private int parse_RTSP_request()
  {
    int request_type = -1;
    try{
      //parse request line and extract the request_type:
      String RequestLine = RTSPBufferedReader.readLine();
      //System.out.println("RTSP Server - Received from Client:");
      System.out.println(RequestLine);

      StringTokenizer tokens = new StringTokenizer(RequestLine);
      String request_type_string = tokens.nextToken();

      //convert to request_type structure:
      if ((new String(request_type_string)).compareTo("SETUP") == 0)
	request_type = SETUP;
      else if ((new String(request_type_string)).compareTo("PLAY") == 0)
	request_type = PLAY;
      else if ((new String(request_type_string)).compareTo("PAUSE") == 0)
	request_type = PAUSE;
      else if ((new String(request_type_string)).compareTo("TEARDOWN") == 0)
	request_type = TEARDOWN;

      if (request_type == SETUP)
	{
	  //extract VideoFileName from RequestLine
	  VideoFileName = tokens.nextToken();
	}

      //parse the SeqNumLine and extract CSeq field
      String SeqNumLine = RTSPBufferedReader.readLine();
      System.out.println(SeqNumLine);
      tokens = new StringTokenizer(SeqNumLine);
      tokens.nextToken();
      RTSPSeqNb = Integer.parseInt(tokens.nextToken());
	
      //get LastLine
      String LastLine = RTSPBufferedReader.readLine();
      System.out.println(LastLine);

      if (request_type == SETUP)
	{
	  //extract RTP_dest_port from LastLine
	  tokens = new StringTokenizer(LastLine);
	  for (int i=0; i<3; i++)
	    tokens.nextToken(); //skip unused stuff
	  RTP_dest_port = Integer.parseInt(tokens.nextToken());
	}
      //else LastLine will be the SessionId line ... do not check for now.
    }
    catch(Exception ex)
      {
	//System.out.println("Exception caught 2: "+ex);
	//System.exit(0);
      }
    return(request_type);
  }

  //------------------------------------
  //Send RTSP Response
  //------------------------------------
  private void send_RTSP_response()
  {
    try{
      RTSPBufferedWriter.write("RTSP/1.0 200 OK"+CRLF);
      RTSPBufferedWriter.write("CSeq: "+RTSPSeqNb+CRLF);
      RTSPBufferedWriter.write("Session: "+RTSP_ID+CRLF);
      RTSPBufferedWriter.flush();
      //System.out.println("RTSP Server - Sent response to Client.");
    }
    catch(Exception ex)
      {
	System.out.println("Exception caught 3: "+ex);
	//System.exit(0);
      }
  }
}
