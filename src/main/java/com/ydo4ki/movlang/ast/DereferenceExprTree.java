package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.lexer.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sulphuris
 * @since 01.10.2024 23:41
 */
@AllArgsConstructor
@Getter
public class DereferenceExprTree implements LValueExprTree {
	private final Token segment;
	private final Token open;
	private final ExprTree address;
	private final @Nullable Token plus;
	private final @Nullable ExprTree offset;
	private final long offsetSize;
	private final Token close;

	@Override
	public Location getLocation() {
		return Location.between(segment, close);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(segment.text).append('[');
		b.append(address);
		if (plus != null) {
			b.append(" + ").append(offset);
		}
		b.append(']');
		return b.toString();
	}
}
