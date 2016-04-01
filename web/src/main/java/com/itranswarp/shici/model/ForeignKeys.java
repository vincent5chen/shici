package com.itranswarp.shici.model;

public interface ForeignKeys {

	static final String[] FOREIGN_KEYS = { "alter table Poem add foreign key (poetId) references Poet (id);",
			// "alter table CategoryPoem add foreign key (categoryId) references
			// Category (id);",
			// "alter table CategoryPoem add foreign key (poemId) references
			// Poem (id);",
			// "alter table FeaturedPoem add foreign key (poemId) references
			// Poem (id);"
	};
}
