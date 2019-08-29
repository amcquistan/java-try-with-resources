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