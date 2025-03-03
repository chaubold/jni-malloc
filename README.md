Build with:

```
mvn clean package
```

Run with:

```
java -Xms512m -Xmx512m -Djava.library.path=$(pwd)/target -cp target/jni-memory-test-1.0-SNAPSHOT.jar com.example.jnimemory.MemoryTest
```

If you add functions to the .c code and define the interface in the MemoryTestNative.java, run:

```
javac -h src/main/c src/main/java/com/example/jnimemory/MemoryTestNative.java
```