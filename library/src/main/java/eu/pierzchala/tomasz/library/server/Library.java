package eu.pierzchala.tomasz.library.server;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import eu.pierzchala.tomasz.library.entity.Book;

public class Library {
	private Long LAST_BOOK_ID;
	private List<Book> lib;

	public Library() {
		LAST_BOOK_ID = 0l;
		this.lib = new LinkedList<Book>();
	}

	private synchronized Long incrementBookId() {
		if (LAST_BOOK_ID < 0l) {
			throw new RuntimeException("No matter how unbelievably it sounds, You have already used all possible IDs.");
		}
		return ++LAST_BOOK_ID;

	}

	public synchronized void addNewBook(String title, String author, int year) {
		Long nextBookId = incrementBookId();
		Book newBook = new Book(nextBookId, title, author, year);
		lib.add(newBook);
	}

	public synchronized Long getLastBookId() {
		return LAST_BOOK_ID;
	}

	public List<Book> getBookList() {
		return lib;
	}

	public synchronized void removeBook(long id) {
		lib = lib.parallelStream().filter(i -> i.getId() != id || i.getBookLender() != null)
				.collect(Collectors.toList());
	}

	public void lendBook(long id, String bookLender) {
		if (bookLender == null || bookLender.isEmpty()){
			return;
		}
		synchronized (this) {
			if (lib.parallelStream().filter(i -> i.getId() == id && i.getBookLender() != null).count() > 0) {
				System.out.println("Can NOT remove book [" + id + "] as it is lent.");
				return;
			}
			lib.parallelStream().filter(i -> i.getId() == id && i.getBookLender() == null)
					.forEach(i -> i.setBookLender(bookLender));
		}
	}

	public synchronized void listBooks() {
		System.out.println("\nBooks lent :");
		lib.stream().filter(i -> i.getBookLender() != null).forEach(System.out::println);
		long lent = lib.parallelStream().filter(i -> i.getBookLender() != null).count();
		System.out.println("\nBooks still available :");
		lib.stream().filter(i -> i.getBookLender() == null).forEach(System.out::println);
		long available = lib.parallelStream().filter(i -> i.getBookLender() == null).count();
		System.out.println("------------------------------------------------------------------");
		System.out.println(
				"Lent : " + lent + "\t\tAvailable : " + available + "\t\tTotal :" + (lent + available));
		System.out.println();
	}
	
	private static Predicate<Book> inTitle( String part ){return b -> b.getTitle().contains(part); }
	private static Predicate<Book> inAuthor( String part ){return b -> b.getAuthor().contains(part); }
	private static Predicate<Book> inYear( int year ){return b -> b.getYear()==year; }
	private static Predicate<Book> before(int year){return b->b.getYear()<year;}
	private static Predicate<Book> after(int year){return b->b.getYear()>year;}
	private static Predicate<Book> TRUE = b->true;

	private static Predicate<Book> is(String key, String value){
		if(value.isEmpty()) return TRUE;
		try{
		switch(key.toLowerCase()){
			case "title" :
				return inTitle(value);
			case "author" : 
				return inAuthor(value);
			case "year" :
				return inYear(Integer.parseInt(value));
			case "before" :
				return before(Integer.parseInt(value));
			case "after" :
				return after(Integer.parseInt(value));
			default :
			return TRUE;
		}
		}catch(NumberFormatException nfe){
			return TRUE; // when condition is wrong, just DO NOT take into account
		}
	}
	
	public List<Book> searchBooks(String params){
		// execution "name:book author:pierzchala" 
		String str[] = params.split("\\s+");
		Predicate<Book> composed = TRUE;
		for(String s : str){
			String key = s.substring(0, s.indexOf(':'));
			String value = s.substring(s.indexOf(':')+1, s.length());
			composed = composed.and(is(key,value));
		}
		return lib.stream().filter(composed).collect(Collectors.toList());
	}
	
	public Book findBook(long id){
		List<Book> found = lib.parallelStream().filter(i -> i.getId() == id).collect(Collectors.toList());
		Book theBook = null;
		if(found.size()>0){
			theBook = found.get(0);
		} 
		return theBook;
	}
			
	public void showBookDetails(long id) {
		Book found = findBook(id);
		if(found != null) {
			System.out.println(found);
		} else {
			System.out.println("There is no book with ID = " + id);
		}
	}
}
