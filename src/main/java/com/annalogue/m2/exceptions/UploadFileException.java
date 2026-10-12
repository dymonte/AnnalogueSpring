package com.annalogue.m2.exceptions;

public class UploadFileException extends Exception {
	public UploadFileException(String messageKey) {
		super(messageKey);
	}
}
