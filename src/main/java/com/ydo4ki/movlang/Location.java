package com.ydo4ki.movlang;

import lombok.Data;

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

	public static Location between(Location start, Location end) {
		if (!end.getSourceFile().equals(start.getSourceFile()))
			return start;
		return new Location(start.startPos,end.endPos,start.startLine,end.endLine, end.getSourceFile());
	}
}
