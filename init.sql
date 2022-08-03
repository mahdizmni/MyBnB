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
    type VARCHAR(10) NOT NULL,            -- putting constraint for options in sql or code?
    PRIMARY KEY(type)
)

CREATE TABLE ListingsType (
    ID INT AUTO_INCREMENT,
    type VARCHAR(10) NOT NULL,            -- putting constraint for options in sql or code?
    FOREIGN KEY(ID) REFERENCES Listings(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY(type) REFERENCES Type(type)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
)

CREATE TABLE Listings (
    ID INT AUTO_INCREMENT,
    latitude DECIMAL(10, 8),            -- https://yeahexp.com/data-type-for-latitude-and-longitude/
    longitude DECIMAL(11, 8),               -- allowing NULLs
    PRIMARY KEY(ID)
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

CREATE TABLE ResidesIn (
    Address_postalcode varchar(12),
    User_SIN INT,
    PRIMARY KEY(User_SIN),

    FOREIGN KEY (Address_postalcode) REFERENCES Address(postalcode)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (User_SIN) REFERENCES User(SIN)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE BelongsTo (
    City_name varchar(20),
    Country_name varchar(20),
    PRIMARY KEY(City_name, Country_name),

    FOREIGN KEY (City_name) REFERENCES City(name)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (Country_name) REFERENCES Country(name)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IsIn (
    Address_postalcode VARCHAR(12), 
    City_name varchar(20),
    PRIMARY KEY(Address_postalcode, City_name),

    FOREIGN KEY (Address_postalcode) REFERENCES Address(postalcode)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (City_name) REFERENCES City(name)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Has (
    Amenities_type varchar(20),
    Listing_ID INT,
    PRIMARY KEY(Amenities_type, Listing_ID),

    FOREIGN KEY (Listing_ID) REFERENCES Listings(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (Amenities_type) REFERENCES Amenities(type)
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
    price INT NOT NULL,
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
    BookingID INT AUTO_INCREMENT,      -- just in case, but may not be neccesarily
    Renter_SIN INT,
    Listing_ID INT,
    start INT,
    end INT,
    isReserved BOOL,              -- may not be needed
    Canceled BOOL,
    PRIMARY KEY(BookingID),
    
    FOREIGN KEY (Renter_SIN) REFERENCES User(SIN)
        ON DELETE SET NULL 
        ON UPDATE CASCADE,

    FOREIGN KEY (Listing_ID) REFERENCES Listings(ID)
        ON DELETE SET NULL 
        ON UPDATE CASCADE
);
-- End
