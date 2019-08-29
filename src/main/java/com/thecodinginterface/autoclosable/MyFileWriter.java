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