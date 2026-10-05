package world.qode.app;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Book {

	@Id
	@GeneratedValue
	private Long id;

	private String title;

	private int published;

	@ManyToOne(optional = false)
	private Author author;

	protected Book() {
	}

	public Book(String title, int published, Author author) {
		this.title = title;
		this.published = published;
		this.author = author;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public int getPublished() {
		return published;
	}

	public Author getAuthor() {
		return author;
	}

}
