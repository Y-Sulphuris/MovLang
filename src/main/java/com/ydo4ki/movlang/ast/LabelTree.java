package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.tokenizer.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Sulphuris
 * @since 01.10.2024 19:22
 */
@AllArgsConstructor
@Getter
public class LabelTree implements Tree {
	private final Token name;
	private final Token colon;
	private final int i;

	@Override
	public Location getLocation() {
		return Location.between(name, colon);
	}

	@Override
	public String toString() {
		return name.text + "(" + i + "):";
	}
}
