package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.CompilerException;
import com.ydo4ki.movlang.tokenizer.Token;
import com.ydo4ki.movlang.tokenizer.TokenType;
import com.ydo4ki.movlang.tokenizer.UnexpectedTokenException;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Sulphuris
 * @since 02.10.2024 10:55
 */
public class Parser {
	private final Stack<Token> tokens;
	private final String filename;
	int curTok = 0;

	public Parser(Stack<Token> tokens, String filename) {
		this.tokens = tokens;
		this.filename = filename;
	}

	Token nextToken() {
		try {
			return tokens.get(curTok++);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new CompilerException(null, "Code????????");
		}
	}

	private Token currentToken() {
		return tokens.get(curTok - 1);
	}

	Token seeNextToken() {
		return tokens.get(curTok);
	}

	Token token;
	private List<StatementTree> statements;
	private final Map<String, LabelTree> labels = new HashMap<>();

	public CompilationUnitTree parse() {
		try {
			token = nextToken();
		} catch (CompilerException e) {
			return null;
		}
		this.statements = new ArrayList<>();
		while (token.type != TokenType.EOF) {
			@Nullable StatementTree decl = parseStatement();
			//noinspection ConstantValue
			if (decl != null) statements.add(decl);
		}
		return new CompilationUnitTree(filename, statements);
	}

	private StatementTree parseStatement() {
		@Nullable val label = parseLabel();
		val dest = parseLValue();
		ExprTree src = parseExpr();
		Token colon = null;
		SizeExprTree expr = null;
		if (token.type == TokenType.COLON) {
			colon = token;
			token = nextToken();
			expr = parseSizeExpr();
		}
		return new StatementTree(label, dest, src, colon, expr);
	}

	private SizeExprTree parseSizeExpr() {
		if (token.type == TokenType.BITS) return parseBitsNumberExpr();
		else return parseNumberExpr();
	}

	private ExprTree parseExpr() {
		//if (token.type == TokenType.NUMBER) return parseNumberExpr();
		if (token.type == TokenType.CHARS) return parseCharLiteral();
		if (token.type == TokenType.BITS) return parseBitsNumberExpr();
		if (token.type == TokenType.IDENTIFIER) {
			if (seeNextToken().type == TokenType.OPEN_SQUARE) return parseLValue();
			if (token.text.charAt(0) == '@') return parseLabelPtr();
			return parseNumberExpr();
		}
		throw new UnexpectedTokenException(token, "Expression expected");
	}

	private ExprTree parseCharLiteral() {
		return new CharLiteralExprTree(assertAndNext(TokenType.CHARS));
	}

	private LabelReferenceExprTree parseLabelPtr() {
		Token name = assertAndNext(TokenType.IDENTIFIER);
		return new LabelReferenceExprTree(name, labels);
	}

	private BitSizeExprTree parseBitsNumberExpr() {
		Token bits = token;
		Token number = token = nextToken();
		assertToken(TokenType.NUMBER);
		token = nextToken();
		return new BitSizeExprTree(bits, new NumericLiteralExprTree(number));
	}

	private NumericLiteralExprTree parseNumberExpr() {
		Token num = token;
		token = nextToken();
		return new NumericLiteralExprTree(num);
	}

	private DereferenceExprTree parseLValue() {
		Token segment = assertAndNext(TokenType.IDENTIFIER);
		Token open = assertAndNext(TokenType.OPEN_SQUARE);
		ExprTree address = parseExpr();
		Token plus = null;
		ExprTree offset = null;
		long offsetSize = 4;
		if (token.type == TokenType.PLUS) {
			plus = token;
			token = nextToken();
			offset = parseExpr();
			if (token.type == TokenType.COLON) {
				token = nextToken();
				try {
					offsetSize = Long.parseUnsignedLong(token.text, 16);
					if (offsetSize > 4)
						throw new CompilerException(token.getLocation(), "Size expected (max value = address size)");
				} catch (Exception e) {
					throw new CompilerException(token.getLocation(), "Size expected");
				}
				token = nextToken();
			}
		}
		assertToken(TokenType.CLOSE_SQUARE);
		Token close = token;
		token = nextToken();
		return new DereferenceExprTree(segment, open, address, plus, offset, offsetSize, close);
	}

	private @Nullable LabelTree parseLabel() {
		if (token.type != TokenType.IDENTIFIER) return null;
		Token name = token;
		Token next = seeNextToken();
		if (next.type != TokenType.COLON) return null;
		nextToken(); // eat colon (next)
		token = nextToken();
		val label = new LabelTree(name, next, statements.size());
		labels.put(name.text, label);
		return label;
	}

	private Token assertAndNext(TokenType type, String expected) {
		Token ret = assertToken(type, expected);
		token = nextToken();
		return ret;
	}

	private Token assertToken(TokenType type, String expected) {
		return assertToken(token, type, expected);
	}

	private static Token assertToken(Token token, TokenType type, String expected) {
		if (token.type != type) {
			throw new UnexpectedTokenException(token, expected + " expected");
		}
		return token;
	}

	private Token assertAndNext(TokenType expected) {
		Token ret = assertToken(expected);
		token = nextToken();
		return ret;
	}

	private Token assertToken(TokenType type) {
		return assertToken(token, type);
	}

	private static Token assertToken(Token token, TokenType type) {
		return assertToken(token, type, expectedCheckName(type));
	}

	private static String expectedCheckName(TokenType type) {
		switch (type) {
			case IDENTIFIER:
				return "identifier";
			case CHARS:
				return "char literal";
			case NUMBER:
				return "number literal";
			case STRING:
				return "string literal";
			case OPEN_SQUARE:
				return "'['";
			case CLOSE_SQUARE:
				return "']'";
		}
		return type.name();
	}

	private Token assertAndNext(String expected) {
		Token ret = assertToken(expected);
		token = nextToken();
		return ret;
	}

	private Token assertToken(String expected) {
		return assertToken(token, expected);
	}

	private static Token assertToken(Token token, String expected) {
		if (!token.text.equals(expected)) {
			throw new UnexpectedTokenException(token, "'" + expected + "' expected");
		}
		return token;
	}
}
