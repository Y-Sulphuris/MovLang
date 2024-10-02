package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.lexer.Token;
import lombok.AllArgsConstructor;

/**
 * @author Sulphuris
 * @since 01.10.2024 19:22
 */
@AllArgsConstructor
public class LabelTree implements Tree {
	private final Token name;
	private final Token colon;

	@Override
	public Location getLocation() {
		return Location.between(name, colon);
	}
}
