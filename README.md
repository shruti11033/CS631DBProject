# CS631
Database System Course Project

To build a scalable solution for an online job portal that scales with the growing number
of applicants and recruiters


## MySQL -Julia
To make the connection between the client program and SQL, we need to do the below:
1. Download and install MySQL server version 8.0.33 using the below link 
Link: [](https://dev.mysql.com/downloads/mysql/)
2. Download and install MySQL workbench
Link: [](https://dev.mysql.com/downloads/workbench/)

## Table structure:
Creating a table named Profile with columns as follows:
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


