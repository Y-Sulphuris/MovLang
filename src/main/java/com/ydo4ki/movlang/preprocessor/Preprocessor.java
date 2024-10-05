package com.ydo4ki.movlang.preprocessor;

import com.ydo4ki.movlang.CompilerException;
import com.ydo4ki.movlang.lexer.Token;
import com.ydo4ki.movlang.lexer.TokenType;
import com.ydo4ki.movlang.lexer.Tokenizer;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * @author Sulphuris
 * @since 04.10.2024 11:33
 */
public class Preprocessor {

	public Preprocessor(Stack<Token> srcTokens) {
		this.srcTokens = srcTokens;
	}

	private final Map<String, Token> consts = new HashMap<>();
	private final List<SegmentInfo> segmentInfoList = new ArrayList<>();


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
		includeFiles();
		srcTokens = newTokens;
		newTokens = new Stack<>();
		readDirectives();
		srcTokens = newTokens;
		newTokens = new Stack<>();
		applyDirectives();
		return new PreprocessorInfo(newTokens, segmentInfoList);
	}

	private void includeFiles() {
		for (iterator = srcTokens.iterator(); hasNextToken(); ) {
			Token token = nextToken();
			if (token.type == TokenType.DIRECTIVE && token.text.equals("include")) {
				try {
					token = nextToken();
					String fileName = token.text;
					if (token.type != TokenType.STRING) fileName += ".movl";

					File srcFile = new File(fileName);
					StringBuilder sourceb = new StringBuilder();
					for (String str : Files.readAllLines(srcFile.toPath())) {
						sourceb.append(str).append('\n');
					}
					val includedTokens = new Tokenizer().tokenize(sourceb.toString(), srcFile);
					includedTokens.pop(); // remove EOF
					newTokens.addAll(includedTokens);
				} catch (IOException e) {
					throw new CompilerException(token.getLocation(), "Cannot read file: " + e);
				}
			} else {
				newTokens.add(token);
			}
		}
	}

	private void applyDirectives() {
		for (iterator = srcTokens.iterator(); hasNextToken(); ) {
			Token token = nextToken();
			Token nn = consts.get(token.text);
			if (nn != null) {
				token = nn.updateLocation(token.location);
			}
			newTokens.add(token);
		}
	}

	private void readDirectives() {
		for (iterator = srcTokens.iterator(); hasNextToken(); ) {
			Token token = nextToken();
			if (token.type == TokenType.COMMENT) continue;
			if (token.type == TokenType.DIRECTIVE) {
				switch (token.text) {
					case "def":
					{
						token = nextToken();
						String alias = token.text;
						token = nextToken();
						consts.put(alias, token);
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
