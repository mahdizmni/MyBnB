-- Tables 

CREATE TABLE Listings (
    ID INT AUTO_INCREMENT,
    type VARCHAR(10) NOT NULL,            -- putting constraint for options in sql or code?
    latitude DECIMAL(10, 8),            -- https://yeahexp.com/data-type-for-latitude-and-longitude/
    longitude DECIMAL(11, 8),               -- allowing NULLs
    PRIMARY KEY(ID)
);

CREATE TABLE RatedOnUser (
    ID INT AUTO_INCREMENT,
    User1_SIN INT REFERENCES User(SIN),              -- we keep the comments of users who deleted account
    User2_SIN INT REFERENCES User(SIN),               -- SIN regex
    score INT,                   -- constrain : 0 - 10
    PRIMARY KEY(ID)
);

CREATE TABLE RatedOnListing (
    ID INT AUTO_INCREMENT,              -- allows multiple comments from the same renter
    Listing_ID INT REFERENCES Listings(ID),
    User_SIN INT REFERENCES RENTER(SIN),
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
    User1_SIN INT REFERENCES User(SIN),
    User2_SIN INT REFERENCES User(SIN),
    text TEXT,
    PRIMARY KEY(ID)
);

CREATE TABLE CommentsOnListing (
    ID INT AUTO_INCREMENT,   
    Listing_ID INT REFERENCES Listings(ID),
    User_SIN INT REFERENCES Renter(SIN),
    text TEXT NOT NULL,
    PRIMARY KEY(ID)
);

CREATE TABLE Amenities (
    type VARCHAR(20),                   -- Constrain for length 
    PRIMARY KEY(type)
);

CREATE TABLE City (
    name VARCHAR(20),                   -- Constrain for length 
    PRIMARY KEY(name)
);

CREATE TABLE Country (
    name VARCHAR(20),                   -- Constrain for length 
    PRIMARY KEY(name)
);

CREATE TABLE Address (
    postalcode Varchar(12), 
    street Text,
    PRIMARY KEY(postalcode)
);

CREATE TABLE User (
    SIN INT,
    firstname TEXT NOT NULL,
    lastname TEXT NOT NULL,
    birthdate DATE,
    occupation TEXT,
    email varchar(255) UNIQUE NOT NULL,      -- regex
    password TEXT NOT NULL,          -- regex
    creditcard varchar(19) UNIQUE NOT NULL,
    PRIMARY KEY(SIN)
);

CREATE TABLE ResidesIn (
    Address_postalcode varchar(12) REFERENCES Address(postalcode),
    User_SIN INT REFERENCES User(SIN),
    PRIMARY KEY(User_SIN)
);

CREATE TABLE BelongsTo (
    City_name varchar(20) REFERENCES City(name),
    Country_name varchar(20) REFERENCES Country(name),
    PRIMARY KEY(City_name, Country_name)
);

CREATE TABLE IsIn (
    Address_postalcode VARCHAR(12) REFERENCES Address(postalcode),
    City_name varchar(20) REFERENCES City(name),
    PRIMARY KEY(Address_postalcode, City_name)
);

CREATE TABLE Has (
    Amenities_type varchar(20) REFERENCES Amenities(type),
    Listing_ID INT REFERENCES Listings(ID),
    PRIMARY KEY(Amenities_type, Listing_ID)
);

CREATE TABLE LocatedIn (
    Address_postal_code VARCHAR(12) REFERENCES Address(postalcode),
    Listing_ID INT REFERENCES Listings(ID),
    PRIMARY KEY(Listing_ID)
);


CREATE TABLE AvailableIn (
    Listing_ID INT REFERENCES Listings(ID),
    Period_ID INT REFERENCES Period(ID),
    price INT NOT NULL,
    PRIMARY KEY(Period_ID, Listing_ID)
);

CREATE TABLE Owns (
    Host_SIN INT  REFERENCES User(SIN),
    Listing_ID INT REFERENCES Listings(ID),
    PRIMARY KEY(Host_SIN, Listing_ID)
);

CREATE TABLE Books (
    BookingID INT AUTO_INCREMENT,      -- just in case, but may not be neccesarily
    Renter_SIN INT REFERENCES User(SIN),
    Listing_ID INT REFERENCES Listings(ID),
    start INT,
    end INT,
    isReserved BOOL,              -- may not be needed
    PRIMARY KEY(BookingID)
);
