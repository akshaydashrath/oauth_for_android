package com.novoda.oauth.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class SqlScanner  implements Iterator<String>, Iterable<String> {

	private final BufferedReader	in;
	private final Scanner			scanner;

	public SqlScanner(InputStream in) {
		this(new InputStreamReader(in));
	}

	public SqlScanner(Reader in) {
		this.in = new BufferedReader(in, 512);
		this.scanner = new Scanner(in);
		scanner.useDelimiter("\\s*;");
		
	}

	public boolean hasNext() {
		return scanner.hasNext();
	}

	public String next() throws NoSuchElementException {
		String initial = scanner.next();
		// Removes the comment
		StringBuffer sql = new StringBuffer(initial.replaceAll("(--).*\n", ""));
		if (sql.toString().contains("FOR EACH ")) {
			sql.append(";").append(scanner.next());
		}
		return sql.toString().trim();
	}

	public void close() throws IOException {
		in.close();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public Iterator<String> iterator() {
		return this;
	}
}