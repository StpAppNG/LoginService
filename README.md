# Loginserice
Container to authenticate against Campus Dual and return a userspecific hash

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
            "hash" : "12389dskjfln324...."
        }
### curl example
curl -X POST \
  http://localhost:8080/login \
  -H 'content-type: application/json' \
  -d '{
	"username" : "<Your username here>",
	"password" : "<Your Password here>"
}'


