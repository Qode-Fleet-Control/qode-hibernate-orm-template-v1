package world.qode.app;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;

/**
 * Builds the SessionFactory: an in-memory H2 database by default, or the
 * Postgres named by DATABASE_URL (postgres://user:pass@host:port/db) when set.
 */
public final class Database {

	/** A JDBC URL plus credentials. */
	public record Target(String jdbcUrl, String user, String password, boolean embedded) {
	}

	public static Target target(String databaseUrl) {
		if (databaseUrl == null || databaseUrl.isBlank()) {
			return new Target("jdbc:h2:mem:app;DB_CLOSE_DELAY=-1", "sa", "", true);
		}
		if (databaseUrl.startsWith("jdbc:")) {
			return new Target(databaseUrl, null, null, false);
		}
		URI uri = URI.create(databaseUrl);
		String user = null;
		String password = null;
		if (uri.getRawUserInfo() != null) {
			String[] parts = uri.getRawUserInfo().split(":", 2);
			user = decode(parts[0]);
			password = parts.length > 1 ? decode(parts[1]) : null;
		}
		int port = uri.getPort() == -1 ? 5432 : uri.getPort();
		String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
		String jdbc = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getRawPath() + query;
		return new Target(jdbc, user, password, false);
	}

	public static SessionFactory sessionFactory(Target target) {
		HibernatePersistenceConfiguration cfg = new HibernatePersistenceConfiguration("app")
			.managedClass(Author.class)
			.managedClass(Book.class)
			.jdbcUrl(target.jdbcUrl())
			// The embedded database is created fresh each run; a real one is only
			// ever added to (never dropped) - use a migration tool for real schemas.
			.schemaToolingAction(target.embedded() ? Action.CREATE_DROP : Action.UPDATE)
			.showSql(true, true, false);
		if (target.user() != null) {
			cfg.jdbcCredentials(target.user(), target.password() == null ? "" : target.password());
		}
		return cfg.createEntityManagerFactory();
	}

	private static String decode(String s) {
		return URLDecoder.decode(s, StandardCharsets.UTF_8);
	}

	private Database() {
	}

}
