# How To Write Better Java with Try-With-Resources

### Introduction

In this How To article I demonstrate using the try-with-resources Java construct and compare it to the traditional try-finally paradigm. Both of these approaches aim to solve the problem of making sure resources get properly closed to avoid resource leaks but, this article intends to help make the case for why try-with-resources is preferrable.

I keep using this word resource which I recognize as a slightly ambiguous term to generically define something so, let me specify what is meant. Accoridng the the [Oracle Docs](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html), a resource is an object that is intended to be closed when a program is done executing and, furthermore, its good practice for objects that are intented to be used as resources to implement either or both java.lang.AutoCloseable and java.io.Closeable interfaces.

### Custom Classes to Make for Easier Tracking

In order to better demonstrate the closing sequence of common resource classes such as BufferedReader I have chosen to implement subclasses that override the close method and prints a message indicating which class is being closed.

MyBufferedReader.java

```
package com.thecodinginterface.twr;

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
package com.thecodinginterface.twr;

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
package com.thecodinginterface.twr;

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
package com.thecodinginterface.twr;

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

The legacy try-finally construct does accomplish the goal of ensuring that resources are capable of being closed via a finally block that is ensured to be reached in a program's execution path in the event of an exception. However, there are a couple of flaws commonly seen in Java programs when utilizing this approach.  The first issue is largely asethetic in that its now unnecessarily verbose and the second is that it sets up a potential for exceptions being thrown in the finally block which are likely to obscure earlier exceptions.

As an example, the following code sample utilizes a try-finally to read a file named greeting.txt which manually closes a MyBufferedReader instance.

greeting.txt

```
hello there,
my name is Adam.
Nice to meet you.

``` 

App.java

```
package com.thecodinginterface.twr;

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
}
```

Here is the output from running the Gradle run task

```
$ ./gradlew run

> Task :run

--------------------------------------------------------------------------------------
                            Read Greeting (try / finally)                             
--------------------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileReader closing ...
MyBufferedReader closing ...
```

Ok, proof enough that the resources are closed, afterall, you see me explicitly doing this in the finally block. Lets take a look at the try-with-resources version of this same method.

```
package com.thecodinginterface.twr;

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
}
```

And the output.

```
$ ./gradlew run

> Task :run

--------------------------------------------------------------------------------------
                          Read Greeting (try-with-resources)                          
--------------------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileReader closing ...
MyBufferedReader closing ...
```

So far you may be saying, "ok thats great and all, less code is more readable so I may go about updating my code if I think about it next time I come accross existing try / finally implementations but, I'm probably not going to go actively do this".

And I'd say fair point.

Moving on, now lets take a look at what happens if I try to open a file that doesn't exist using the same try-finally implementation shown in the first example.

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
}
```

And the output.

```
$ ./gradlew run

> Task :run

--------------------------------------------------------------------------------------
                           Greeting Missing (try / finally)                           
--------------------------------------------------------------------------------------
java.lang.NullPointerException
        at com.thecodinginterface.twr.App.readGreeting_NPE(App.java:89)
        at com.thecodinginterface.twr.App.main(App.java:25)
```

As you can see the reader variable is never assigned a MyBufferedReader instance and thus, throws a NullPointerException (NPE) when I try to call close on it in the finally block. More importantly, notice that the true source of the problem, the FileNotFoundException thrown due to the misisng file, is obscured and otherwise not included in the stack trace leaving me hunting down the cause of the NPE which is pretty straight forward but, it sure would be nice to know right off the bat the true source of the problem in my code.

Lets now take a look at the same missing file example as a try-with-resources implementation and inspect it's output as well.

```
package com.thecodinginterface.twr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class App {
    public static void main(String[] args) {
        try {
            readGreeting_TWR_NoNPE();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * notice that the try-with-resources does not obscure
     * lower exceptions like the try / finally did
     */
    static void readGreeting_TWR_NoNPE() throws IOException {
        printHeader("Greeting Missing (try-with-resources)");
        try (var reader = new MyBufferedReader(new MyFileReader("does_not_exist.txt"))) {
            String line = null;
            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
```

And the output.

