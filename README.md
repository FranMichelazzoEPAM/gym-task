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

