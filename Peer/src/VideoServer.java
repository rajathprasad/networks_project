public class VideoServer {
    String fileName;
    int nextRTSPPort;
    int nextRTPPort;

    public VideoServer(String fileName, int nextRTSPPort, int nextRTPPort){
        this.fileName = fileName;
        this.nextRTPPort = nextRTPPort;
        this.nextRTSPPort = nextRTSPPort;
    }
}
