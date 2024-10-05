package com.ydo4ki.movlang;

import com.ydo4ki.movlang.ast.Tree;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Sulphuris
 * @since 01.10.2024 11:46
 */
@Data
public final class Location {
	private final int startPos;
	private final int endPos;
	private final int startLine;
	private final int endLine;
	private final File sourceFile;

	public static Location between(@NotNull Tree start,@NotNull Tree end) {
		return between(start.getLocation(), end.getLocation());
	}
	public static Location between(Location start, Location end) {
		if (!end.getSourceFile().equals(start.getSourceFile()))
			return start;
		return new Location(start.startPos,end.endPos,start.startLine,end.endLine, end.getSourceFile());
	}

	public static boolean lineIntersects(Location a, Location b) {
		if (!a.getSourceFile().getName().equals(b.getSourceFile().getName())) return false;
		return !(a.startLine > b.endLine || a.endLine < b.startLine);
	}
}
