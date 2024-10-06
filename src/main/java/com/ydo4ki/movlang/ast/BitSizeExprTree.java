package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.tokenizer.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Sulphuris
 * @since 02.10.2024 10:15
 */
@AllArgsConstructor
@Getter
public class BitSizeExprTree implements SizeExprTree {
	private final Token bits;
	private final NumericLiteralExprTree expr;
	@Override
	public Location getLocation() {
		return Location.between(bits, expr);
	}
}
