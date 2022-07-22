-- Tables 

-- TODO: contraints

CREATE TABLE Listings (
    ID INT AUTO_INCREMENT,
    type VARCHAR(10) NOT NULL,            -- putting constraint for options in sql or code?
    latitude DECIMAL(10, 8),            -- https://yeahexp.com/data-type-for-latitude-and-longitude/
    longitude DECIMAL(11, 8),               -- allowing NULLs
    PRIMARY KEY(ID)
);

CREATE TABLE RatedOnUser (
    ID INT AUTO_INCREMENT,
    User1.SIN INT,
    User2.SIN INT,               -- SIN regex
    score INT,                   -- constrain : 0 - 10
    PRIMARY KEY(ID)
);

CREATE TABLE RatedOnListing (
    ID INT AUTO_INCREMENT,              -- allows multiple comments from the same renter
    Listing.ID INT,
    User.SIN INT,
    score INT,
    PRIMARY KEY(ID)
);

CREATE TABLE Period (
    ID INT AUTO_INCREMENT,   
    start DATE NOT NULL,
    end DATE NOT NULL,
    PRIMARY KEY(ID)
);

CREATE TABLE CommentsOnUser (
    ID INT AUTO_INCREMENT,   
    User1.SIN INT,
    User2.SIN INT,
    text TEXT,
    PRIMARY KEY(ID)
);

CREATE TABLE CommentsOnListing (
    ID INT AUTO_INCREMENT,   
    Listing.ID INT,
    User.SIN INT,
    text TEXT NOT NULL,
    PRIMARY KEY(ID)
);

CREATE TABLE Amenities (
    type TEXT,                   -- Constrain for types?
    PRIMARY KEY(type)
);

CREATE TABLE City (
    name TEXT,
    PRIMARY KEY(name)
);

CREATE TABLE Country (
    name TEXT,
    PRIMARY KEY(name)
);

CREATE TABLE Address (
    postal_code Varchar(12), 
    street Text,
    PRIMARY_KEY(postal_code)
);

CREATE TABLE Renter (
    SIN INT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    birthdate DATE,
    occupation TEXT,
    email TEXT UNIQUE NOT NULL,      -- regex
    password TEXT NOT NULL,          -- regex
    credit_card INT(19) UNIQUE NOT NULL,
    PRIMARY KEY(SIN)
);

CREATE TABLE ResidesIn (
    Address.postal_code TEXT,
    User.SIN INT,
    PRIMARY KEY(User.SIN)
);

CREATE TABLE Host (
    SIN INT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    birthdate DATE,
    occupation TEXT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,             -- regex
    PRIMARY KEY(SIN)
);

CREATE TABLE BelongsTo (
    City.name TEXT,
    Country.name  TEXT,
    PRIMARY KEY(City.name, Country.name)
);

CREATE TABLE IsIn (
    Address.postal_code VARCHAR(12),
    City.name TEXT,
    PRIMARY KEY(Address.postal_code, City.name)
);

CREATE TABLE Has (
    Amenities.type TEXT,
    Listing.ID INT,
    PRIMARY KEY(Amenities.type, Listing.ID)
);

CREATE TABLE LocatedIn (
    Address.postal_code VARCHAR(12),
    Listing.ID INT,
    PRIMARY KEY(Listing.ID)
);


CREATE TABLE AvailableIn (
    Listing.ID INT,
    Period.ID INT,
    price INT NOT NULL,
    PRIMARY KEY(Period.ID, Listing.ID)
);

CREATE TABLE Owns (
    Host.SIN INT,
    Listing.ID INT,
    PRIMARY KEY(Host.SIN, Listing.ID)
);

CREATE TABLE Books (
    Booking_ID INT AUTO_INCREMENT,      -- just in case, but may not be neccesarily
    Renter.SIN INT,
    Listing.ID INT,
    start INT,
    end INT,
    isReserved BOOL,              -- may not be needed
    PRIMARY KEY(Booking_ID)
);
