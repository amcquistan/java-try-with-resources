# How To Write Better Java with Try-With-Resources

### Introduction

In this How To article I demonstrate using the try-with-resources construct introduced in java 7 and compare it to the traditional try-finally paradigm. Both of these constructs aim to solve the problem of making sure a resources get properly closed to avoid memory leaks but this article intends to help make the case for why the newer try-with-resources construct is preferred.

### Custom Classes to Make for Easier Tracking

In order to better demonstrate the closing sequence of common resource classes such as BufferedReader I have chosen to implement subclasses that override the close method and prints a message indicating which class is being closed.

MyBufferedReader.java

```
package com.thecodinginterface.autoclosable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class MyBufferedReader extends BufferedReader {

    public MyBufferedReader(Reader in) {
        super(in);
    }
    
  @Override
    public void close() throws IOException {
        super.close();

        System.out.println("MyBufferedReader closing ...");
    }
}
```

MyFileReader.java

```
package com.thecodinginterface.autoclosable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MyFileReader extends FileReader {

    public MyFileReader(String fileName) throws FileNotFoundException {
        super(fileName);
    }
    
  @Override
    public void close() throws IOException {
        super.close();

        System.out.println("MyFileReader closing ...");
    }
}
```

MyBufferedWriter.java

```
package com.thecodinginterface.autoclosable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class MyBufferedWriter extends BufferedWriter {

    public MyBufferedWriter(Writer out) {
        super(out);
    }
    
  @Override
    public void close() throws IOException {
        super.close();

        System.out.println("MyBufferedWriter closing ...");
    }
}
```

MyFileWriter.java

```
package com.thecodinginterface.autoclosable;

import java.io.FileWriter;
import java.io.IOException;

public class MyFileWriter extends FileWriter {

    public MyFileWriter(String fileName) throws IOException {
        super(fileName);
    }

    @Override
    public void close() throws IOException {
        super.close();
        System.out.println("MyFileWriter closing ...");
    }
}
```

### Traditional Try - Finally 

The legacy try-finally construct does accomplish the goal of ensuring that resources are capable of being closed by providing a finally block that is ensured to be reached in a programs execution path in the event of an exception there are a couple of flaws commonly seen.  The first issue I have is that its potentially unnecessarily verbose and the second is that it sets up a potential for exceptions being thrown in the finally block which are likely to obscure earlier exceptions.

As an example, I the following code example successfully uses a try-finally to read a file named greeting.txt which manually closes a MyBufferedReader instance.

greeting.txt

```
hello there,
my name is Adam.
Nice to meet you.

``` 

App.java

```
package com.thecodinginterface.autoclosable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class App {
    public static void main(String[] args) {
        try {
            readGreeting();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    static void readGreeting() throws IOException {
        printHeader("Read Greeting (try / finally)");
        // pre-autoclosable (aka, try-with-resource enabler)
        BufferedReader reader = null;

        try {
            reader = new MyBufferedReader(new MyFileReader("greeting.txt"));
            String line = null;
            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } finally {
            reader.close();
        }
    }

    static void printHeader(String header) {
        int w = 100;
        String bar = "-".repeat(w);
        System.out.println("\n" + bar);
        System.out.println(StringUtils.center(header, w));
        System.out.println(bar);
    }
}
```

Here is the output from running the Gradle run task

```
$ ./gradlew run

> Task :run

--------------------------------------------------------------------------
                                 Read Greeting                                  
--------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileReader closing ...
MyBufferedReader closing ...
```

Ok, proof enough that the resources are closed right, afterall, you see me explicitly doing this in the finally block. Lets take a look at the try-with-resources version of this same method.

```

package com.thecodinginterface.autoclosable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class App {
    public static void main(String[] args) {
        try {
            readGreeting_TWR();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * implemented greeting reader with try-with-resources
     * which is noticably less code
     */
    static void readGreeting_TWR() throws IOException {
        printHeader("Read Greeting (try-with-resources)");
        try (var reader = new MyBufferedReader(new MyFileReader("greeting.txt"))) {
            String line = null;
            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    static void printHeader(String header) {
        int w = 100;
        String bar = "-".repeat(w);
        System.out.println("\n" + bar);
        System.out.println(StringUtils.center(header, w));
        System.out.println(bar);
    }
}
```

And the output.

```
             Read Greeting (try-with-resources)                       
Adams-MacBook-Pro-191:autoclosable adammcquistan$ ./gradlew run

> Task :run

----------------------------------------------------------------------------------------------
                                 Read Greeting (try-with-resources)                                 
----------------------------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileReader closing ...
MyBufferedReader closing ...
```

// make argument, fewer lines of code, maybe more readable / maybe not depending on taste


Moving on, now lets take a look at what happens if I try to open a file that doesn't exist.

```
package com.thecodinginterface.autoclosable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class App {
    public static void main(String[] args) {

        try {
            readGreeting_NPE();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * since file doesn't exist an early exception is throw
     * leading to reader variable never being assigned an object
     * which is then used to call close on a null which causes
     * a null pointer exception
     */
    static void readGreeting_NPE() throws IOException {
        printHeader("Greeting Missing (try / finally)");
        // pre-autoclosable (aka, try-with-resource enabler)
        BufferedReader reader = null;

        try {
            // does_not_exist.txt does not exist which causes a FileNotFoundException
            // to be thrown which inhibits the MyBufferedReader reader variable from being
            // constructed.
            reader = new MyBufferedReader(new MyFileReader("does_not_exist.txt"));
            String line = null;
            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } finally {
            // finally still gets called but, at this point reader is null rather than
            // a instance of MyBufferedReader resulting in a NPE being thrown. Unfortunately,
            // the NPE hides the true source of the error (ie, the FileNotFoundException)
            reader.close();
        }
    }

    static void printHeader(String header) {
        int w = 100;
        String bar = "-".repeat(w);
        System.out.println("\n" + bar);
        System.out.println(StringUtils.center(header, w));
        System.out.println(bar);
    }
}
```

And the output.

```
$ ./gradlew run

> Task :run

--------------------------------------------------------------------------------
                                Greeting Missing                                
--------------------------------------------------------------------------------
java.lang.NullPointerException
        at com.thecodinginterface.autoclosable.App.readGreeting_NPE(App.java:80)
        at com.thecodinginterface.autoclosable.App.main(App.java:19)
```

As you can see the reader variable is never assigned a MyBufferedReader instance and thus, throws a NullPointerException when I try to call close on it in the finally block but, notice that the true source problem exception of the file not existing is obscured and not included in the stack trace.




















