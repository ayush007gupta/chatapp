
package server;


import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable
{
    private String from;
    private String to;
    private String content;
    private Timestamp SentTime;
    private Timestamp RecievedTime;
    private Timestamp SeenTime;

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Message(String from, String to, String content, Timestamp sentTime, Timestamp recievedTime, Timestamp seenTime) {
        this.from = from;
        this.to = to;
        this.content = content;
        SentTime = sentTime;
        RecievedTime = recievedTime;
        SeenTime = seenTime;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getSentTime() {
        return SentTime;
    }

    public void setSentTime(Timestamp sentTime) {
        SentTime = sentTime;
    }

    public Timestamp getRecievedTime() {
        return RecievedTime;
    }

    public void setRecievedTime(Timestamp receivedTime) {
        RecievedTime = receivedTime;
    }

    public Timestamp getSeenTime() {
        return SeenTime;
    }

    public void setSeenTime(Timestamp seenTime) {
        SeenTime = seenTime;
    }
}


