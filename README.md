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
  * Create multiple Mysql servers, in this example we are creating two more instances of MySQL server
  ```sh
sudo bash -c 'cat <<EOF > /etc/mysql/conf.d/mysql2.cnf
[mysql]
[mysqld]
user=mysql
datadir=/var/lib/mysql2
socket=/var/run/mysqld/mysqld2.sock
port=3307
log-error=/var/log/mysql/mysql2.error.log
pid-file=/var/run/mysqld/mysqld2.pid
EOF'

sudo bash -c 'cat <<EOF > /etc/mysql/conf.d/mysql3.cnf
[mysql]
[mysqld]
user=mysql
datadir=/var/lib/mysql3
socket=/var/run/mysqld/mysqld3.sock
port=3308
log-error=/var/log/mysql/mysql3.error.log
pid-file=/var/run/mysqld/mysqld3.pid
EOF'

sudo mkdir /var/lib/mysql2
sudo mkdir /var/lib/mysql3

sudo chown -R mysql:mysql /var/lib/mysql2
sudo chmod 750 /var/lib/mysql2
sudo chown -R mysql:mysql /var/lib/mysql3
sudo chmod 750 /var/lib/mysql3

# Setup the server data directories
sudo mysqld --initialize-insecure --datadir=/var/lib/mysql2 --defaults-file=/etc/mysql/conf.d/mysql2.cnf
sudo mysqld --initialize-insecure --datadir=/var/lib/mysql3 --defaults-file=/etc/mysql/conf.d/mysql3.cnf
sudo mysql_install_db --user=mysql --ldata=/var/lib/mysql2
sudo mysql_install_db --user=mysql --ldata=/var/lib/mysql3

# Start the servers
sudo mysqld --defaults-file=/etc/mysql/conf.d/mysql2.cnf --user=mysql
sudo mysqld --defaults-file=/etc/mysql/conf.d/mysql3.cnf --user=mysql

# To connect the specific instances
sudo mysql -u root -p --socket=/var/run/mysqld/mysqld.sock
sudo mysql -u root -p --socket=/var/run/mysqld/mysqld2.sock
sudo mysql -u root -p --socket=/var/run/mysqld/mysqld3.sock

```
  * Setup a user that can be used for the application
  ```sh
  # Login with default user=root password=root on first instance
  sudo mysql -u myuser -p --socket=/var/run/mysqld/mysqld.sock
  
  CREATE USER 'myuser'@'localhost' IDENTIFIED BY 'mypassword';
  GRANT ALL PRIVILEGES ON *.* TO 'myuser'@'localhost';
  FLUSH PRIVILEGES;
  
  # Repeat the above commands for rest of the instances
  ```
  * Disable query caching
  ```sh
  # Repeat the following commands by connecting to mysql prompt for all instances
  SHOW VARIABLES LIKE 'query_cache_type';
  SET GLOBAL query_cache_type = OFF;
  ```
3. Set environment variables, to access the database, for Tomcat server
  ```sh
  # Create and update the Tomcat setenev.sh(Unix) or setenv.bat(Windows) script 
  sudo sh -c 'echo "export DB_USER=myuser" >> $CATALINA_HOME/bin/setenv.sh'
  sudo sh -c 'echo "export DB_PASS=mypassword" >> $CATALINA_HOME/bin/setenv.sh"' 
  
  # Restart the Tomcat server
  sudo systemctl restart tomcat9
  ```

#### Database Schema

Login into Mysql server console using your user
```sh
mysql -u myuser -p
```
Create a database with name `jobapplication`
```sql
CREATE DATABASE jobapplication;
```
Create the `profile` table using following command

```sql
USE jobapplication;

CREATE TABLE `jobapplication`.`profile` (
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

