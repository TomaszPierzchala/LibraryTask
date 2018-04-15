package eu.pierzchala.tomasz.library.entity;

public class Book {
	
	private Long id;
	private String title;
	private String author;
	private int year;
	
	private String bookLender;

	public Book(String title, String author, int year){
		//
		this.title = title;
		this.author = author;
		this.year = year;
	}
	
	public Book(Long id, String title, String author, int year){
		this(title, author, year);
		this.id = id;
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
