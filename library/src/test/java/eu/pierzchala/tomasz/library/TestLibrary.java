package eu.pierzchala.tomasz.library;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.pierzchala.tomasz.library.entity.Book;
import eu.pierzchala.tomasz.library.server.Library;

public class TestLibrary {

	Library library;

	@Before
	public void initialize() {
		library = new Library();
	}

	@Test
	public void initialLibraryIsEmpty() {
		assertEquals(new Long(0l), library.getLastBookId());
		assertEquals(0, library.getBookList().size());
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void incrementBookId_used_all_IDs_Exception() {
		try {
			Field last_id_field = library.getClass().getDeclaredField("LAST_BOOK_ID");
			last_id_field.setAccessible(true);
			last_id_field.set(library, new Long(-1l));
		} catch (Exception e) {
			e.printStackTrace();
		}

		thrown.expect(RuntimeException.class);
		thrown.expectMessage("No matter how unbelievably it sounds, You have already used all possible IDs.");
		library.addNewBook("Some title", "Some Author", 2018);
	}

	private void sleep(long msec) {
		// auxiliary method
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void addBooksByFewClients() {
		// settings
		final int N1 = 200, N2 = 200, N3 = 200;

		// Clients
		Thread one = new Thread(new Runnable() {

			public void run() {
				for (int i = 0; i < N1; i++) {
					library.addNewBook("Title XXX", "Author1, Author9", 1999);
					sleep(1);
				}
			}
		});
		Thread two = new Thread(new Runnable() {

			public void run() {
				for (int i = 0; i < N2; i++) {
					library.addNewBook("Title YYYY", "Author2", 2015);
					sleep(1);
				}
			}
		});
		Thread three = new Thread(new Runnable() {

			public void run() {
				for (int i = 0; i < N3; i++) {
					library.addNewBook("Title ZZZ", "Author13, Author6", 2016);
					sleep(1);
				}
			}
		});

		// Run
		one.start();
		two.start();
		three.start();
		try {
			one.join();
			two.join();
			three.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(new Long(N1 + N2 + N3), library.getLastBookId());

		assertEquals(N1 + N2 + N3, library.getBookList().size());

		assertEquals(0, library.getBookList().parallelStream().filter(i -> i.getBookLender() != null).count());
	}

	@Test
	public void lendAndRemoveBooksById() {
		// set initial 15 books in the test Library
		int nBooks = 15;
		for (int i = 0; i < nBooks; i++) {
			library.addNewBook("A best seller", "Famous Author", 2010);
		}

		assertEquals(0, library.getBookList().parallelStream().filter(i -> i.getBookLender() != null).count());

		library.lendBook(5, "Tomasz");// lent
		assertEquals(1, library.getBookList().parallelStream().filter(i -> i.getBookLender() != null).count());

		library.lendBook(50, "Tomasz");// NOT lent
		assertEquals(1, library.getBookList().parallelStream().filter(i -> i.getBookLender() != null).count());

		library.lendBook(6, null);// NOT lent as unknown book lender
		assertEquals(1, library.getBookList().parallelStream().filter(i -> i.getBookLender() != null).count());

		library.lendBook(6, "");// NOT lent as unknown book lender
		assertEquals(1, library.getBookList().parallelStream().filter(i -> i.getBookLender() != null).count());
		
		library.lendBook(5, "Tomasz");// NOT lent too, as already lent
		assertEquals(1, library.getBookList().parallelStream().filter(i -> i.getBookLender() != null).count());

		assertEquals(nBooks, library.getBookList().parallelStream().count());
		library.removeBook(5); // can NOT be removed as lent
		assertEquals(nBooks, library.getBookList().parallelStream().count());

		library.removeBook(6); // can be removed
		assertEquals(nBooks - 1, library.getBookList().parallelStream().count());

		library.removeBook(69); // can be removed, out of library scope
		assertEquals(nBooks - 1, library.getBookList().parallelStream().count());

//		library.listBooks();
	}

	@Test
	public void findBook() {
		// set
		int N1 = 693, N3 = 1256;

		for (int i = 1; i < N1; i++) {
			library.addNewBook("A book", "An author", 1965);
		}

		library.addNewBook("The book", "The author", 2002);

		for (int i = 0; i < N3; i++) {
			library.addNewBook("A book", "An author", 1965);
		}

		Book theBook = library.findBook(N1);
		assertEquals(N1, theBook.getId());
		assertEquals("The book", theBook.getTitle());
		assertEquals("The author", theBook.getAuthor());
		assertEquals(2002, theBook.getYear());
	}

	@Test
	public void searchBook() {
		// set
		for (int i = 0; i < 5; i++) {
			library.addNewBook("The book", "Author", 1999);
			library.addNewBook("Cook book", "Another Author", 2003);
			library.addNewBook("Letters", "Unknown", 1590);
		}

		System.out.println("title:book");
		List<Book> found = library.searchBooks("title:book");
		assertEquals(10, found.size());
		found.stream().forEach(System.out::println);
		System.out.println();

		System.out.println("aUthor:known");
		found = library.searchBooks("aUthor:known");
		assertEquals(5, found.size());
		found.stream().forEach(System.out::println);
		System.out.println();

		System.out.println("aUthor:known year:1590");
		found = library.searchBooks("aUthor:known year:1590");
		assertEquals(5, found.size());
		found.stream().forEach(System.out::println);
		System.out.println();

		System.out.println("aUthor:known year:1999");
		found = library.searchBooks("aUthor:known year:1999");
		assertEquals(0, found.size());
		found.stream().forEach(System.out::println);
		System.out.println();

		System.out.println("aUthor:known year:1590a");
		found = library.searchBooks("aUthor:known year:1590a");
		assertEquals(5, found.size());
		found.stream().forEach(System.out::println);
		System.out.println();

		System.out.println("authoR:thor after:2000");
		found = library.searchBooks("authoR:thor after:2000");
		assertEquals(5, found.size());
		found.stream().forEach(System.out::println);
		System.out.println();

		System.out.println("before:1600 year:");
		found = library.searchBooks("before:1600 year:");
		assertEquals(5, found.size());
		found.stream().forEach(System.out::println);
		System.out.println();

		System.out.println("before:1600 year: unknownKey:value");
		found = library.searchBooks("before:1600 year: unknownKey:value");
		assertEquals(5, found.size());
		found.stream().forEach(System.out::println);
		System.out.println();
	}

	@Test
	public void listAllBooks() {
		// set
		for (int i = 0; i < 5; i++) {
			library.addNewBook("The book", "Author", 1999);
			library.addNewBook("Cook book", "Another Author", 2003);
			library.addNewBook("Letters", "Unknown", 1590);
		}
		library.lendBook(1, "Tomek");
		library.lendBook(3, "Maciek");
		
		// list 
		library.listBooks();
		
	}
	
	@Test
	public void showBookDetails() {
		library.addNewBook("The book", "Author", 1999);
		library.lendBook(1, "Tomek");
		//
		System.out.println("showBookDetails() :");
		System.out.print("Found book :  ");
		library.showBookDetails(1l);
		
		System.out.println();
		// no such book
		library.showBookDetails(2l);
		System.out.println("------------------");
		
	}
}