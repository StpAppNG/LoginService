# Loginserice
Container to authenticate against Campus Dual and return userspecific content

## deployment
``
docker run -d --rm --name loginservice -p 8080:8080 loginservice
``
## usage
GET http(s)://Container:8080/login

RESPONSE:
    
    401: unauthorized
    
    400: username or password are not given
    
    200:
        {
            "course": "course",
            "forename": "Max",
            "group": "6HT13-1",
            "hash": "aff1afdfa1f23df....",
            "surename": "Mustermann",
            "university": "Staatliche Studienakademie Leipzig"
        }
### curl example
``
curl -X GET http://localhost:8080/login --user <Your Username>
``


