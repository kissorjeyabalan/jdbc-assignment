CREATE TABLE IF NOT EXISTS Room (
  id int(11) NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  capacity SMALLINT(6) NOT NULL,
  campus VARCHAR(15) NOT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS Lecturer (
  id int(11) NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Available (
  id int(11) NOT NULL AUTO_INCREMENT,
  lecturer int(11) NOT NULL,
  start int(2) NOT NULL,
  end int(2) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (lecturer) REFERENCES Lecturer(id)
);

CREATE TABLE IF NOT EXISTS Contact (
  id int(11) NOT NULL AUTO_INCREMENT,
  lecturer int(11) NOT NULL,
  number VARCHAR(25),
  email VARCHAR(255),
  PRIMARY KEY (id),
  FOREIGN KEY (lecturer) REFERENCES Lecturer(id)
);

CREATE TABLE IF NOT EXISTS Subject (
  id int(11) NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  shortname VARCHAR(255) NOT NULL,
  enrolled SMALLINT(6) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS Subject_Lecturer(
  subject int(11) NOT NULL,
  lecturer int(11) NOT NULL,
  PRIMARY KEY (subject, lecturer),
  FOREIGN KEY (subject) REFERENCES Subject(id),
  FOREIGN KEY (lecturer) REFERENCES Lecturer(id)
);