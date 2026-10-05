package world.qode.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

class DatabaseTest {

	@Test
	void parsesDatabaseUrl() {
		Database.Target t = Database.target("postgres://fleet:p%40ss@db.internal:6543/app?sslmode=disable");
		assertEquals("jdbc:postgresql://db.internal:6543/app?sslmode=disable", t.jdbcUrl());
		assertEquals("fleet", t.user());
		assertEquals("p@ss", t.password());
	}

	@Test
	void defaultsToEmbeddedH2() {
		assertTrue(Database.target(null).embedded());
	}

	@Test
	void persistsAndQueries() {
		try (SessionFactory sf = Database.sessionFactory(new Database.Target("jdbc:h2:mem:test", "sa", "", true))) {
			sf.inTransaction(s -> {
				Author a = new Author("A");
				s.persist(a);
				s.persist(new Book("B", 2000, a));
			});
			long n = sf.fromTransaction(s -> s.createSelectionQuery("select count(b) from Book b", Long.class).getSingleResult());
			assertEquals(1, n);
		}
	}

}
