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
* Expected output(eye):
```
Registration successful. Proceeding...
End
```
* Expected output(spotter):
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

