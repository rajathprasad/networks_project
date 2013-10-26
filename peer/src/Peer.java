import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Peer {
    static int nextRTPPort = 3000;
    static int nextRTSPPort = 2500;
    BufferedWriter serverOutputBuffer;
    BufferedReader clientInputBuffer;
    BufferedWriter clientOutputBuffer;
    Socket serverSocket;
    Socket listeningSocket;
    int centralPort;

    public Peer(int centralPort) throws IOException {
        this.centralPort = centralPort;
        this.serverSocket = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
        this.serverOutputBuffer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));

    }
    public void initialize(List<String> fileNames) throws IOException {
        serverOutputBuffer.write("INIT " + centralPort + Constants.CRLF);
        serverOutputBuffer.write(fileNames.size() + Constants.CRLF);
        for (String fileName : fileNames) {
            serverOutputBuffer.write(fileName + Constants.CRLF);
        }
        serverOutputBuffer.flush();
    }
    public void listenForRequests() throws IOException {
        while(true){
            ServerSocket listenSocket = new ServerSocket(centralPort);
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
}
