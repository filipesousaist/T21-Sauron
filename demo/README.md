## Manual Tests

### Preparation

* Before running these tests, make sure the project files are installed and the server is running, by executing the following commands in the terminal (Linux), in the project root:
```
mvn clean install -DskipTests
./silo-server/target/appassembler/bin/silo-server 8080
```
* Note: You only need to run these commands once, before all tests.


---

### Running the tests
* The tests should be run in the presented order.

---

#### Test 1: Eye demonstration
* Run the following commands in the terminal, in the project root:
```
./eye/target/appassembler/bin/eye localhost 8080 Cam1 45 45 < demo/eyeDemo.txt
```
* Expected output:
```
Registration successful. Proceeding...
End
```

* Note that the test takes some time, indicating that the "zzz" command executed successfully.

---
#### Test 2: Eye invalid commands
* Run the following commands in the terminal, in the project root:
```
./eye/target/appassembler/bin/eye localhost 8080 Cam2 45 45 < demo/eyeInvalid.txt
```
* Expected output:
```
Registration successful. Proceeding...
Error: INVALID_ARGUMENT: Person ID does not match the specification
Error: INVALID_ARGUMENT: Car ID does not match the specification
Invalid line: Unknown command: airplane
Invalid line: Wrong number of arguments. Expected 2, but 1 were given.
End
```

---


#### Test 3: Eye invalid join
* Run the following commands in the terminal, in the project root:
```
./eye/target/appassembler/bin/eye localhost 8080 Tagus 45 45 < demo/emptyFile.txt
./eye/target/appassembler/bin/eye localhost 8080 Tagus -45 80 < demo/emptyFile.txt
```
* Expected output (1st command):
```
Registration successful. Proceeding...
End
```
* Expected output (2nd command):
```
Error registering in server: An Eye already exists with same name, but different coordinates.
End
```
---

#### Test 4: Spotter demonstration
* Run the following commands in the terminal, in the project root:
```
./spotter/target/appassembler/bin/spotter localhost 8080 < demo/spotterDemo.txt
```
* Expected output:
```
Supported commands:
spot <type> <id> (returns observation(s) of <type> with <id> or partial <id>)
trail <type> <id> (returns path taken by <type> with <id>)
ping (sends control message to server, and server sends feedback)
clear (clears server state)
init (allows definition of initial configuration parameters of server)
exit (exits Spotter)
Observations added.
Observations added.
Observations added.
123456,PERSON,2020-04-03T15:13:16,Tagus,38.737613,-9.303164
AA43BY,CAR,2020-04-03T15:13:16,Alameda,38.736748,-9.138908
LD04BY,CAR,2020-04-03T15:13:16,Alameda,38.736748,-9.138908
123456,PERSON,2020-04-03T15:13:16,Tagus,38.737613,-9.303164
12344321,PERSON,2020-04-03T15:13:16,Alameda,38.736748,-9.138908
AA00BB,CAR,2020-04-03T15:13:16,Tagus,38.737613,-9.303164
AA00BB,CAR,2020-04-03T15:13:16,Tagus,38.737613,-9.303164
AA00BB,CAR,2020-04-03T15:13:16,Tagus,38.737613,-9.303164
Server has been cleared.
Hello, ABC!
```
* Note: The command output will not be exactly the same as above, as the time will differ. 
---

#### Test 5: Spotter invalid commands
* Run the following commands in the terminal, in the project root:
```
./spotter/target/appassembler/bin/spotter localhost 8080 < demo/spotterInvalid.txt
```
* Expected output:
```
Observations added.
Wrong number of arguments. Expected 3, but 2 were given.
Unknown command: boat
Unknown command: abc
Server has been cleared.
```

---

