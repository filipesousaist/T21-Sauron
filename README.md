# Sauron

Distributed Systems 2019-2020, 2nd semester project


## Authors

**Group T21**

### Code identification

In all the source files (including POMs), please replace __CXX__ with your group identifier.  
The group identifier is composed by Campus - A (Alameda) or T (Tagus) - and number - always with two digits.  
This change is important for code dependency management, to make sure that your code runs using the correct components and not someone else's.

### Team members

| Number | Name           | User                                | Email                                           |
| -------|----------------|-------------------------------------| ------------------------------------------------|
| 90714  | Filipe Sousa   | <https://github.com/filipesousaist> | <mailto:filipe.miguel.sousa@tecnico.ulisboa.pt> |
| 90762  | Pedro Vilela   | <https://github.com/pedro19v>       | <mailto:pedro.vilela@tecnico.ulisboa.pt>        |
| 90766  | Pedro Pereira  | <https://github.com/pedro99p>       | <mailto:pedro.l.pereira@tecnico.ulisboa.pt>     |

### Task leaders

| Task set | To-Do                         | Leader              |
| ---------|-------------------------------| --------------------|
| core     | protocol buffers, silo-client | _(whole team)_      |
| T1       | cam_join, cam_info, eye       | _Filipe Sousa_      |
| T2       | report, spotter               | _Pedro Vilela_      |
| T3       | track, trackMatch, trace      | _Pedro Pereira_     |
| T4       | test T1                       | _Pedro Pereira_     |
| T5       | test T2                       | _Filipe Sousa_      |
| T6       | test T3                       | _Pedro Vilela_      |


## Getting Started

The overall system is composed of multiple modules.
The main server is the _silo_.
The clients are the _eye_ and _spotter_.

See the [project statement](https://github.com/tecnico-distsys/Sauron/blob/master/README.md) for a full description of the domain and the system.

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

To confirm that you have them installed, open a terminal and type:

```
javac -version

mvn -version
```

### Installing

To compile and install all modules:

```
mvn clean install -DskipTests
```

The integration tests are skipped because they require the servers to be running.

##Running Instructions

In order to try the application start by running the following command in the /silo-server directory, 
replacing {instance} with a value from 1 to 9 and {gossip_delay} in milliseconds between each time a server
replica asks the other server replicas for updates. {gossip_delay} is optional, the default value is 
30000 milliseconds(30 seconds).
```
./target/appassembler/bin/silo-server localhost 2181 {instance} localhost 808{instance} {gossip_delay}
``` 

If you want to try out the Eye, run the following command in the /eye directory and replace camName, latitude and
 longitude by actual values, and {instance} with a value from 1 to 9. {instance} is optional, and if it is not provided a random instance
 will be selected.
```
./target/appassembler/bin/eye localhost 2181 {eye_name} {latitude} {longitude} {instance}
```

If you want to try out the Spotter, run the following command in the /spotter directory and replace {instance} with 
a value from 1 to 9. {instance} is optional, and if it is not provided a random instance will be selected. 
{cacheSize} is also optional and represents the size that the client cache will have. If it is not provided a default
value is provived. 
```
./target/appassembler/bin/spotter localhost 2181 {instance} {cacheSize}
```

To run the automatic tests run the following command in the root directory, while the server is running:
```
mvn verify
```

We have created a demo that allows to see the application working. See demo/README.md for further instructions.

## Built With

* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework


## Versioning

We use [SemVer](http://semver.org/) for versioning. 
