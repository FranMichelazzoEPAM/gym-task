# gym-task
This repo is a simple implementation of a gym management system using Spring framework. It includes services for managing trainees, trainers, and training sessions.

The idea of this repository is that it serves to keep proper track of the changes made in the application.

## Branching strategy
My intention is to always have a stable branch (main) that contains the testable versions of the gym-task,
so the development of new deliverable tasks will be done in new branches,
which, when ready, will be merged into main.

## Tags
The last commit of a delivery (after merging main with feature) 
will be tagged so you can quickly navigate to it. 
After tagging the commit, the feature branch will be deleted.

## Notes for task-3

---
### Authentication
In this project, I'm using simple validation for the authentication requirement.

It would have been better to use JWT, since after logging in, subsequent requests wouldn't need to send the user's password anymore—only a token.
However, I realized this quite late (when my mentor told me), so by that point, almost all of my methods already required the caller's credentials and validated them against the database. (Oops!)

At this point, it's quite late to change it, because the core logic of my aspect no longer makes sense as-is.
I would need to rewrite the security layer: add token generation, implement a filter to validate the tokens and extract 
the username, and change the aspect to trust that the token is valid instead of checking the password against the database.

That would also mean changing the GymFacade method signatures and removing callerPassword from every method, 
which would be quite a headache at this stage.

Sorry!

## Notes for Spring Boot task

---
### Database connectivity
For dev and local profile, I didn't change the H2 database configuration.

For the prod and stg profile, I used a MySQL database. In a real production environment, 
it should be created and configured properly, but in this task I did this in the mysql command line
, so the application can connect to it. Here are the commands I used:

```
CREATE DATABASE gymdb;
CREATE USER 'gymuser'@'localhost' IDENTIFIED BY 'gympass123';
GRANT ALL PRIVILEGES ON gymdb.* TO 'gymuser'@'localhost';
FLUSH PRIVILEGES;
```

The database is created manually, and the application is configured to connect to it using the `application-prod.properties` file.
If you run the application with the `prod` profile, it will connect to this MySQL database
 that you created.
```
PS C:\Users\FranciscoMichelazzo> mysql -u gymuser -pgympass123 gymdb -e "SHOW TABLES;"
mysql: [Warning] Using a password on the command line interface can be insecure.
+------------------+
| Tables_in_gymdb  |
+------------------+
| trainee_trainers |
| trainees         |
| trainers         |
| training_types   |
| trainings        |
| users            |
+------------------+

```
And if you want to see the contents of the `training_types` table, you can run the following command:
```
PS C:\Users\FranciscoMichelazzo> mysql -u gymuser -pgympass123 gymdb -e "SELECT * FROM training_types;"
mysql: [Warning] Using a password on the command line interface can be insecure.
PS C:\Users\FranciscoMichelazzo>
```
Notice that the table is empty, as in a production environment, when you initialize the app,
it shouldn't drop the tables and reinsert the data, but rather use the existing data in the database.

That is my reasoning for not using the `data.sql` file.
I hope this completes the `Implement support for different environments (local, dev, stg, prod). Use Spring profiles.`
 task and the `Pay attention that each environment - different db properties.` note.
