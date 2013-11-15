import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Peer {
	  //RTSP variables
	  //----------------
	  //rtsp states 
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
    static ArrayList<String> fileNames;
    
    

    public Peer() throws IOException {
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
    public void listenForRequests() throws IOException {
        while(true){
            ServerSocket listenSocket = new ServerSocket(servicingPort);
            this.listeningSocket = listenSocket.accept();
            clientInputBuffer = new BufferedReader(new InputStreamReader(listeningSocket.getInputStream()));
            String fileName = clientInputBuffer.readLine();
            VideoServer videoServer = new VideoServer(fileName, nextRTSPPort, nextRTPPort);
            //create separate thread to service the request
            clientOutputBuffer.write("RTSP:" + nextRTSPPort);
            clientOutputBuffer.write("RTP:" + nextRTPPort);
            nextRTPPort++;
            nextRTSPPort++;
            this.listeningSocket.close();
        }
    }
    
    public static void main(String argv[]) throws Exception
    {
    	fileNames= new ArrayList<String>();
      //Create a Client object
     Peer thePeer = new Peer();
      
      //get server RTSP port and IP address from the command line
      //------------------
      int RTSP_server_port = 4003;//Integer.parseInt(argv[1]);
      String ServerHost = "localhost";//argv[0];
      InetAddress ServerIPAddr = InetAddress.getByName(ServerHost);

      //Establish a TCP connection with the server to exchange RTSP messages
      //------------------
      thePeer.serverSocket = new Socket(ServerIPAddr, RTSP_server_port);
System.out.println("In client");
      //Set input and output stream filters:
      clientInputBuffer = new BufferedReader(new InputStreamReader(thePeer.serverSocket.getInputStream()) );
      clientOutputBuffer = new BufferedWriter(new OutputStreamWriter(thePeer.serverSocket.getOutputStream()) );
      fileNames.add("test.avi");
      String allfiles= "";
      System.out.println("client"+fileNames);
      //init RTSP state:
      state = INIT;
    
    //clientOutputBuffer.write(fileNames.size() + Constants.CRLF);
    for (String fileName : fileNames) {
    	allfiles=allfiles+fileName+ Constants.CRLF;
        //clientOutputBuffer.write(fileName + Constants.CRLF);
    }
    clientOutputBuffer.write("INIT " + RTSP_server_port + Constants.CRLF + fileNames.size() + Constants.CRLF + allfiles);
    //clientOutputBuffer.flush();
      
      
      
      
    }
}
