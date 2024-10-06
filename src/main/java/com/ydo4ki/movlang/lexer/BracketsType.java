package com.ydo4ki.movlang.lexer;

/**
 * @author Sulphuris
 * @since 05.10.2024 19:52
 */
public enum BracketsType {
	FIGURE('{','}'),
	ROUND('(',')'),
	SQUARE('[', ']'),
	TRIANGLE('<', '>')
	;

	public final char open, close;

	BracketsType(char open, char close) {
		this.open = open;
		this.close = close;
	}
}
