package com.ydo4ki.movlang;

import lombok.Getter;
import lombok.Setter;

import java.io.PrintStream;

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

	@Override
	public void printStackTrace(PrintStream err) {
		String filename = location.getSourceFile().getAbsolutePath();
		filename = filename.substring(0).replaceAll("\\|/", ".");
		System.err.println(getErrorDescription(this, filename, Compiler.source));
		if (this.getCause() != this && this.getCause() instanceof CompilerException) {
			System.err.println("for:");
			System.err.println(getErrorDescription((CompilerException) this.getCause(), filename, Compiler.source));
		}
		super.printStackTrace(err);
	}
	private static String getErrorDescription(CompilerException e, String filename, String source) {
		StringBuilder msg = new StringBuilder(e.getClass().getSimpleName()).append(": ").append(e.getRawMessage()).append(" (")
				.append(filename);
		if (e.getLocation() != null) {
			msg.append(':').append(e.getLocation().getStartLine());
		}
		msg.append(')').append("\n\n");

		if (e.getLocation() != null) {
			String line;
			try {
				line = source.split("\n")[e.getLocation().getStartLine() - 1];
			} catch (ArrayIndexOutOfBoundsException ee) {
				line = " ";
			}

			int linePos = source.substring(0, e.getLocation().getStartPos()).lastIndexOf('\n');
			int errStart = e.getLocation().getStartPos() - linePos;
			int errEnd = e.getLocation().getEndPos() - linePos;

			msg.append(line).append('\n');

			char[] underline = new char[line.length()];
			for (int i = 0; i < underline.length; i++) {
				if (i >= errStart - 1 && i < errEnd - 1) underline[i] = '~';
				else if (line.charAt(i) == '\t') underline[i] = '\t';
				else underline[i] = ' ';
			}
			return msg.append(underline).append('\n').toString();
		}

		return msg.append('\n').toString();
	}
}

