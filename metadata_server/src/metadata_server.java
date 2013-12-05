import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
        

public class metadata_server {
        //private variables
        Socket RTSPsocket;
        static int RTSP_ID = 123456; //ID of the RTSP session
        int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
        static InetAddress ClientIPAddr; //Client IP address
        static int ClientPortNumber;
        static BufferedReader RTSPBufferedReader;
        static BufferedWriter RTSPBufferedWriter;
        
        //rtsp states
        final static int INIT = 0;  //format: INIT portnumber
                                                                        // number of files
                                                                        // n lines of filenames
        final static int REQUEST = 1; // format: REQUEST filename
        final static int COMPLETE = 2;// format: COMPLETE filename IPaddress
        
        static String VideoFileName; //video file requested from the client
        
        final static String CRLF = "\r\n";
        
        static Map<String, ArrayList<peer_info>> metadata= new HashMap<String, ArrayList<peer_info>>();
        
        
        public metadata_server() {
                
        }

        public static void main(String argv[]) throws Exception
        {
                metadata_server theServer = new metadata_server();
                //get RTSP socket port from the command line
            int RTSPport = 3003;//Integer.parseInt(argv[0]);//listening port
           
            while(true) {
                    //Initiate TCP connection with the client for the RTSP session
            		System.out.println("Creating new socket");
                    ServerSocket listenSocket = new ServerSocket(RTSPport);
                    theServer.RTSPsocket = listenSocket.accept();
                    System.out.println("Connection accepted");
                    //listenSocket.close();
        
                    //Get Client IP address
                    theServer.ClientIPAddr = theServer.RTSPsocket.getInetAddress();
                    System.out.println("Received connection from " + theServer.ClientIPAddr);
                    RTSPBufferedReader = new BufferedReader(new InputStreamReader(theServer.RTSPsocket.getInputStream()) );
                    RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theServer.RTSPsocket.getOutputStream()) );
        
                    int request_type;
                    boolean done = false;
                    //while(!done)
                    //{
                            request_type = theServer.parse_RTSP_request(); //blocking
                            
