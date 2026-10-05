package world.qode.app;

import java.util.List;

import org.hibernate.SessionFactory;

/**
 * The job: persist a few entities, query them back with HQL, exit 0.
 */
public final class Main {

	public static void main(String[] args) {
		Database.Target target = Database.target(System.getenv("DATABASE_URL"));
		System.out.println("database: " + target.jdbcUrl());

		try (SessionFactory sf = Database.sessionFactory(target)) {
			sf.inTransaction(session -> {
				Author tolkien = new Author("J.R.R. Tolkien");
				Author le_guin = new Author("Ursula K. Le Guin");
				session.persist(tolkien);
				session.persist(le_guin);
				session.persist(new Book("The Hobbit", 1937, tolkien));
				session.persist(new Book("The Fellowship of the Ring", 1954, tolkien));
				session.persist(new Book("A Wizard of Earthsea", 1968, le_guin));
			});

			List<Book> books = sf.fromTransaction(session -> session
				.createSelectionQuery("from Book b join fetch b.author order by b.published", Book.class)
				.getResultList());
			books.forEach(b -> System.out.printf("%d  %-30s %s%n", b.getPublished(), b.getTitle(), b.getAuthor().getName()));

			long count = sf.fromTransaction(session -> session
				.createSelectionQuery("select count(b) from Book b where b.author.name = :name", Long.class)
				.setParameter("name", "J.R.R. Tolkien")
				.getSingleResult());
			System.out.println("books by Tolkien: " + count);
		}
		System.out.println("done");
	}

	private Main() {
	}

}
