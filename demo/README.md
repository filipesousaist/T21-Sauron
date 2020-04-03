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
```

---

#### Test 2: Eye invalid join
* Run the following commands in the terminal, in the project root:
```
./eye/target/appassembler/bin/eye localhost 8080 Tagus 45 45
./eye/target/appassembler/bin/eye localhost 8080 Tagus -45 80
```
* Expected output:
```
```

---

#### Test 3: Eye invalid commands
* Run the following commands in the terminal, in the project root:
```
./eye/target/appassembler/bin/eye localhost 8080 Cam1 45 45 < demo/eyeInvalid.txt
```
* Expected output:
```
```

---

#### Test 4: Spotter demonstration
* Run the following commands in the terminal, in the project root:
```
./spotter/target/appassembler/bin/spotter localhost 8080 < demo/spotterDemo.txt
```
* Expected output:
```
```

---

#### Test 5: Spotter invalid commands
* Run the following commands in the terminal, in the project root:
```
./spotter/target/appassembler/bin/spotter localhost 8080 < demo/spotterInvalid.txt
```
* Expected output:
```
```

---

#### Test 6: Eye and spotter integration
* Run the following commands in the terminal, in the project root:
```
./eye/target/appassembler/bin/eye localhost 8080 Cam1 45 45 < demo/eyeInt.txt
./spotter/target/appassemler/bin/spotter localhost 8080 < demo/spotterInt.txt
```
* Expected output:
```
```

