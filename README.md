# CS631
Job Portal - Database Systems Course Project

To build a scalable solution for an online job portal that scales with the growing number
of applicants and recruiters


## Web-Server Setup

* Install Java Development Kit from [here](https://docs.oracle.com/en/java/javase/18/install/overview-jdk-installation.html)
* Install Apache Maven from [here](https://maven.apache.org/install.html)
* Install Apache Tomcat from [here](https://tomcat.apache.org/tomcat-8.5-doc/setup.html)

### Building the web-server

Run this command from the JobPortal directory
```bash
mvn clean package
```
  
### Running the web-serverr
 
Run this command from the JobPortal directory
```bash
mvn clean tomcat7:deploy
```


## Database setup

To make the connection between the web-server and SQL database, we need to do the below:
1. Download MySQL server version 8.0.33 from [here](https://dev.mysql.com/downloads/mysql/)
2. Download MySQL workbench from [here](https://dev.mysql.com/downloads/workbench/)
3. JDBC driver using below link

## Database Schema

Create a table named Profile with columns as follows:
1. ProfileID - Integer (Primary Key)
2. FullName - Varchar(40)
3. DOB - Date
4. Address - Varchar(45)
5. Phone - Varchar(13)
6. Degree - Varchar(45) (Primary Key)
7. University - Varchar(45)
8. YOE - Integer

Make three partitions of the table based on values in Degree (underGraduate, postGraduate, PhD) 
*PARTITION BY LIST COLUMNS(Degree)*


