```json
{
  "info": {
    "_postman_id": "a7e74140-ec97-4cca-9f9b-9c1f9ad2a001",
    "name": "Fijación de sesión",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "01 crear sesión previa",
      "request": {
        "method": "POST",
        "header": [],
        "body": {
          "mode": "urlencoded",
          "urlencoded": [
            { "key": "sessionId", "value": "demo-fijacion", "type": "text" }
          ]
        },
        "url": {
          "raw": "http://localhost:5173/api/auth/fijar-sesion?sessionId=demo-fijacion",
          "protocol": "http",
          "host": ["localhost"],
          "port": "5173",
          "path": ["api", "auth", "fijar-sesion"],
          "query": [
            { "key": "sessionId", "value": "demo-fijacion", "type": "text" }
          ]
        }
      },
      "response": []
    },
    {
      "name": "02 login con sesión previa",
      "request": {
        "method": "POST",
        "header": [
          { "key": "Content-Type", "value": "application/json" }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"admin\",\n  \"password\": \"admin\"\n}",
          "options": { "raw": { "language": "json" } }
        },
        "url": {
          "raw": "http://localhost:5173/api/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "5173",
          "path": ["api", "auth", "login"]
        }
      },
      "response": []
    },
    {
      "name": "03 comprobar sesión autenticada",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:5173/api/auth/yo",
          "protocol": "http",
          "host": ["localhost"],
          "port": "5173",
          "path": ["api", "auth", "yo"]
        }
      },
      "response": []
    }
  ]
}
```
