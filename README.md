# Loginserice
Container to authenticate against Campus Dual and return userspecific content

## usage
POST http(s)://Container:8080/login

BODY:
    {
        "username" : "username",
        "password" : "password"
    }

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
curl -X POST \
  http://localhost:8080/login \
  -H 'content-type: application/json' \
  -d '{
	"username" : "<Your username here>",
	"password" : "<Your Password here>"
}'


