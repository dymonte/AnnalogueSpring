package com.annalogue.m2.models;

public enum FileType {
	CM("cm"),
	TD("tp"),
	TP("tp"),
	EXAM("exam");

	private final String value;

	FileType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}