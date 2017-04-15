package eu.pierzchala.tomasz.library.entity;

import eu.pierzchala.tomasz.library.server.Library;

public class Book {
	private Library belongsTo;
	
	private long id;
	private String title;
	private String author;
	private int year;
	
	private String bookLender;

	public Book( String title, String author, int year, Library addedTo){
		this.belongsTo = addedTo;
		//
		this.id = belongsTo.incrementBookId();
		this.title = title;
		this.author = author;
		this.year = year;
	}
	
	public void newBook(String title, String author, short year){
		
	}
	
	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public int getYear() {
		return year;
	}

	public String getBookLender() {
		return bookLender;
	}

	public void setBookLender(String bookLender) {
		this.bookLender = bookLender;
	}

	@Override
	public String toString() {
		return "Book [id=" + id + ", title=" + title + ", author=" + author + ", year=" + year + ((bookLender!=null)?", bookLender="
				+ bookLender + "]" : "]");
	}

}