#### Test 6: Eye and spotter integration
* Run the following commands in the terminal, in the project root:
```
./eye/target/appassembler/bin/eye localhost 8080 Cam1 45 45 < demo/eyeInt.txt
./spotter/target/appassembler/bin/spotter localhost 8080 < demo/spotterInt.txt
```
* Expected output (eye):
```
Registration successful. Proceeding...
End
```
* Expected output (spotter):
```
123456,PERSON,2020-04-03T09:24:08,Cam1,45.0,45.0
AALS00,CAR,2020-04-03T09:24:07,Cam1,45.0,45.0
123456,PERSON,2020-04-03T09:24:08,Cam1,45.0,45.0
12344321,PERSON,2020-04-03T09:24:07,Cam1,45.0,45.0
456,PERSON,2020-04-03T09:24:07,Cam1,45.0,45.0
11256,PERSON,2020-04-03T09:24:08,Cam1,45.0,45.0
123456,PERSON,2020-04-03T09:24:08,Cam1,45.0,45.0
AA6223,CAR,2020-04-03T09:24:08,Cam1,45.0,45.0
AABC34,CAR,2020-04-03T09:24:08,Cam1,45.0,45.0
AALS00,CAR,2020-04-03T09:24:07,Cam1,45.0,45.0
123456,PERSON,2020-04-03T09:24:06,Cam1,45.0,45.0
123456,PERSON,2020-04-03T09:24:08,Cam1,45.0,45.0
AALS00,CAR,2020-04-03T09:24:06,Cam1,45.0,45.0
AALS00,CAR,2020-04-03T09:24:07,Cam1,45.0,45.0
Server has been cleared.
```
* Note: The date in the spotter's expected output may vary according to the computer clock

#### Test 7: Test data replication
* Open 5 terminals:
  * 2 with silo-servers, one with instance number 1 and another with instance number 2;
  * 1 with and eye that connects to the server instance 1
  * 2 with spotters, one connecting to server instance 1 and other to server instance 2;

Note: See the README.md, to know how to run the commands above.

* Run the following commands in eye:
```
person,55

car,ATRT51

person,55
car,ATRT51
```
* Now run the following commands for both spotters. If at least one gossiping round has happened the results 
should be the same, meaning that the updates were propagated successfully.

```
> spot person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
55,PERSON,2020-05-02T00:48:31,Tagus,0.0,0.0
> spot car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
ATRT51,CAR,2020-05-02T00:48:31,Tagus,0.0,0.0
> exit
```
#### Test 8: Test a server going down and recovering its state

* Open 5 terminals:
  * 2 with silo-servers, one with instance number 1 and another with instance number 2;
  * 1 with an eye that connects to the server instance 1;
  * 2 with spotters, one connecting to server instance 1 and other to server instance 2;

Note: See the README.md, to know how to run the commands above.

* Run the following commands in eye:
```
person,55

car,ATRT51

person,55
car,ATRT51
```
* Press 'Ctrl+D' to shutdown the eye. It will no longer be needed.
* Wait for the updates to be propagated to other replicas. See when the following message pops up on the server
instance 1 terminal.
```
Updates sent...
```

* Run the following commands for both spotters, just to check that the updates were propagated to both replicas.
```
> spot person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
55,PERSON,2020-05-02T00:48:31,Tagus,0.0,0.0
> spot car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
ATRT51,CAR,2020-05-02T00:48:31,Tagus,0.0,0.0
```

* Shutdown server instance 1, by pressing 'Enter' on its terminal.
* Start the server instance 1, and wait for it to receive updates from the other server instance.
* Wait for the server instance 1 to receive updates from the other replica. The following message will pop up in the 
server instance 1 terminal.
```
Updates Received!
```
* Run the following commands again on the spotter that's connected to the server instance 1.
```
> spot person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
55,PERSON,2020-05-02T00:48:31,Tagus,0.0,0.0
> spot car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
ATRT51,CAR,2020-05-02T00:48:31,Tagus,0.0,0.0
```
* It prints the same results that were printed last time these commands were ran, which means that the server
 instance after a gossip round already has the most recent information on the system, including the information
 it had registered before going down.
 
