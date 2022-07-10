package sg.edu.np.ignight.ChatRequest;

import java.text.ParseException;

import sg.edu.np.ignight.Objects.TimestampObject;

public class ChatRequestObject {

    private String requestID;
    private String creatorID, creatorName;
    private String receiverID, receiverName;
    private String creatorProfile, receiverProfile;
    private TimestampObject createTimestamp, responseTimestamp;
    private boolean pendingRequest, requestAccepted;

    public ChatRequestObject(String requestID, String creatorID, String receiverID, String createTimestampString, boolean pendingRequest) throws ParseException {
        this.requestID = requestID;
        this.creatorID = creatorID;
        this.receiverID = receiverID;
        this.createTimestamp = new TimestampObject(createTimestampString);
        this.pendingRequest = pendingRequest;
    }

    public String getRequestID() {
        return requestID;
    }
    public String getCreatorID() {
        return creatorID;
    }
    public String getCreatorName() {
        return creatorName;
    }
    public String getReceiverID() {
        return receiverID;
    }
    public String getReceiverName() {
        return receiverName;
    }
    public String getCreatorProfile() {
        return creatorProfile;
    }
    public String getReceiverProfile() {
        return receiverProfile;
    }
    public TimestampObject getCreateTimestamp() {
        return createTimestamp;
    }
    public TimestampObject getResponseTimestamp() {
        return responseTimestamp;
    }
    public boolean isPendingRequest() {
        return pendingRequest;
    }
    public boolean isRequestAccepted() {
        return requestAccepted;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    public void setCreatorProfile(String creatorProfile) {
        this.creatorProfile = creatorProfile;
    }
    public void setReceiverProfile(String receiverProfile) {
        this.receiverProfile = receiverProfile;
    }
    public void setResponseTimestamp(String responseTimestampString) throws ParseException {
        this.responseTimestamp = new TimestampObject(responseTimestampString);
    }
    public void setPendingRequest(boolean pendingRequest) {
        this.pendingRequest = pendingRequest;
    }
    public void setRequestAccepted(boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
    }
}
