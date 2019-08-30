# java-try-with-resources

This is the sample code for the blog article [How To Write Better Java with Try-With-Resources](https://thecodinginterface.com/blog/java-try-with-resources/)

### Usage

Mac / Linux

```
$ ./gradlew run
```

Windows

```
gradlew.bat run
```

which outputs

```

> Task :run

----------------------------------------------------------------------------------------------------
                                   Read Greeting (try / finally)                                    
----------------------------------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileReader closing ...
MyBufferedReader closing ...

----------------------------------------------------------------------------------------------------
                                  Greeting Missing (try / finally)                                  
----------------------------------------------------------------------------------------------------
java.lang.NullPointerException
        at com.thecodinginterface.autoclosable.App.readGreeting_NPE(App.java:89)
        at com.thecodinginterface.autoclosable.App.main(App.java:19)

----------------------------------------------------------------------------------------------------
                                 Copy Line By Line (try / finally)                                  
----------------------------------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileWriter closing ...
MyBufferedWriter closing ...
MyFileReader closing ...
MyBufferedReader closing ...

----------------------------------------------------------------------------------------------------
                                 Read Greeting (try-with-resources)                                 
----------------------------------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileReader closing ...
MyBufferedReader closing ...

----------------------------------------------------------------------------------------------------
                               Greeting Missing (try-with-resources)                                
----------------------------------------------------------------------------------------------------
java.io.FileNotFoundException: does_not_exist.txt (No such file or directory)
        at java.base/java.io.FileInputStream.open0(Native Method)
        at java.base/java.io.FileInputStream.open(FileInputStream.java:213)
        at java.base/java.io.FileInputStream.<init>(FileInputStream.java:155)
        at java.base/java.io.FileInputStream.<init>(FileInputStream.java:110)
        at java.base/java.io.FileReader.<init>(FileReader.java:60)
        at com.thecodinginterface.autoclosable.MyFileReader.<init>(MyFileReader.java:10)
        at com.thecodinginterface.autoclosable.App.readGreeting_TWR_NoNPE(App.java:141)
        at com.thecodinginterface.autoclosable.App.main(App.java:37)

----------------------------------------------------------------------------------------------------
                               Copy Line By Line (try-with-resources)                               
----------------------------------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileWriter closing ...
MyBufferedWriter closing ...
MyFileReader closing ...
MyBufferedReader closing ...

```
