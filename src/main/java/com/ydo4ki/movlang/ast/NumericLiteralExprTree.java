package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.lexer.Token;
import lombok.Getter;

/**
 * @author Sulphuris
 * @since 02.10.2024 10:10
 */
@Getter
public class NumericLiteralExprTree implements SizeExprTree {
	private final Token literal;
	private final int value;

	public NumericLiteralExprTree(Token literal) {
		this.literal = literal;
		String text = literal.text;
		if (text.charAt(0) == '_') {
			this.value = Integer.parseInt(text.substring(1));
		} else {
			this.value = Integer.parseInt(text, 16);
		}
	}

	public boolean isDecimal() {
		return literal.text.charAt(0) == '_';
	}

	@Override
	public Location getLocation() {
		return literal.getLocation();
	}

	@Override
	public String toString() {
		return Integer.toHexString(value);
	}
}
