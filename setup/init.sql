-- Tables 

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

CREATE TABLE Type (
    ID INT AUTO_INCREMENT,
    type VARCHAR(10) UNIQUE NOT NULL,            -- putting constraint for options in sql or code?

    PRIMARY KEY(ID)
);

CREATE TABLE Listings (
    ID INT AUTO_INCREMENT,
    latitude DECIMAL(10, 8),            -- https://yeahexp.com/data-type-for-latitude-and-longitude/
    longitude DECIMAL(11, 8),               -- allowing NULLs
    PRIMARY KEY(ID)
);

CREATE TABLE ListingsType (
    Listings_ID INT,
    type_ID INT,            -- putting constraint for options in sql or code?

    PRIMARY KEY(Listings_ID, type_ID),

    FOREIGN KEY(Listings_ID) REFERENCES Listings(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY(type_ID) REFERENCES Type(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE RatedOnUser (
    ID INT AUTO_INCREMENT,
    User1_SIN INT,
    User2_SIN INT,
    score INT,                   -- constrain : 0 - 10
    PRIMARY KEY(ID),

    FOREIGN KEY(User1_SIN) REFERENCES User(SIN)
        ON DELETE SET NULL 
        ON UPDATE CASCADE,

    FOREIGN KEY(User2_SIN) REFERENCES User(SIN)
        ON DELETE SET NULL 
        ON UPDATE CASCADE

);

CREATE TABLE RatedOnListing (
    ID INT AUTO_INCREMENT,              
    Listing_ID INT,
    User_SIN INT,
    score INT,
    PRIMARY KEY(ID),

    FOREIGN KEY(Listing_ID) REFERENCES Listings(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY(User_SIN) REFERENCES User(SIN)
        ON DELETE SET NULL 
        ON UPDATE CASCADE
);

CREATE TABLE Period (
    ID INT AUTO_INCREMENT,   
    start DATE NOT NULL,
    end DATE NOT NULL,
    PRIMARY KEY(ID)
);

CREATE TABLE CommentsOnUser (
    ID INT AUTO_INCREMENT,   
    User1_SIN INT,
    User2_SIN INT,
    text TEXT,
    PRIMARY KEY(ID),

    FOREIGN KEY(User1_SIN) REFERENCES User(SIN)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY(User2_SIN) REFERENCES User(SIN)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE CommentsOnListing (
    ID INT AUTO_INCREMENT,   
    Listing_ID INT,
    User_SIN INT,
    text TEXT NOT NULL,
    PRIMARY KEY(ID),

    FOREIGN KEY(Listing_ID) REFERENCES Listings(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY(User_SIN) REFERENCES User(SIN)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE Amenities (
    ID INT AUTO_INCREMENT,
    type VARCHAR(20) UNIQUE NOT NULL,                   -- Constrain for length
    PRIMARY KEY(ID)
);

CREATE TABLE City (
    ID INT AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL UNIQUE,                   -- Constrain for length 
    PRIMARY KEY(ID)
);

CREATE TABLE Country (
    ID INT AUTO_INCREMENT,
    name VARCHAR(20) UNIQUE NOT NULL,                   -- Constrain for length 
    PRIMARY KEY(ID)
);

CREATE TABLE Address (
    ID INT AUTO_INCREMENT,
    postalcode Varchar(12), 
    street Text,
    num INT,
    
    UNIQUE (postalcode, num),
    PRIMARY KEY(ID)
);

CREATE TABLE ResidesIn (
    Address_ID INT,
    User_SIN INT,
    PRIMARY KEY(User_SIN),

    FOREIGN KEY (Address_ID) REFERENCES Address(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (User_SIN) REFERENCES User(SIN)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE BelongsTo (
    City_ID INT,
    Country_ID INT,
    PRIMARY KEY(City_ID, Country_ID),

    FOREIGN KEY (City_ID) REFERENCES City(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (Country_ID) REFERENCES Country(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IsIn (                 
    Address_ID INT, 
    City_ID INT,
    PRIMARY KEY(Address_ID, City_ID),

    FOREIGN KEY (Address_ID) REFERENCES Address(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (City_ID) REFERENCES City(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Has (
    Amenities_ID INT,
    Listing_ID INT,
    PRIMARY KEY(Amenities_ID, Listing_ID),

    FOREIGN KEY (Listing_ID) REFERENCES Listings(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (Amenities_ID) REFERENCES Amenities(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE LocatedIn (
    Address_postalcode VARCHAR(12),
    Listing_ID INT,
    PRIMARY KEY(Listing_ID),

    FOREIGN KEY (Address_postalcode) REFERENCES Address(postalcode)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (Listing_ID) REFERENCES Listings(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE AvailableIn (
    Listing_ID INT,
    Period_ID INT,
    price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY(Period_ID, Listing_ID),

    FOREIGN KEY (Listing_ID) REFERENCES Listings(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (Period_ID) REFERENCES Period(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE

);

CREATE TABLE Owns (
    Host_SIN INT,
    Listing_ID INT,
    PRIMARY KEY(Listing_ID),

    FOREIGN KEY (Host_SIN) REFERENCES User(SIN) 
        ON DELETE CASCADE                  -- deletes the history of ownerships
        ON UPDATE CASCADE,
    FOREIGN KEY (Listing_ID) REFERENCES Listings(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Books (
    BookingID INT AUTO_INCREMENT,      -- just in case, but may not be necessary
    Renter_SIN INT,
    Listing_ID INT,
    start DATE NOT NULL,
    end DATE NOT NULL,
    isReserved BOOL,
    PRIMARY KEY(BookingID),
    
    FOREIGN KEY (Renter_SIN) REFERENCES User(SIN)
        ON DELETE SET NULL 
        ON UPDATE CASCADE,

    FOREIGN KEY (Listing_ID) REFERENCES Listings(ID)
        ON DELETE SET NULL 
        ON UPDATE CASCADE
);
-- End