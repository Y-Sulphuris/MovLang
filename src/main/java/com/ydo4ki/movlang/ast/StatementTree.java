package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.lexer.Token;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sulphuris
 * @since 01.10.2024 19:26
 */
@AllArgsConstructor
public class StatementTree implements Tree {
	private final @Nullable LabelTree label;
	private final LValueTree dest;
	private final ExprTree src;
	private final @Nullable Token colon;
	private final @Nullable SizeExprTree size;

	@Override
	public Location getLocation() {
		return Location.between(label == null ? dest : label, src);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (label != null) b.append(label).append('\n');
		b.append(dest).append(' ').append(src);
		if (size != null) {
			b.append(" :").append(size);
		}
		return b.toString();
	}
}
