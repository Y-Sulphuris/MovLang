package com.ydo4ki.movlang.lexer;

import java.io.File;
import java.util.Stack;

/**
 * @author Sulphuris
 * @since 01.10.2024 11:48
 */
public class Tokenizer {

	private File file;

	private String source = null;
	private int pos = 0;
	private int line = 1;

	private Exception exception = null;

	public Stack<Token> tokenize(String source, File file) {
		this.source = source;
		this.file = file;
		pos = 0;
		line = 1;
		exception = null;

		Stack<Token> tokens = new Stack<>();
		Token token = nextToken();
		try {
			tokens.push(token);
			while (token.type != TokenType.EOF) {
				tokens.push(token = nextToken());
				if (token.type == TokenType.ERROR)
					throw new UnexpectedTokenException(token, "Invalid token", exception);
			}
		} catch (UnexpectedTokenException e) {
			Token errorToken = e.getToken();
			if (errorToken != null) {
				if (errorToken.text != null) System.err.println(errorToken.text);
				String str = source.substring(errorToken.location.getStartPos(), errorToken.location.getEndPos());
				System.err.println("Invalid token at line: " + errorToken.location.getStartLine());
				System.err.println(str);
				for (int i = 0; i < str.length(); i++)
					System.err.print('~');
				System.err.println();
			}
			throw e;
		}
		return tokens;
	}


	private char nextChar() {
		if (pos >= source.length()) return '\0';
		return source.charAt(pos++);
	}

	private char seeNextChar() {
		if (pos >= source.length()) return '\0';
		return source.charAt(pos);
	}

	private Token nextToken() {
		char ch = nextChar();

		//skip
		while (ch == ' ' || ch == '\n' || ch == '\t') {
			if (ch == '\n') line++;
			ch = nextChar();
		}

		if (ch == '\0') return new Token(TokenType.EOF, pos - 1, pos, line, file);

		//comments
		if (ch == '/') {
			if (seeNextChar() == '/') {
				ch = nextChar();
				int startpos = pos;
				ch = nextChar();
				StringBuilder builder = new StringBuilder();
				while (ch != '\n') {
					builder.append(ch);
					ch = nextChar();
				}
				pos--;
				return new Token(TokenType.COMMENT, builder.toString(), startpos, pos, line, file);
			} else if (seeNextChar() == '*') {
				int startpos = pos;
				ch = nextChar();
				StringBuilder builder = new StringBuilder();
				while (!(ch == '*' && seeNextChar() == '/')) {
					if (ch == '\n') line++;
					builder.append(ch);
					ch = nextChar();
				}
				pos++;
				return new Token(TokenType.COMMENT, builder.toString(), startpos, pos, line, file);
			} else {
				//exception = new Exception();
				//return new Token(TokenType.ERROR,pos-1,pos,line);
			}
		}


		//string literals
		Token stringToken = readLiteral('"', ch, TokenType.STRING);
		if (stringToken != null) return stringToken;


		//char literals
		Token charsToken = readLiteral('\'', ch, TokenType.CHARS);
		if (charsToken != null) return charsToken;


		// text
		if (isValidNameChar(ch)) {
			int startpos = pos - 1;
			StringBuilder builder = new StringBuilder();
			while (isValidNameChar(ch)) {
				builder.append(ch);
				ch = nextChar();
			}
			pos--;
			String value = builder.toString();
			return new Token(TokenType.IDENTIFIER, value, startpos, pos, line, file);
		}


		if (ch == '#') {
			int startpos = pos - 1;
			ch = nextChar();
			StringBuilder builder = new StringBuilder();
			while (isValidNameChar(ch)) {
				builder.append(ch);
				ch = nextChar();
			}
			pos--;
			String value = builder.toString();
			return new Token(TokenType.DIRECTIVE, value, startpos, pos, line, file);
		}

		// operators
		TokenType type;
		switch (ch) {
			case '*':
				type = TokenType.STAR;
				break;
			case '+':
				type = TokenType.PLUS;
				break;
			case '^':
				type = TokenType.BITS;
				break;
			case ':':
				type = TokenType.COLON;
				break;
			default:
				type = TokenType.ERROR;
				exception = new Exception();
		}
		return new Token(type, String.valueOf(ch), pos - 1, pos, line, file);
	}


	private Token readLiteral(char separators, char ch, TokenType type) {
		if (ch == separators) {
			int startpos = pos;
			ch = nextChar();
			StringBuilder builder = new StringBuilder();
			while (ch != separators) {
				if (ch == '\\') {
					ch = nextChar();
					switch (ch) {
						case '\\':
							builder.append('\\');
							break;
						case '\'':
							builder.append('\'');
							break;
						case '"':
							builder.append('"');
							break;
						case 'n':
							builder.append('\n');
							break;
						case 't':
							builder.append('\t');
							break;
						case 'u': {
							char character = (char) Integer.parseInt(String.valueOf(nextChar()) + nextChar() + nextChar() + nextChar());
							builder.append(character);
							break;
						}
						default: {
							if (hex.contains(String.valueOf(ch))) {
								char character = (char) Integer.parseInt(String.valueOf(ch) + nextChar());
								builder.append(character);
								break;
							}
						}
					}
				} else {
					if (ch == '\n')
						throw new UnexpectedTokenException(
								new Token(type, builder.toString(), startpos - 1, pos, line, file), "Unexpected '\\n'"
						);
					builder.append(ch);
				}
				ch = nextChar();
			}
			return new Token(type, builder.toString(), startpos - 1, pos, line, file);
		}
		return null;
	}

	private static final String hex = "0123456789abcdefABCDEF";

	private boolean isNumeric(char ch) {
		return "0123456789".contains(String.valueOf(ch));
	}

	private boolean isValidNameChar(char ch) {
		return ch == '_' || isNumeric(ch) || Character.isAlphabetic(ch);
	}
}
