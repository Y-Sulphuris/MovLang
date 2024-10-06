package com.ydo4ki.movlang.preprocessor;

import com.ydo4ki.movlang.Compiler;
import com.ydo4ki.movlang.CompilerException;
import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.tokenizer.*;
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

	private final Map<String, Macro> macros = new HashMap<>();
	private final List<SegmentInfo> segmentInfoList = new ArrayList<>();


	private Stack<Token> srcTokens;
	private Stack<Token> newTokens = new Stack<>();
	private int curTok;

	private Token nextToken() {
		return srcTokens.get(curTok++);
	}

	private Token prevToken() {
		return srcTokens.get(--curTok);
	}

	private boolean hasNextToken() {
		return curTok < srcTokens.size();
	}

	private long default_seg_size = 0xFFFF;
	private int address_size = 0x4;
	private boolean implicit_seg = true;

	public PreprocessorInfo preprocess() {
		includeFiles();
		readMacrosAndDirectives();
		applyMacros();
		readAndApplyDefs();
		return new PreprocessorInfo(newTokens, segmentInfoList, default_seg_size, address_size, implicit_seg);
	}

	private void readAndApplyDefs() {
		final Map<String, Token> defs = new HashMap<>();
		for (curTok = 0; hasNextToken(); ) {
			Token token = nextToken();
			if (token.type == TokenType.DIRECTIVE) {
				switch (token.text) {
					case "def": {
						token = nextToken();
						String alias = token.text;
						token = nextToken();
						putChecked(defs, alias, token, token.getLocation());
					} break;
					case "undef": {
						token = nextToken();
						String alias = token.text;
						defs.remove(alias);
					} break;
				}
			} else {
				Token nn = null;
				if (token.type == TokenType.IDENTIFIER || token.type == TokenType.NUMBER)
					nn = defs.get(token.text);
				if (nn != null) {
					token = nn.updateLocation(token.location);
				}
				newTokens.add(token);
			}
		}
	}

	private void includeFiles() {
		boolean wasAnythingIncluded;
		do {
			wasAnythingIncluded = false;
			for (curTok = 0; hasNextToken(); ) {
				Token token = nextToken();
				if (token.type == TokenType.DIRECTIVE && token.text.equalsIgnoreCase("include")) {
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

	private void applyMacros() {
		for (curTok = 0; hasNextToken(); ) {
			Token token = nextToken();
			Macro macro;
			if ((macro = macros.get(token.text)) != null) {
				applyMacro(macro, token);
				continue;
			}
			newTokens.add(token);
		}
		srcTokens = newTokens;
		newTokens = new Stack<>();
	}

	private void popOrDisbalance(ArrayList<BracketsType> brackets, BracketsType pop, Location location) {
		if (!brackets.isEmpty()) {
			BracketsType removed = brackets.remove(brackets.size() - 1);
			if (removed == pop) return;
			brackets.add(removed);
		}
		disbalance(brackets, location);
	}

	private void disbalance(ArrayList<BracketsType> brackets, Location location) throws CompilerException {
		StringBuilder expected = new StringBuilder();
		for (int i = brackets.size() - 1; i >= 0; i--) {
			BracketsType bracket = brackets.get(i);
			expected.append(bracket.close);
		}
		throw new CompilerException(location, "Brackets disbalance: '" + expected + "' expected");
	}

	private void addArgsToken(Map<String, List<Token>> args, Token macroToken, Token token) {
		List<Token> list = args.computeIfAbsent(macroToken.text, k -> new ArrayList<>());
		list.add(token);
	}


	private void applyMacro(Macro macro, Token token) {
		Map<String, List<Token>> args = new HashMap<>();
		ArrayList<BracketsType> brackets = new ArrayList<>();

		balance(brackets, token);
		List<Token> macroText = macro.getMacroText();
		for (int i = 0; i < macroText.size(); i++) {
			Token macroToken = macroText.get(i);
			token = nextToken();
			//System.out.println("mt: " + macroToken);
			//System.out.println("t: " + token);
			balance(brackets, token);
			if (macroToken.type == TokenType.DIRECTIVE_ARG) {
				int startBalance = brackets.size();

				for (; ; ) {
					addArgsToken(args, macroToken, token);
					token = nextToken();
					//System.out.println("t(a): " + token);
					if (token.isBracket() && (i == macroText.size() - 1 || token.type == macroText.get(i + 1).type)) {
						curTok--;
						//System.out.println(1);
						break;
					}
					if (brackets.size() == startBalance - 1 || startBalance == 0) {
						curTok--;
						//System.out.println(2);
						break;
					}
					balance(brackets, token);
				}
			} else {
				if (macroToken.type != token.type || !Objects.equals(macroToken.text, token.text)) {
					throw new UnexpectedTokenException(token, "\"" + macroToken.text + "\" expected");
				}
			}
		}
		/*for (Map.Entry<String, List<Token>> entry : args.entrySet()) {
			System.out.println(entry.getKey() + " -> " + entry.getValue());
		}*/

		if (!brackets.isEmpty()) disbalance(brackets, token.getLocation());

		for (Token unfoldToken : macro.getUnfolds()) {
			if (unfoldToken.type == TokenType.DIRECTIVE_ARG) {
				val ts = args.get(unfoldToken.text);
				if (ts == null) throw new CompilerException(unfoldToken.getLocation(), "Unknown macro argument");
				newTokens.addAll(ts);
			} else {
				newTokens.add(unfoldToken.updateLocation(token.getLocation()));
			}
		}
	}

	private void balance(ArrayList<BracketsType> brackets, Token token) {
		if (token.type == TokenType.OPEN) {
			brackets.add(BracketsType.FIGURE);
		} else if (token.type == TokenType.OPEN_SQUARE) {
			brackets.add(BracketsType.SQUARE);
		} else if (token.type == TokenType.OPEN_ROUND) {
			brackets.add(BracketsType.ROUND);
		} else if (token.type == TokenType.OPEN_TRIANGLE) {
			brackets.add(BracketsType.TRIANGLE);
		} else if (token.type == TokenType.CLOSE) {
			popOrDisbalance(brackets, BracketsType.FIGURE, token.getLocation());
		} else if (token.type == TokenType.CLOSE_SQUARE) {
			popOrDisbalance(brackets, BracketsType.SQUARE, token.getLocation());
		} else if (token.type == TokenType.CLOSE_ROUND) {
			popOrDisbalance(brackets, BracketsType.ROUND, token.getLocation());
		} else if (token.type == TokenType.CLOSE_TRIANGLE) {
			popOrDisbalance(brackets, BracketsType.TRIANGLE, token.getLocation());
		}
	}

	private <T> void putChecked(Map<String, T> target, String name, T value, Location location) {
		if (macros.containsKey(name)) throw new CompilerException(location, "There is already a macro with that name");
		target.put(name, value);
	}

	private void readMacrosAndDirectives() {
		for (curTok = 0; hasNextToken(); ) {
			Token token = nextToken();
			if (token.type == TokenType.COMMENT) continue;
			if (token.type == TokenType.DIRECTIVE) {
				switch (token.text.toLowerCase(Locale.ROOT)) {
					case "macro": {
						Macro macro = parseMacro();
						//System.out.println(macro);
						putChecked(macros, macro.getName().text, macro, token.getLocation());
					} break;
					case "segment": {
						token = nextToken();
						String seg = token.text;
						token = nextToken();
						try {
							long size = Long.parseUnsignedLong(token.text, 16);
							segmentInfoList.add(new SegmentInfo(seg, size));
						} catch (NumberFormatException e) {
							throw new CompilerException(token.getLocation(), "Segment size expected", e);
						}
					} break;
					case "rule": {
						token = nextToken();
						String rule = token.text;
						token = nextToken();
						String value = token.text;
						try {
							switch (rule) {
								case "default_seg_size": {
									default_seg_size = Long.parseUnsignedLong(value, 16);
								} break;
								case "address_size": {
									address_size = Integer.parseUnsignedInt(value, 16);
									switch (address_size) {
										case 1:
										case 2:
										case 4:
										case 8:
											break;
										default:
											throw new CompilerException(token.getLocation(), "Invalid address size: " + address_size);
									}
								} break;
								case "implicit_seg": {
									int val = Integer.parseUnsignedInt(value, 16);
									if (val != 0 && val != 1)
										throw new CompilerException(token.getLocation(), "Invalid value: " + val + "(0 or 1 expected)");
									implicit_seg = val == 1;
								} break;
							}
						} catch (CompilerException e) {
							throw e;
						} catch (Exception e) {
							throw new CompilerException(token.getLocation(), "Cannot parse rule", e);
						}
					} break;
					case "def":
					case "undef":
						newTokens.add(token); // pass
						break;
					default:
						throw new CompilerException(token.getLocation(), "unknown directive: #" + token.text);
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

		List<Token> macroText = new ArrayList<>();
		Token token = nextToken();

		//noinspection ConditionalBreakInInfiniteLoop
		while (true) {
			if (token.type == TokenType.DIRECTIVE && token.text.equals("unfolds"))
				break;
			macroText.add(token);
			token = nextToken();
		}
		// token = #unfolds there
		List<Token> unfolds = new ArrayList<>();
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
