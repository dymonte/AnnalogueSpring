package com.annalogue.m2.exceptions;

import java.net.MalformedURLException;

public class StorageFileNotFoundException extends MalformedURLException {
	public StorageFileNotFoundException(String message) {
		super(message);
	}
}
