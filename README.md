# s2dr
Secure Shared Data Repository

## server setup
1. install Node.js and Mongodb.
2. clone the repository and obtain CA's private key.
3. install the Node.js packages via ```npm```: ```cd ./server & npm install```
4. start the Mongodb server: ```mongod -f dbconfig.yaml```
5. start server: ```node app.js```
