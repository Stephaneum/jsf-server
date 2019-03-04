package objects;

public class Nachricht {
	
	private int id, senderID;
	private String sender;
	private String text, date, time;
	
	public Nachricht(int nachrichtID, int senderID, String sender, int projektID, String text, String date, String time) {
		this.id = nachrichtID;
		this.senderID = senderID;
		this.sender = sender;
		this.text = text;
		this.date = date;
		this.time = time;
	}
	
	public int getId() {
		return id;
	}
	
	public int getSenderID() {
		return senderID;
	}

	public String getSender() {
		return sender;
	}
	
	public String getText() {
		return text;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getTime() {
		return time;
	}
}
