package com.ydo4ki.movlang.preprocessor;

import com.ydo4ki.movlang.Compiler;
import com.ydo4ki.movlang.CompilerException;
import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.lexer.Token;
import com.ydo4ki.movlang.lexer.TokenType;
import com.ydo4ki.movlang.lexer.Tokenizer;
import com.ydo4ki.movlang.lexer.UnexpectedTokenException;
import com.ydo4ki.movlang.misc.OneOrMore;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Sulphuris
 * @since 04.10.2024 11:33
 */
public class Preprocessor {

	public Preprocessor(Stack<Token> srcTokens) {
		this.srcTokens = srcTokens;
	}

	private final Map<String, Token> defs = new HashMap<>();
	private final Map<String, Macro> macros = new HashMap<>();
	private final List<SegmentInfo> segmentInfoList = new ArrayList<>();


	private Stack<Token> srcTokens;
	private Stack<Token> newTokens = new Stack<>();
	private ListIterator<Token> iterator;

	private Token nextToken() {
		return iterator.next();
	}

	private boolean hasNextToken() {
		return iterator.hasNext();
	}


	public PreprocessorInfo preprocess() {
		includeFiles();
		readDirectives();
		applyDirectives();
		return new PreprocessorInfo(newTokens, segmentInfoList);
	}

	private void includeFiles() {
		boolean wasAnythingIncluded;
		do {
			wasAnythingIncluded = false;
			for (iterator = srcTokens.listIterator(); hasNextToken(); ) {
				Token token = nextToken();
				if (token.type == TokenType.DIRECTIVE && token.text.equals("include")) {
					try {
						token = nextToken();
						String fileName = token.text;
						if (token.type != TokenType.STRING) fileName += ".movl";

						File srcFile = new File(fileName);
						val includedTokens = new Tokenizer().tokenize(Compiler.getSource(srcFile), srcFile);
						includedTokens.pop(); // remove EOF
						newTokens.addAll(includedTokens);
						wasAnythingIncluded = true;
					} catch (IOException e) {
						throw new CompilerException(token.getLocation(), "Cannot read file: " + e);
					}
				} else {
					newTokens.add(token);
				}
			}
			srcTokens = newTokens;
			newTokens = new Stack<>();
		} while (wasAnythingIncluded);
	}

	private void applyDirectives() {
		for (iterator = srcTokens.listIterator(); hasNextToken(); ) {
			Token token = nextToken();
			Token nn = defs.get(token.text);
			Macro macro;
			if (nn != null) {
				token = nn.updateLocation(token.location);
			} else if ((macro = macros.get(token.text)) != null) {
				Map<String, OneOrMore<Token>> args = new HashMap<>();
				for (Token macroToken : macro.getMacroText()) {
					token = nextToken();
					if (macroToken.type == TokenType.DIRECTIVE_ARG) {
						args.put(macroToken.text, OneOrMore.of(token));
					} else {
						if (macroToken.type != token.type || !Objects.equals(macroToken.text, token.text)) {
							throw new UnexpectedTokenException(token, "\"" + macroToken.text + "\" expected");
						}
					}
				}
				for (Token unfoldToken : macro.getUnfolds()) {
					if (unfoldToken.type == TokenType.DIRECTIVE_ARG) {
						for (Token t : args.get(unfoldToken.text)) {
							nn = defs.get(t.text);
							if (nn != null) t = nn.updateLocation(t.getLocation());
							newTokens.add(t);
						}
					}
					else {
						nn = defs.get(unfoldToken.text);
						if (nn != null) unfoldToken = nn.updateLocation(unfoldToken.getLocation());
						newTokens.add(unfoldToken);
					}
				}
			} else {
				newTokens.add(token);
			}
		}
	}

	private <T> void putChecked(Map<String, T> target, String name, T value, Location location) {
		if (defs.containsKey(name)) throw new CompilerException(location, "There is already an alias with that name");
		if (macros.containsKey(name)) throw new CompilerException(location, "There is already a macro with that name");
		target.put(name, value);
	}

	private void readDirectives() {
		for (iterator = srcTokens.listIterator(); hasNextToken(); ) {
			Token token = nextToken();
			if (token.type == TokenType.COMMENT) continue;
			if (token.type == TokenType.DIRECTIVE) {
				switch (token.text) {
					case "def": {
						token = nextToken();
						String alias = token.text;
						token = nextToken();
						putChecked(defs, alias, token, token.getLocation());
						defs.put(alias, token);
					}
					break;
					case "macro": {
						Macro macro = parseMacro();
						//System.out.println(macro);
						putChecked(macros, macro.getName().text, macro, token.getLocation());
					}
					break;
					default:
						throw new CompilerException(token.getLocation(), "unknown directive: " + token.text);
				}
			} else {
				newTokens.add(token);
			}
		}
		srcTokens = newTokens;
		newTokens = new Stack<>();
	}


	private Macro parseMacro() {
		Token name = nextToken();
		if (name.type != TokenType.IDENTIFIER)
			throw new UnexpectedTokenException(name, "macro name expected");

		Collection<Token> macroText = new ArrayList<>();
		Token token = nextToken();

		//noinspection ConditionalBreakInInfiniteLoop
		while (true) {
			if (token.type == TokenType.DIRECTIVE && token.text.equals("unfolds"))
				break;
			macroText.add(token);
			token = nextToken();
			iterator.remove();
		}
		// token = #unfolds there
		Collection<Token> unfolds = new ArrayList<>();
		token = nextToken();

		//noinspection ConditionalBreakInInfiniteLoop
		while (true) {
			if (token.type == TokenType.DIRECTIVE && token.text.equals("end_macro"))
				break;
			unfolds.add(token);
			token = nextToken();
		}

		// token = #end_macro there
		return new Macro(name, macroText, unfolds);
	}
}
