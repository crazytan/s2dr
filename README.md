# s2dr
Secure Shared Data Repository

## Dependencies
### client
* jre 1.8.0
* gson 2.4
* mongodb-driver-core 3.0.4 & mongodb-driver 3.0.4
* bson 3.0.4
* openssl command line tool

### server
* Node.js v5.1.0 & npm 3.3.12
* Mongod v3.0.7
* openssl command line tool

## Server setup
1. install Node.js and Mongodb.
2. clone the repository and obtain private keys for CA and server.
3. install the Node.js packages via ```npm```: ```cd ./server & npm install```
4. start the Mongodb server: ```mongod -f dbconfig.yaml```
5. start server: ```node app.js```
