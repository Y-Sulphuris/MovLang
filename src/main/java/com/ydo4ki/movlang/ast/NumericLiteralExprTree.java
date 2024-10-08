package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.CompilerException;
import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.tokenizer.Token;
import com.ydo4ki.movlang.tokenizer.UnexpectedTokenException;
import lombok.Getter;

import java.math.BigInteger;

/**
 * @author Sulphuris
 * @since 02.10.2024 10:10
 */
@Getter
public class NumericLiteralExprTree implements SizeExprTree {
	private final Token literal;
	private final BigInteger value;

	public NumericLiteralExprTree(Token literal) {
		this.literal = literal;
		String text = literal.text;
		try {
			if (text.charAt(0) == '_') {
				this.value = new BigInteger(text.substring(1));
			} else {
				this.value = new BigInteger(text, 16);
			}
		} catch (NumberFormatException e) {
			throw new UnexpectedTokenException(literal, "Number expected", e);
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
		return value.toString(16);
	}
}
