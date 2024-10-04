package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.lexer.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Sulphuris
 * @since 02.10.2024 17:14
 */
@AllArgsConstructor
@Getter
public class CharLiteralExprTree implements ExprTree {
	private final Token ch;

	@Override
	public Location getLocation() {
		return ch.getLocation();
	}

	@Override
	public String toString() {
		return Integer.toHexString(ch.text.charAt(0)) + "'" + ch.text + "'";
	}
}
