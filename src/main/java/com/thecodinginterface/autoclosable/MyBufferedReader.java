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