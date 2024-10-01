package com.ydo4ki.movlang;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Sulphuris
 * @since 01.10.2024 11:51
 */
@Getter
public class CompilerException extends RuntimeException {
	@Setter
	private Location location;
	private final String rawMessage;


	public CompilerException(Location location, String message) {
		super(message);
		this.location = location;
		this.rawMessage = message;
	}

	public CompilerException(Location location, String message, Throwable cause) {
		super(message, cause);
		this.location = location;
		this.rawMessage = message;
	}


	public CompilerException(Location location, String message, String rawMessage) {
		super(message);
		this.location = location;
		this.rawMessage = rawMessage;
	}

	public CompilerException(Location location, String message, Throwable cause, String rawMessage) {
		super(message, cause);
		this.location = location;
		this.rawMessage = rawMessage;
	}

	public CompilerException(Location location, Throwable cause, String rawMessage) {
		super(cause);
		this.location = location;
		this.rawMessage = rawMessage;
	}

}

