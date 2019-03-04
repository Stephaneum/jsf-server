package sitzung;

public class Count {
	
	/*
	 * für die letzten 30 Tage, 1 Objekt entspricht 1 Tag
	 */
	
	private int views;
	private String date;
	private int day, month;
	
	public Count(String date, int views, int day, int month) {
		this.date = date;
		this.views = views;
		this.day = day;
		this.month = month;
	}
	
	public int getViews() {
		return views;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public void setViews(int views) {
		this.views = views;
	}
	
	public int getDay() {
		return day;
	}
	
	public int getMonth() {
		return month;
	}
	
	@Override
	public String toString() {
		return date;
	}

}
