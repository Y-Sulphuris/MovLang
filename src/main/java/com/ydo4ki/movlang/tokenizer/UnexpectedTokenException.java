package com.ydo4ki.movlang.tokenizer;

import com.ydo4ki.movlang.CompilerException;
import lombok.Getter;

/**
 * @author Sulphuris
 * @since 01.10.2024 11:51
 */
@Getter
public class UnexpectedTokenException extends CompilerException {
	private final Token token;

	public UnexpectedTokenException(Token token, String msg, Exception e) {
		super(token.location, token + " ("+msg+")",e,msg);
		this.token = token;
	}
	public UnexpectedTokenException(Token token, char expected) {
		this(token,"'" + expected + "' expected");
	}
	public UnexpectedTokenException(Token token, String msg) {
		super(token.location, token + " ("+msg+")",msg);
		this.token = token;
	}
	public UnexpectedTokenException(Token token) {
		super(token.location, String.valueOf(token));
		this.token = token;
	}
}

