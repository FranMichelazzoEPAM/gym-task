Hi Rafael, here are some explanations regarding the endpoints:

For the trianer workload on a specific month (this was implemented before):
http://localhost:8080/api/trainers/{username}/workload/{year}/{month}

or if you want to access the complete document (new):
http://localhost:8080/api/trainers/{username}/workload

both are endpoints in port 8080 (gym crm) that are redirected to port 8081 (workload service)

### ActiveMQ
Remember you have to run ActiveMQ manually.