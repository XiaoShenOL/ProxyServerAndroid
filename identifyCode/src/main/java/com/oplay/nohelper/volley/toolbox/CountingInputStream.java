package com.oplay.nohelper.volley.toolbox;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by qin on 15-3-15.
 */
public class CountingInputStream extends FilterInputStream {

	public int bytesRead = 0;

	/**
	 * Constructs a new {@code FilterInputStream} with the specified input
	 * stream as source.
	 * <p/>
	 * <p><strong>Warning:</strong> passing a null source creates an invalid
	 * {@code FilterInputStream}, that fails on every method that is not
	 * overridden. Subclasses should check for null in their constructors.
	 *
	 * @param in the input stream to filter reads on.
	 */
	public CountingInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		int result = super.read();
		if (result != -1) {
			bytesRead++;
		}
		return result;
	}

	@Override
	public int read(byte[] buffer, int offset, int count) throws IOException {
		int result = super.read(buffer, offset, count);
		if (result != -1) {
			bytesRead += result;
		}
		return result;
	}
}
