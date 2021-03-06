package kimbaudi.fabflix.moviequiz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

class CSVReader {
	private BufferedReader in;
	private Vector<String> line;

	CSVReader(InputStream input) {
		in = new BufferedReader(new InputStreamReader(input));
		line = new Vector<String>();
	}

	boolean next() {
		line.clear();
		try {
			String currentLine = in.readLine();
			StringBuilder currentElement = new StringBuilder();
			boolean inQuote = false;
			if (currentLine == null)
				return false;
			for (int i = 0; i < currentLine.length(); i++) {
				if (currentLine.charAt(i) == ' ' && !inQuote)
					continue;
				if (currentLine.charAt(i) == '"' && !inQuote) {
					inQuote = true;
					continue;
				}
				if (currentLine.charAt(i) == ',' && !inQuote) {
					line.add(currentElement.toString());
					currentElement = new StringBuilder();
					continue;
				}
				if (currentLine.charAt(i) == '"' && inQuote) {
					inQuote = false;
					continue;
				}
				if (currentLine.charAt(i) != '"')
					currentElement.append(currentLine.charAt(i));
			}
			line.add(currentElement.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;

	}

	String[] getLine() {
		String[] ret = new String[line.size()];
		line.toArray(ret);
		return ret;
	}

}
