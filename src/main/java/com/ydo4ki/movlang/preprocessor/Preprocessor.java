package com.ydo4ki.movlang.preprocessor;

import com.ydo4ki.movlang.lexer.Token;
import com.ydo4ki.movlang.lexer.TokenType;

import java.util.*;

/**
 * @author Sulphuris
 * @since 04.10.2024 11:33
 */
public class Preprocessor {

	public Preprocessor(Stack<Token> srcTokens) {
		this.srcTokens = srcTokens;
	}

	private final Map<String, String> consts = new HashMap<>();


	private Stack<Token> srcTokens;
	private Stack<Token> newTokens = new Stack<>();
	private Iterator<Token> iterator;
	private Token nextToken() {
		return iterator.next();
	}
	private boolean hasNextToken() {
		return iterator.hasNext();
	}


	public PreprocessorInfo preprocess() {
		readDirectives();
		srcTokens = newTokens;
		newTokens = new Stack<>();
		applyDirectives();
		return new PreprocessorInfo(newTokens);
	}

	private void applyDirectives() {
		for (iterator = srcTokens.iterator(); hasNextToken(); ) {
			Token token = nextToken();
			String nn = consts.get(token.text);
			if (nn != null) token.text = nn;
			newTokens.add(token);
		}
	}

	private void readDirectives() {
		for (iterator = srcTokens.iterator(); hasNextToken(); ) {
			Token token = nextToken();
			if (token.type == TokenType.COMMENT) continue;
			if (token.type == TokenType.DIRECTIVE) {
				switch (token.text) {
					case "const":
					{
						token = nextToken();
						String alias = token.text;
						token = nextToken();
						String value = token.text;
						consts.put(alias, value);
					} break;
					/*case "macro":
					{
						Macro macro = parseMacro();
						System.out.println(macro);
						macros.put(macro.getName().text, macro);
					} break;*/
				}
			} else {
				newTokens.add(token);
			}
		}
	}
}
