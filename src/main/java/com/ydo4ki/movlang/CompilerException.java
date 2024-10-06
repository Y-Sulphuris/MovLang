package com.ydo4ki.movlang;

import com.ydo4ki.movlang.tokenizer.Token;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Stack;

/**
 * @author Sulphuris
 * @since 01.10.2024 11:51
 */
@Getter
public class CompilerException extends RuntimeException {
	@Setter
	private Location location;
	private final String rawMessage;

	@Setter
	@Nullable private Stack<Token> tokens = Compiler.getFinalTokens();


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

	@SneakyThrows // вот это офигенная идея
	@Override
	public void printStackTrace(PrintStream err) {
		String filename = location.getSourceFile().getAbsolutePath();
		filename = filename.substring(0).replaceAll("\\|/", ".");
		String source = Compiler.getSource(location.getSourceFile());
		System.err.println(getErrorDescription(this, filename, source));
		if (this.getCause() != this && this.getCause() instanceof CompilerException) {
			System.err.println("for:");
			System.err.println(getErrorDescription((CompilerException) this.getCause(), filename, source));
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
			/*if (e.tokens == null) {*/
				try {
					line = source.split("\n")[e.getLocation().getStartLine() - 1];
				} catch (ArrayIndexOutOfBoundsException ee) {
					line = " ";
				}
			/*} else {
				StringBuilder lb = new StringBuilder();
				for (int i = 0; i < e.tokens.size(); i++) {
					Token t = e.tokens.get(i);
					if (!Location.lineIntersects(t.getLocation(), e.getLocation())) {
						continue;
					}
					while (Location.lineIntersects(t.getLocation(), e.getLocation())) {
						lb.append(t.text).append(' ');
						t = e.tokens.get(++i);
					}
					break;
				}
				line = lb.toString();
			}*/

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

