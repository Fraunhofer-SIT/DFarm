package de.fraunhofer.sit.beast.internal.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;


public class LookAheadBufferedReader {
	private final BufferedReader reader;
	private String lookAhead;

	public String lookAheadLine() throws IOException {
		lookAhead = reader.readLine();
		return lookAhead;
	}
	
	public String readLine() throws IOException {
		if (lookAhead != null) {
			String l = lookAhead;
			lookAhead = null;
			return l;
		}
		return reader.readLine();
	}
	
	public LookAheadBufferedReader(Reader reader, int bufferSize) {
		this.reader = new BufferedReader(reader, bufferSize); 
	}

	
	
}
