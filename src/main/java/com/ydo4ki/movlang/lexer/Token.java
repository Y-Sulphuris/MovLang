package com.ydo4ki.movlang.lexer;

import com.ydo4ki.movlang.Location;

import java.io.File;

/**
 * @author Sulphuris
 * @since 01.10.2024 11:45
 */
public class Token {
	public final TokenType type;
	public final String text;
	public final Location location;

	public Token(TokenType type, String text, int startpos, int endpos, int line, File file) {
		this.type = type;
		this.text = text;
		this.location = new Location(startpos,endpos,line,line,file);
	}
	public Token(TokenType type,int startpos,int endpos, int line, File file) {
		this(type,"\0",startpos,endpos,line,file);
	}

	@Override
	public String toString() {
		return "Token{" + type +
				(text != null ?" = '" + text + '\'' : "")+
				", startpos=" + location.getStartPos() +
				", endpos=" + location.getEndPos() +
				", line=" + location.getStartLine() +
				'}';
	}
}
