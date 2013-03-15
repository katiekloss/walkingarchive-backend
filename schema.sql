---
--- Extensions
---
CREATE EXTENSION hstore;

---
--- Types
---

CREATE TYPE manacolor AS ENUM('white', 'blue', 'black', 'red', 'green', 'colorless',
	'whiteblue', 'whiteblack', 'blueblack', 'bluered', 'blackred',
	'blackgreen', 'redgreen', 'redwhite', 'greenwhite', 'greenblue',
	'2white', '2blue', '2black', '2red', '2green');

CREATE TYPE type AS ENUM('artifact', 'creature', 'enchantment', 'instant', 'interrupt',
	'land', 'mana source', 'phenomenon', 'plane', 'planeswalker', 'scheme', 'sorcery',
	'summon', 'tribal', 'vanguard');

CREATE TYPE tradedirection AS ENUM('giving', 'receiving');

---
--- Tables
----

CREATE TABLE Sets (
	setid serial NOT NULL,
	setname character varying(50),

	PRIMARY KEY (setid)
);

CREATE TABLE Cards (
	cardid serial NOT NULL,
	name character varying(150) NOT NULL,
	mana hstore NOT NULL,
	type type NOT NULL,
	subtype character varying(40),
	cardtext text,
	flavortext text,
	extid integer NOT NULL,

	PRIMARY KEY (cardid)
);

CREATE TABLE CardSets (
	cardid integer NOT NULL,
	setid integer NOT NULL,

	PRIMARY KEY (cardid, setid),
	FOREIGN KEY (cardid) REFERENCES Cards ON DELETE CASCADE,
	FOREIGN KEY (setid) REFERENCES Sets ON DELETE CASCADE
);

CREATE TABLE CardVectors (
	cardid integer NOT NULL,
	textvector tsvector,

	PRIMARY KEY (cardid),
	FOREIGN KEY (cardid) REFERENCES Cards ON DELETE CASCADE
);

CREATE TABLE Users (
	userid serial NOT NULL,
	name character varying(30) NOT NULL,
	email character varying(40) NOT NULL,
	password character(60) NOT NULL,

	PRIMARY KEY (userid)
);

CREATE TABLE Decks (
	deckid serial NOT NULL,
	userid integer NOT NULL,
	deckname character varying(60) NOT NULL,

	PRIMARY KEY (deckid),
	FOREIGN KEY (userid) REFERENCES Users ON DELETE CASCADE
);

CREATE TABLE DeckCards (
	deckid integer NOT NULL,
	cardid integer NOT NULL,
	count smallint NOT NULL,

	PRIMARY KEY (deckid, cardid),
	FOREIGN KEY (deckid) REFERENCES Decks ON DELETE CASCADE,
	FOREIGN KEY (cardid) REFERENCES Cards ON DELETE CASCADE
);

CREATE TABLE Trades (
	tradeid serial NOT NULL,
	tradedate date NOT NULL,
	userid integer NOT NULL,
	active boolean NOT NULL,

	PRIMARY KEY (tradeid),
	FOREIGN KEY (userid) REFERENCES Users ON DELETE CASCADE
);

CREATE TABLE TradeCards (
	tradeid integer NOT NULL,
	cardid integer NOT NULL,
	count smallint NOT NULL,
	direction tradedirection NOT NULL,

	PRIMARY KEY (tradeid, cardid),
	FOREIGN KEY (tradeid) REFERENCES Trades ON DELETE CASCADE,
	FOREIGN KEY (cardid) REFERENCES Cards ON DELETE CASCADE
);

---
--- Views
---

CREATE OR REPLACE VIEW RawDictionary (word, count) AS
SELECT word, count FROM
(
	SELECT regexp_split_to_table(
		lower(
			concat_ws(' ', name, type, subtype, cardtext, flavortext)
		), E'[^a-zA-Z]+'
	) AS word,
	COUNT(*) AS count
	FROM Cards
	GROUP BY word
	ORDER BY count DESC
) AS X
WHERE word != '';

CREATE OR REPLACE VIEW TokenDictionary (token, count) AS
SELECT word AS token, nentry AS count
FROM ts_stat('SELECT textvector FROM CardVectors')
ORDER BY count DESC;

---
--- Indexes
---

CREATE INDEX idx_cardvectors_textvector ON CardVectors USING gin(textvector);
