package com.ydo4ki.movlang.preprocessor;

import com.ydo4ki.movlang.lexer.Token;
import com.ydo4ki.movlang.lexer.TokenType;

import java.util.Stack;

/**
 * @author Sulphuris
 * @since 04.10.2024 11:33
 */
public class Preprocessor {
	private final Stack<Token> srcTokens;

	public Preprocessor(Stack<Token> srcTokens) {
		this.srcTokens = srcTokens;
	}

	public PreprocessorInfo preprocess() {
		PreprocessorInfo info = new PreprocessorInfo(new Stack<>());


		Stack<Token> newTokens = info.getTokens();
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < srcTokens.size(); i++) {
			Token tok = srcTokens.get(i);
			if (tok.type == TokenType.COMMENT) continue;
			newTokens.add(tok);
		}


		return info;
	}
}
