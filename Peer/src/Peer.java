import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;







public class Peer {
	  //RTSP variables
	  //----------------
	  JFrame f = new JFrame("Peer");
	  JTextField videoname= new JTextField();
	  JButton setupButton = new JButton("Setup");
	  JButton reqButton = new JButton("Request");

	  JPanel mainPanel = new JPanel();
	  JPanel videoPanel= new JPanel();
	  JPanel buttonPanel = new JPanel();
	  JLabel iconLabel = new JLabel();
	  JLabel videoNameLabel= new JLabel();
	  ImageIcon icon;

	  final static int INIT = 0;
	  final static int READY = 1;
	  final static int PLAYING = 2;
	  static int state; //RTSP state == INIT or READY or PLAYING
    static int nextRTPPort = 3000;
    static int nextRTSPPort = 2500;
    BufferedWriter serverOutputBuffer;
    static BufferedReader clientInputBuffer;
    static BufferedWriter clientOutputBuffer;
    Socket serverSocket;
    Socket listeningSocket;
    int servicingPort;
    static String fileNames;
    static String reqfileName;
    static int RTSP_listening_port=0;
    static int RTSP_metaserver_port=0;
   // static ArrayList<String> fileNames;
    
    

    public Peer() throws IOException {

        //build GUI
        //--------------------------
     
        //Frame
        f.addWindowListener(new WindowAdapter() {
           public void windowClosing(WindowEvent e) {
    	 System.exit(0);
           }
        });

        //Buttons
        buttonPanel.setLayout(new GridLayout(1,0));
        buttonPanel.add(setupButton);
        buttonPanel.add(reqButton);
        
      videoNameLabel.setText("Enter video name:");
      videoPanel.setLayout(new GridLayout() );

      videoPanel.add(videoNameLabel);
      videoPanel.add(videoname);
      

        setupButton.addActionListener(new setupButtonListener());
        reqButton.addActionListener(new reqButtonListener());


        //Image display label
        iconLabel.setIcon(null);
        
        //frame layout
        mainPanel.setLayout(null);
        mainPanel.add(iconLabel);
        mainPanel.add(videoPanel);
        mainPanel.add(buttonPanel);

        iconLabel.setBounds(0,0,380,280);
        videoPanel.setBounds(0,100,380,50);
        buttonPanel.setBounds(0,280,380,50);

        f.getContentPane().add(mainPanel, BorderLayout.CENTER);
        f.setSize(new Dimension(390,370));
        f.setVisible(true);

    }
    
    public static void main(String argv[]) throws Exception
    {   	
      //Create a Client object
     Peer thePeer = new Peer();
 
    }
    
    class setupButtonListener implements ActionListener{
    	 Socket serverSocket;
    	 InetAddress ServerIPAddr;
        public void actionPerformed(ActionEvent e){

          //System.out.println("Setup Button pressed !");      


		RTSP_metaserver_port = 3003;//Integer.parseInt(argv[1]);
		RTSP_listening_port = 4000;
		String ServerHost = "143.215.111.239";//argv[0];
		 try {
			ServerIPAddr= InetAddress.getByName(ServerHost);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

//Establish a TCP connection with the server to exchange RTSP messages
//------------------
		try {
			serverSocket = new Socket(ServerIPAddr, RTSP_metaserver_port);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		 System.out.println("In client");
		//Set input and output stream filters:
		try {
			clientInputBuffer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()) );
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			clientOutputBuffer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()) );
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		fileNames="movie.Mjpeg";
		// String allfiles= "";
		//System.out.println("client"+fileNames);
		//init RTSP state:
		state = INIT;

          if (state == INIT) 
    	{
    	  //Init non-blocking RTPsocket that will be used to receive data
    	 
    	  //Send SETUP message to the server
        	   try {
				clientOutputBuffer.write("INIT " + RTSP_listening_port + Constants.CRLF + 1 + Constants.CRLF+fileNames+Constants.CRLF);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	    try {
					clientOutputBuffer.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    	  //Wait for the response 
                //create new thread with server instance
        	    Server listening_server = new Server(4000);
        	    new Thread(listening_server).start();
        	    
    	}//else if state != INIT then do nothing

          
        }
      }
    
    class reqButtonListener implements ActionListener{
   	 Socket serverSocket;
   	 InetAddress ServerIPAddr;
     public void actionPerformed(ActionEvent e){

         //System.out.println("Setup Button pressed !");      

    	 String peerIp = "";
    	 int peerPort = 0;
 		 String ServerHost = "143.215.111.239";//argv[0];
		 try {
			ServerIPAddr= InetAddress.getByName(ServerHost);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

//Establish a TCP connection with the server to exchange RTSP messages
//------------------
		try {
			serverSocket = new Socket(ServerIPAddr, RTSP_metaserver_port);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		 System.out.println("In client");
		//Set input and output stream filters:
		try {
			clientInputBuffer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()) );
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			clientOutputBuffer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()) );
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}


		reqfileName=videoname.getText();
		System.out.println(reqfileName);
		//System.out.println("client"+fileNames);

        	   try {
				clientOutputBuffer.write("REQUEST " + reqfileName+ Constants.CRLF);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	    try {
					clientOutputBuffer.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    	  //Wait for the response 
                //create new thread with server instance
        	   try {
        		System.out.println("message from server after request");
				peerIp = clientInputBuffer.readLine();
				peerIp= peerIp.substring(1);
				peerPort = Integer.parseInt(clientInputBuffer.readLine());
				System.out.println(peerIp);
				System.out.println(peerPort);
				
        	   } 
        	   catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	   
        	Client client = new Client();  
        try {
        	 InetAddress ServerIPAddr = InetAddress.getByName(peerIp);
             client.start_client(peerPort,ServerIPAddr,reqfileName);

        	//	client.start_client(4000,"198.162.1.8","C:\\Users\\Amritha\\Downloads\\movie.Mjpeg");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
        
   	 //Establish a TCP connection with the server to exchange RTSP messages
   	 //------------------
   	 try {
   		 serverSocket = new Socket(ServerIPAddr, RTSP_metaserver_port);
   	 } catch (IOException e2) {
   		 // 	TODO Auto-generated catch block
   		 e2.printStackTrace();
   	 }
   	 System.out.println("In client");
   	 //	Set input and output stream filters:
   	 try {
			clientInputBuffer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()) );
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			clientOutputBuffer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()) );
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	}

          
      }
      
      
}