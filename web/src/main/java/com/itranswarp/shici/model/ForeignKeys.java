package com.itranswarp.shici.model;

public interface ForeignKeys {

	static final String[] FOREIGN_KEYS = { "alter table Poem add foreign key (poetId) references Poet (id)" };
}
