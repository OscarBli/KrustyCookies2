SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Recipe;
DROP TABLE IF EXISTS Ingredient;
DROP TABLE IF EXISTS Cookie;
DROP TABLE IF EXISTS Pallet;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Customers;


CREATE TABLE Cookie (
  cookieName varchar(30) NOT NULL,
  PRIMARY KEY (cookieName)
);

CREATE TABLE Customer (
  customerName varchar(30) NOT NULL,
  address varchar(30) DEFAULT NULL,
  PRIMARY KEY (customerName)
);

CREATE TABLE Ingredient (
  ingredientName varchar(30) NOT NULL,
  amountInStock int(11) DEFAULT NULL,
  unit varchar(30) DEFAULT NULL,
  PRIMARY KEY (ingredientName)
);

CREATE TABLE Recipe (
  cookieName varchar(30) NOT NULL,
  ingredientName varchar(30) NOT NULL,
  amountIngredient int(30) DEFAULT NULL,
  PRIMARY KEY (cookieName,ingredientName),
  KEY ingredientName (ingredientName),
  FOREIGN KEY (cookieName) REFERENCES Cookie (cookieName),
  FOREIGN KEY (ingredientName) REFERENCES Ingredient (ingredientName)
);

CREATE TABLE Orders (
  orderId bigint(30) NOT NULL AUTO_INCREMENT,
  customerName varchar(30) DEFAULT NULL,
  delivered tinyint(4) DEFAULT NULL,
  deliveredDate date DEFAULT NULL,
  PRIMARY KEY (orderId),
  
  KEY customerName (customerName),
  FOREIGN KEY (customerName) REFERENCES Customer (customerName)
);

CREATE TABLE Pallet (
  palletId int(10) NOT NULL AUTO_INCREMENT,
  cookieName varchar(30) DEFAULT NULL,
  createdDate datetime DEFAULT NULL,
  blocked tinyint(1) DEFAULT NULL,
  location varchar(30) DEFAULT NULL,
  orderId bigint(30) DEFAULT NULL,
  PRIMARY KEY (palletId),
  FOREIGN KEY (cookieName) REFERENCES Cookie (cookieName),
  FOREIGN KEY (orderId) REFERENCES Orders (orderId)
);

SET SQL_SAFE_UPDATES = 1;
SET FOREIGN_KEY_CHECKS = 1;