* Shutdown the server instance 2, by pressing 'Enter' on its terminal.
* Start the server instance 2, and wait for it to receive updates from the other server instance.
* Wait for the server 2 to receive updates from the other replica. The following message will pop up in the 
server instance 2 terminal.
```
Updates Received!
```

* Run the following commands again on the spotter that's connected to the server instance 2.
```
> spot person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
55,PERSON,2020-05-02T00:48:31,Tagus,0.0,0.0
> spot car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
ATRT51,CAR,2020-05-02T00:48:31,Tagus,0.0,0.0
```
* It prints the same results again.

#### Test 9: Test a client reconnecting to another server

* Open 4 terminals in the following order:
  * 1 with a silo-server with instance number 1;
  * 1 with an eye, without giving a specific instance to connect to;
  * 1 with a spotter without giving a specific instance to connect to;
  * 1 with another silo-server with instance number 2;
  
Note: This order is to guarantee that both eye and spotter connect to server 1 and are able to change servers
 when the one they are connected to fails.
 
Note2: See the README.md, to know how to run the commands above. 

* Run the following commands in eye:
```
person,55

car,ATRT51

person,55
car,ATRT51
```
* Wait for the updates to be propagated to other replicas. See when the following message pops up on the server
instance 1 terminal.
```
Updates sent...
```

* Shutdown server instance 1, by pressing 'Enter' on its terminal.
* Run the following commands in eye:
```
person,55
car,ATRT51
```
* The eye will print the message: 
```
Registration successful. Proceeding...
```
* Because it lost the connection with the server and reconnected successfully to the other server and will still
send the observations from the command. 

* Run the following commands for the spotter. The spotter will also reconnect when the first command is run and 
the results will be with all the observations the eye made taken into account.
```
>spot person 55
55,PERSON,2020-05-02T14:43:37,Tagus,0.0,0.0
>trail person 55
55,PERSON,2020-05-02T14:43:37,Tagus,0.0,0.0
55,PERSON,2020-05-02T14:42:52,Tagus,0.0,0.0
55,PERSON,2020-05-02T14:42:26,Tagus,0.0,0.0
>spot car ATRT51
ATRT51,CAR,2020-05-02T14:43:37,Tagus,0.0,0.0
>trail car ATRT51
ATRT51,CAR,2020-05-02T14:43:37,Tagus,0.0,0.0
ATRT51,CAR,2020-05-02T14:42:52,Tagus,0.0,0.0
ATRT51,CAR,2020-05-02T14:42:39,Tagus,0.0,0.0
```
* It prints all the observations made

#### Test 10: Test the client cache

* Open 3 terminals:
  * 1 with a silo-server with instance number 1;
  * 1 with an eye that connects to the server instance 1;
  * 1 with a spotter that connects to the server instance 1; 

Note: See the README.md, to know how to run the commands above.

* Run the following commands in eye:
```
person,55

car,ATRT51

person,55
car,ATRT51
```
* Press 'Ctrl+D' to shutdown the eye. It will no longer be needed.

* Run the following commands in the spotter, to make the spotter save these command-result pair in its cache.
```
> spot person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
55,PERSON,2020-05-02T00:48:31,Tagus,0.0,0.0
> spot car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
ATRT51,CAR,2020-05-02T00:48:31,Tagus,0.0,0.0
```
* Shutdown server instance 1, by pressing 'Enter' on its terminal.
* Start the server instance 1.

* Run the following commands again on the spotter.
```
> spot person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail person 55
55,PERSON,2020-05-02T00:48:38,Tagus,0.0,0.0
55,PERSON,2020-05-02T00:48:31,Tagus,0.0,0.0
> spot car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
> trail car ATRT51
ATRT51,CAR,2020-05-02T00:48:38,Tagus,0.0,0.0
ATRT51,CAR,2020-05-02T00:48:31,Tagus,0.0,0.0
```
* It is printed the same results even though the server this spotter is connected to doesn't have the 
 required information to respond to its requests.