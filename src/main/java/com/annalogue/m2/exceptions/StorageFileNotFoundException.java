package com.annalogue.m2.exceptions;

import java.net.MalformedURLException;

public class StorageFileNotFoundException extends MalformedURLException {

	private static final String messageKey = "error.upload.storage.notFound";

	public StorageFileNotFoundException() {
		super(messageKey);
	}
}
