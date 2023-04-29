# CS631
Job Portal - Database Systems Course Project

To build a scalable solution for an online job portal that scales with the growing number
of applicants and recruiters


## Setup

### Web-Server Setup

* Install Java Development Kit from [here](https://docs.oracle.com/en/java/javase/18/install/overview-jdk-installation.html)
  * Ensure that you have set `JAVA_HOME` environment variable as path to JDK installation dir, and added `$JAVA_HOME/bin` to `$PATH`
* Install Apache Maven from [here](https://maven.apache.org/install.html)
  * Ensure that you have set `M2_HOME` environment variable as path to Maven installation dir, and added `$M2_HOME/bin` to `$PATH`  
* Install Apache Tomcat from [here](https://tomcat.apache.org/tomcat-8.5-doc/setup.html)
  * Ensure that you have set the `CATALINA_HOME` environment variable as path to Tomcat installation dir.
  * Start the Tomcat server, by default it should run on port 8080, you can verify this by visiting the URL `http://localhost:8080`
* Add users to managing Tomcat server from UI and scripts
  ```sh
  # Open tomcat users config file
  sudo vi /etc/tomcat9/tomcat-users.xml
  
  # Add these lines for new users, replace the username and password as needed
  <user username="gui_admin" password="password" roles="manager-gui"/>
  <user username="script_admin" password="password" roles="manager-script"/>
  ```
* Add Tomcat user to Maven settings, this is needed so that Maven can deploy the war file to the Tomcat server.
  ```sh
  # Open Maven settings file, create one if it does not exist
  sudo vi ~/.m2/settings.xml
  
  # Add the Tomcat user with manager-scripts role to this file as follows
  <settings>
    <servers>
      <server>
        <id>TomcatServer</id>
        <username>script_admin</username>
        <password>password</password>
      </server>
    </servers>
  </settings>
  ```

#### Building the web-server

Run this command from the JobPortal directory to compile the web-server code and generate the .war archive file.
```bash
mvn clean install
```
  
#### Running the web-server
 
Run this command from the JobPortal directory to deploy the generated .war archive file on the Tomcat server.
```bash
mvn tomcat7:deploy
```

You can verify this works by visting the URL `http://localhost:8080/job-portal/`


### Database setup

To make the connection between the web-server and SQL database, we need to do the below:
1. Install MySQL server from [here](https://dev.mysql.com/downloads/mysql/)
2. Install MySQL workbench from [here](https://dev.mysql.com/downloads/workbench/)
3. Set environment variables to access the database
  ```sh
      # For unix systems
      sudo sh -c 'echo "DB_USER=root" >> /etc/environment'
      sudo sh -c 'echo "DB_PASS=password" >> /etc/environment'
      source /etc/environment   
      # For windows systems
      $env:DB_USER = 'root'
      $env:DB_USER = 'password'
  ```

#### Database Schema

Login into Mysql server console using default root user and password
```sh
mysql -u root -p
```
Create a database with name `jobapplication`
```sql
CREATE DATABASE jobapplication;
```
Create the `Profile` table using following command

```sql
USE jobapplication;

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

DESCRIBE Profile;
```

Create table partitions using following command

Make three partitions of the table based on values in Degree (underGraduate, postGraduate, PhD)

```sql
ALTER TABLE Profile PARTITION BY LIST COLUMNS (Degree) ( 
PARTITION p1 VALUES IN ('underGraduate'), 
PARTITION p2 VALUES IN ('postGraduate', 'MS'), 
PARTITION p3 VALUES IN ('PhD') );
```

