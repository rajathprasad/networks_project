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
import javax.swing.Timer;







public class Peer {
	  //RTSP variables
	  //----------------
	 JFrame f = new JFrame("Client");
	  JButton setupButton = new JButton("Setup");
	  JButton reqButton = new JButton("Request");
	  JButton pauseButton = new JButton("Pause");
	  JButton tearButton = new JButton("Teardown");
	  JPanel mainPanel = new JPanel();
	  JPanel buttonPanel = new JPanel();
	  JLabel iconLabel = new JLabel();
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
    static int RTSP_server_port=0;
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

        setupButton.addActionListener(new setupButtonListener());
        reqButton.addActionListener(new reqButtonListener());


        //Image display label
        iconLabel.setIcon(null);
        
        //frame layout
        mainPanel.setLayout(null);
        mainPanel.add(iconLabel);
        mainPanel.add(buttonPanel);
        iconLabel.setBounds(0,0,380,280);
        buttonPanel.setBounds(0,280,380,50);

        f.getContentPane().add(mainPanel, BorderLayout.CENTER);
        f.setSize(new Dimension(390,370));
        f.setVisible(true);

 
//        this.servicingPort = centralPort;
//        this.serverSocket = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
//        this.serverOutputBuffer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));

    }
//    public void initialize(List<String> fileNames) throws IOException {
//        serverOutputBuffer.write("INIT " + servicingPort + Constants.CRLF);
//        serverOutputBuffer.write(fileNames.size() + Constants.CRLF);
//        for (String fileName : fileNames) {
//            serverOutputBuffer.write(fileName + Constants.CRLF);
//        }
//        serverOutputBuffer.flush();
//    }
//    public void listenForRequests() throws IOException {
//        while(true){
//            ServerSocket listenSocket = new ServerSocket(servicingPort);
//            this.listeningSocket = listenSocket.accept();
//            clientInputBuffer = new BufferedReader(new InputStreamReader(listeningSocket.getInputStream()));
//            String fileName = clientInputBuffer.readLine();
//            VideoServer videoServer = new VideoServer(fileName, nextRTSPPort, nextRTPPort);
//            //create separate thread to service the request
//            clientOutputBuffer.write("RTSP:" + nextRTSPPort);
//            clientOutputBuffer.write("RTP:" + nextRTPPort);
//            nextRTPPort++;
//            nextRTSPPort++;
//            this.listeningSocket.close();
//        }
//    }
    
    public static void main(String argv[]) throws Exception
    {
    	
      //Create a Client object
     Peer thePeer = new Peer();
  //   fileNames= new ArrayList<String>();
      
      //get server RTSP port and IP address from the command line
      //------------------
//      RTSP_server_port = 3003;//Integer.parseInt(argv[1]);
//      String ServerHost = "localhost";//argv[0];
//      InetAddress ServerIPAddr = InetAddress.getByName(ServerHost);
//
//      //Establish a TCP connection with the server to exchange RTSP messages
//      //------------------
//      thePeer.serverSocket = new Socket(ServerIPAddr, RTSP_server_port);
//       System.out.println("In client");
//      //Set input and output stream filters:
//      clientInputBuffer = new BufferedReader(new InputStreamReader(thePeer.serverSocket.getInputStream()) );
//      clientOutputBuffer = new BufferedWriter(new OutputStreamWriter(thePeer.serverSocket.getOutputStream()) );
//      fileNames="test2.avi";
//     // String allfiles= "";
//      System.out.println("client"+fileNames);
//      //init RTSP state:
//      state = INIT;
    
    //clientOutputBuffer.write(fileNames.size() + Constants.CRLF);
      
 

      
      
      
           
    }
    
    class setupButtonListener implements ActionListener{
    	 Socket serverSocket;
    	 InetAddress ServerIPAddr;
        public void actionPerformed(ActionEvent e){

          //System.out.println("Setup Button pressed !");      


		RTSP_server_port = 3003;//Integer.parseInt(argv[1]);
		String ServerHost = "localhost";//argv[0];
		 try {
			ServerIPAddr= InetAddress.getByName(ServerHost);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

//Establish a TCP connection with the server to exchange RTSP messages
//------------------
		try {
			serverSocket = new Socket(ServerIPAddr, RTSP_server_port);
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
		fileNames="test2.avi";
		// String allfiles= "";
		System.out.println("client"+fileNames);
		//init RTSP state:
		state = INIT;

          if (state == INIT) 
    	{
    	  //Init non-blocking RTPsocket that will be used to receive data
    	 
    	  //Send SETUP message to the server
        	   try {
				clientOutputBuffer.write("INIT " + RTSP_server_port + Constants.CRLF + 1 + Constants.CRLF+fileNames+Constants.CRLF);
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


    	 RTSP_server_port = 3003;//Integer.parseInt(argv[1]);
    	 String ServerHost = "localhost";//argv[0];
    	 try {
    		 ServerIPAddr= InetAddress.getByName(ServerHost);
    	 } catch (UnknownHostException e2) {
    		 // TODO Auto-generated catch block
    		 e2.printStackTrace();
    	 }

    	 //Establish a TCP connection with the server to exchange RTSP messages
    	 //------------------
    	 try {
    		 serverSocket = new Socket(ServerIPAddr, RTSP_server_port);
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
		reqfileName="test2.avi";
		//String allfiles= "";
		System.out.println("client"+fileNames);
		//init RTSP state:

          //System.out.println("Setup Button pressed !");      


    	  //Init non-blocking RTPsocket that will be used to receive data
    	 
    	  //Send SETUP message to the server
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
				System.out.println(clientInputBuffer.readLine());
				System.out.println(clientInputBuffer.readLine());
        	   } 
        	   catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	   
        	Client client = new Client();  
        try {
        	 InetAddress ServerIPAddr = InetAddress.getByName("192.168.1.8");
             client.start_client(4000,ServerIPAddr,"C:\\Users\\Amritha\\Downloads\\movie.Mjpeg");

        	//	client.start_client(4000,"198.162.1.8","C:\\Users\\Amritha\\Downloads\\movie.Mjpeg");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	   
    	}

          
      }
      
      
}