```
$ ./gradlew run

> Task :run

--------------------------------------------------------------------------------------
                        Greeting Missing (try-with-resources)                         
--------------------------------------------------------------------------------------
java.io.FileNotFoundException: does_not_exist.txt (No such file or directory)
        at java.base/java.io.FileInputStream.open0(Native Method)
        at java.base/java.io.FileInputStream.open(FileInputStream.java:213)
        at java.base/java.io.FileInputStream.<init>(FileInputStream.java:155)
        at java.base/java.io.FileInputStream.<init>(FileInputStream.java:110)
        at java.base/java.io.FileReader.<init>(FileReader.java:60)
        at com.thecodinginterface.twr.MyFileReader.<init>(MyFileReader.java:10)
        at com.thecodinginterface.twr.App.readGreeting_TWR_NoNPE(App.java:141)
        at com.thecodinginterface.twr.App.main(App.java:31)
```

As you can see the original source, or first root cause, exception is the one that gets included in the stack trace which is likely to get me to the problematic root cause (ie, I'm opening a file that does not exist) more directly than what was seen it the try-finally implementation.

Next I'd like to try to make a better case for how try-with-resources can actually improve code readability and better manage the order in which resources are closed with working with multiple resources. Below is a simple method that uses a BufferedReader / BufferedWriter pair to copy the greeting.txt file line by line to greeting_copy.txt using the try-finally statement.

```
package com.thecodinginterface.twr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class App {
    public static void main(String[] args) {
        try {
            copyLineByLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * hard to read nested try / finally which also still
     * suffers from the problem of a potential NPE obsuring
     * an earlier thrown exception and made more likely due to 
     * the fact there are two resources being utilized in the code
     */
    static void copyLineByLine() throws IOException {
        printHeader("Copy Line By Line (try / finally)");
        // pre-autoclosable (aka, try-with-resource enabler)
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            reader = new MyBufferedReader(new MyFileReader("greeting.txt"));
            try {
                writer = new MyBufferedWriter(new MyFileWriter("greeting_copy.txt"));
                String line = null;
                while((line = reader.readLine()) != null) {
                    System.out.println(line);
                    writer.write(line);
                }
            } finally {
                writer.close();
            }
        } finally {
            reader.close();
        }
    }
}
```

And for consistency the output.

```
$ ./gradlew run

> Task :run

--------------------------------------------------------------------------------------
                          Copy Line By Line (try / finally)                           
--------------------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileWriter closing ...
MyBufferedWriter closing ...
MyFileReader closing ...
MyBufferedReader closing ...
```

Now I show a refactored version that utilizes the try-with-resources paradigm. 

```
package com.thecodinginterface.twr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class App {
    public static void main(String[] args) {
        try {
            copyLineByLine_TWR();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * again, noticably few lines of code using try-with-resources
     * and safer / won't obscure early exceptions. When multiple resources
     * are declared in the TWR they are separated with semicolons and
     * closed in the order they are declared right-to-left
     */
    static void copyLineByLine_TWR() throws IOException {
        printHeader("Copy Line By Line (try-with-resources)");

        try (var reader = new MyBufferedReader(new MyFileReader("greeting.txt"));
                var writer = new MyBufferedWriter(new MyFileWriter("greeting_copy.txt"))) {
            String line = null;
            while((line = reader.readLine()) != null) {
                System.out.println(line);
                writer.write(line);
            }
        }
    }
}
```

Now the decreased verbosity of the code is starting to become quite evident as this version accomplishes the same task in roughly half the number of lines of code as the try-finally approach. What this means to me is that I'm likely to be able to readily at a glance understand what this bit of code is doing and, since I am actually writing less code there is less of a chance of me introducing a error in the code because there is less of it. Additionally, I still benefit from the improved stack trace handing and NullPointerException safety.

Then for completeness the output.

```
$ ./gradlew run

> Task :run

--------------------------------------------------------------------------------------
                        Copy Line By Line (try-with-resources)                        
--------------------------------------------------------------------------------------
hello there,
my name is Adam.
Nice to meet you.

MyFileWriter closing ...
MyBufferedWriter closing ...
MyFileReader closing ...
MyBufferedReader closing ...
```


### Conclusion

In this How To article I have demonstrated how to use the try-with-resources Java construct and contrasted it against the traditional pre Java 7 way which utilizes the try-finally statement.  As is customary for How To articles on The Coding Interface I have intentionally left a lot of the explanation in the code itself rather than lengthy explanations so, if you are interested in further investigation please see the resources for learning more section.














