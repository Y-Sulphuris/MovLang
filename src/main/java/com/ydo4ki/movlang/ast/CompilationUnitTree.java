package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;

import java.io.File;

/**
 * @author Sulphuris
 * @since 01.10.2024 19:19
 */
public class CompilationUnitTree implements Tree {
	private final String fileName;

	public CompilationUnitTree(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public Location getLocation() {
		return new Location(-1,-1,-1,-1,new File(fileName));
	}
}

