# s2dr
Secure Shared Data Repository

## Dependencies
### client
* JRE 1.8.0
* gson 2.4, mongodb-driver-core 3.0.4 & mongodb-driver 3.0.4, bson 3.0.4
* openssl command line tool

### server
* Node.js v4.2.2 & npm 3.3.12
* Mongod v3.0.7
* openssl command line tool

## Setup
Clone the repository, obtain CA's private key and serial file. The root directory should look like this:
```
.
├── CA.crt
├── CA.key
├── CA.pub
├── CA.srl
├── README.md
├── client
└── server
```
### client
1. install JRE 1.8.0 or higher
2. set up the workspace by creating a directory ```workspace```
3. obtain the jar libraries.
4. the client directory should look like this:
```
.
├── build.xml
├── lib
│   ├── bson-3.0.4.jar
│   ├── gson-2.4.jar
│   ├── mongodb-driver-3.0.4.jar
│   └── mongodb-driver-core-3.0.4.jar
├── src
│   └── s2dr
│       ├── ca
│       │   ├── CA.java
│       │   └── ProcessUtil.java
│       ├── client
│       │   ├── Channel.java
│       │   ├── ClientCrypto.java
│       │   ├── InsecureClient.java
│       │   ├── InsecureMessage.java
│       │   ├── SecureClient.java
│       │   └── SecureMessage.java
│       └── test
│           ├── Mongo.java
│           └── Test.java
└── workspace
```

### server
1. install Node.js and Mongodb.
2. obtain the private key and the certificate for the server
3. install the Node.js packages via ```npm```: ```cd ./server & npm install```
4. set up the database and document directories
5. the server directory should look like this:
```
.
├── app.js
├── data
├── dbconfig.yaml
├── docs
├── lib
│   ├── cryptoLib.js
│   ├── dbLib.js
│   └── docLib.js
├── node_modules
├── package.json
├── routes
│   ├── checkin.js
│   ├── checkout.js
│   ├── delegate.js
│   ├── delete.js
│   ├── init.js
│   └── terminate.js
├── server.crt
├── server.key
└── server.pub
```

## Test
To perform any test, start the MongoDB daemon by ```mongod -f dbconfig.yaml``` and start the server by ```node app.js```

The test as described in project deliverable can be invoked as ```ant test```

To use the client as a REPL shell, first compile the jar file by ```ant```. Then the shell can be start by ```java -jar s2dr-client.jar```

## Issues
1. should add authentication method to DB
2. information stored in DB should be encrypted
3. should add nonce to messages to prevent attack