                            if (request_type == INIT) {
                                    
                                    }
                            else if(request_type == REQUEST ) {
                                    peer_info selected_peer = select_a_peer();
                                    theServer.send_RTSP_response_REQUEST(selected_peer);
                                    //listenSocket.close();
                            }
                            else if(request_type == COMPLETE) {
                                    //decide if you want to send response
                                    decrement_number_peer(theServer.ClientIPAddr);
                            }
                             //       done = true;
                            listenSocket.close();
                            System.out.println("Connection closed with peer");
                            //}
                    //}
                            
            }
                
        }
        
        private static void decrement_number_peer(InetAddress clientIP) {
        	
        	ArrayList<peer_info> curr_list = metadata.get(VideoFileName);
            
            peer_info output = curr_list.get(0);
            
            
          
                    Iterator<peer_info> it = curr_list.iterator();
                    System.out.println("recieved complete from"+clientIP);
                    while (it.hasNext()) {
                            peer_info temp = it.next();
                            System.out.println("trying peer " + temp.ClientIPAddr);
                            System.out.println("no. of conn peers: " + temp.no_of_con_peers);
                            
                          if (temp.ClientIPAddr.toString().equals(clientIP.toString()) ) {
                        	  //output=temp;
                        	  System.out.println("inside if-"+temp.ClientIPAddr.toString());
                        	  output.no_of_con_peers--;
                                //  temp.no_of_con_peers--;
                                  System.out.println("peer decremented"+ clientIP.toString());
                          }
                   
                    }
           
            
            //add new peer to list
            
//                ArrayList<peer_info> curr_list = metadata.get(VideoFileName);
//                Iterator<peer_info> it = curr_list.iterator();
//                
//                while (it.hasNext()) {
//                        peer_info temp = it.next();
//                        if (temp.ClientIPAddr == clientIP) {
//                                temp.no_of_con_peers--;
//                                System.out.println("peer decremented"+ clientIP.toString());
//                        }
//                }
                
                
               // System.out.println();
        }
        
        private static peer_info select_a_peer() //havent checked when filename not present
        {
        	if(! metadata.containsKey(VideoFileName)){
        		System.out.println("File not present, no key!");
        	}
                ArrayList<peer_info> curr_list = metadata.get(VideoFileName);
                
                peer_info output = curr_list.get(0);
                
                
                if (output != null) {
                        Iterator<peer_info> it = curr_list.iterator();
                        
                        while (it.hasNext()) {
                                peer_info temp = it.next();
                                System.out.println("trying peer " + temp.ClientIPAddr);
                                System.out.println("no. of conn peers: " + temp.no_of_con_peers);
                                if (temp.no_of_con_peers < output.no_of_con_peers) 
                                        output = temp;
                        }
                        System.out.println("selected peer " + output.ClientIPAddr);
                }
                else{
                        System.out.println("File not present!!!!");
                        //exit(0);
                }
                System.out.println("sending IP: "+ output.ClientIPAddr);
                output.no_of_con_peers++;
                
                //add new peer to list
                
                peer_info current = new peer_info();
                current.ClientIPAddr = ClientIPAddr;
                current.port_number = ClientPortNumber;
                current.no_of_con_peers = 0;   
                curr_list.add(current);
                
                return output;
                
        }
        
        //------------------------------------
          //Parse RTSP Request
          //------------------------------------
          private int parse_RTSP_request()
          {
            int request_type = -1;
            System.out.println("metadata parsereq function");
            try{
              //parse request line and extract the request_type:
              String RequestLine = RTSPBufferedReader.readLine();
              System.out.println("RTSP Server - Received from Client:");
              System.out.println(RequestLine);

              StringTokenizer tokens = new StringTokenizer(RequestLine);
              String request_type_string = tokens.nextToken();

              //convert to request_type structure:
              if ((new String(request_type_string)).compareTo("INIT") == 0){
                              request_type = INIT;
                              System.out.println("inside if loop server");}
              else if ((new String(request_type_string)).compareTo("REQUEST") == 0)
                              request_type = REQUEST;
              else if ((new String(request_type_string)).compareTo("COMPLETE") == 0)
                              request_type = COMPLETE;
             
              if(request_type == INIT) {
                      System.out.println("in server");
                      ClientPortNumber = Integer.parseInt(tokens.nextToken());
                      int number_of_files = Integer.parseInt(RTSPBufferedReader.readLine());
                      for(int i = 0; i < number_of_files; i++) {
                              //fill data structure
                              peer_info current = new peer_info();
                              current.ClientIPAddr = ClientIPAddr;
                              current.port_number = ClientPortNumber;
                              current.no_of_con_peers = 0;
                              String video_filename = RTSPBufferedReader.readLine();
                              ArrayList<peer_info> curr_list;
                              if(!metadata.containsKey(video_filename)) {
                                      curr_list = new ArrayList<peer_info>();//if the video is not present in the main list(metadata) that is maintained, create a new ArrayList
                              }
                              else {
                                      curr_list = metadata.get(video_filename);//if it is already present, get the instance of the ArrayList
                              }
                              curr_list.add(current); // Add current peer info to the ArrayList
                              System.out.println("inside for server");
                              metadata.put(video_filename, curr_list); // Update metadata
                              System.out.println(video_filename);
                              System.out.println(current.ClientIPAddr);
                          
                             
                      }
//                      Collection<ArrayList<peer_info>> coll= metadata.values();
//                      for(int j=0; j< coll.size(); j++)
//                      {
//                    	 coll[j][0].port_number = 0;
//                      }
              }
              else if(request_type == COMPLETE) {              
                      //extract VideoFileName from RequestLine
                      VideoFileName = tokens.nextToken();
                      //ClientIPAddr  = InetAddress.getByName(tokens.nextToken());
                     
              }
              else if(request_type == REQUEST) {
                      VideoFileName = tokens.nextToken();
              }
            }
            catch(Exception ex)
              {
                System.out.println("Exception caught 2: "+ex);
                //System.exit(0);
              }
            return(request_type);
          }
          
        //------------------------------------
        //Send RTSP Response
        //------------------------------------
          private void send_RTSP_response_REQUEST(peer_info selected) 
          {
           try{
        	   
        	
                    RTSPBufferedWriter.write(selected.ClientIPAddr.toString() + CRLF);
                    RTSPBufferedWriter.write(Integer.toString(selected.port_number) + CRLF);
                          RTSPBufferedWriter.flush();
                          //System.out.println("RTSP Server - Sent response to Client.");
            }
            catch(Exception ex)
                  {
                    System.out.println("Exception caught 3: "+ex);
                        //System.exit(0);
                     }
          }//end of send_RTSP_response

} //end of server