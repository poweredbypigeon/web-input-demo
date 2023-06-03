/**
 * Stores all the stuff related to the request (e.g. the file it's asking for, the host, etc. etc. etc.) Only accepts the raw request as the argument.
 * */

public class Request {
    private String rawRequest;
    private String fileRequested;

    public Request (String rawRequest) {
        this.rawRequest = rawRequest;
        // do some processing in here. 
    }
}
