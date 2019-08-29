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
