# CS631
Job Portal - Database Systems Course Project

To build a scalable solution for an online job portal that scales with the growing number
of applicants and recruiters


## Setup

### Web-Server Setup

* Install Java Development Kit from [here](https://docs.oracle.com/en/java/javase/18/install/overview-jdk-installation.html)
* Install Apache Maven from [here](https://maven.apache.org/install.html)
* Install Apache Tomcat from [here](https://tomcat.apache.org/tomcat-8.5-doc/setup.html)

#### Building the web-server

Run this command from the JobPortal directory
```bash
mvn clean package
```
  
#### Running the web-server
 
Run this command from the JobPortal directory
```bash
mvn tomcat7:deploy
```

### Database setup

To make the connection between the web-server and SQL database, we need to do the below:
1. Install MySQL server version 8.0.33 from [here](https://dev.mysql.com/downloads/mysql/)
2. Install MySQL workbench from [here](https://dev.mysql.com/downloads/workbench/)
3. Set environment variables to access the database
  ```sh
      # For unix systems
      export DB_USER='root'
      export DB_PASS='password'   
      # For windows systems
      $env:DB_USER = 'root'
      $env:DB_USER = 'password'
  ```    

#### Database Schema

Create a database with name `jobapplication`

Create the `Profile` table using following command

```sql
CREATE TABLE `jobapplication`.`Profile` (
  `ProfileID` INT NOT NULL AUTO_INCREMENT,
  `FullName` VARCHAR(50) NOT NULL,
  `DOB` DATE NULL,
  `Address` VARCHAR(45) NULL,
  `Phone` VARCHAR(13) NULL,
  `Degree` VARCHAR(50) NOT NULL,
  `University` VARCHAR(50) NOT NULL,
  `YOE` INT NULL,
  `Skills` VARCHAR(500) DEFAULT '[]',
  PRIMARY KEY(`ProfileID`, `Degree`));
```

Create table partitions using following command

Make three partitions of the table based on values in Degree (underGraduate, postGraduate, PhD)

```sql
ALTER TABLE Profile PARTITION BY LIST COLUMNS (Degree) ( 
PARTITION p1 VALUES IN ('underGraduate'), 
PARTITION p2 VALUES IN ('postGraduate', 'MS'), 
PARTITION p3 VALUES IN ('PhD') );
```

