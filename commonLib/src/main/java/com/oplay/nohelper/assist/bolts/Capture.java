package com.oplay.nohelper.assist.bolts;

/**
 * Provides a class that can be used for capturing variables in an anonymous class implementation.
 *
 * @param <T>
 */
public class Capture<T> {
	private T value;

	public Capture() {
	}

	public Capture(T value) {
		this.value = value;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}
}
