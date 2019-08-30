
package com.thecodinginterface.twr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class App {
    public static void main(String[] args) {
        // try {
        //     readGreeting();
        // } catch(IOException e) {
        //     e.printStackTrace();
        // }

        // try {
        //     readGreeting_TWR();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        // try {
        //     readGreeting_NPE();
        // } catch(Exception e) {
        //     e.printStackTrace();
        // }

        // try {
        //     readGreeting_TWR_NoNPE();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        // try {
        //     copyLineByLine();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        try {
            copyLineByLine_TWR();
        } catch (Exception e) {
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

    static void printHeader(String header) {
        int w = 86;
        String bar = "-".repeat(w);
        System.out.println("\n" + bar);
        System.out.println(StringUtils.center(header, w));
        System.out.println(bar);
    }
}
