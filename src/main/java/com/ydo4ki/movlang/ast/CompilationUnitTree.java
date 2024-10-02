package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import lombok.Getter;

import java.io.File;
import java.util.List;

/**
 * @author Sulphuris
 * @since 01.10.2024 19:19
 */
@Getter
public class CompilationUnitTree implements Tree {
	private final String fileName;
	private final List<StatementTree> statements;

	public CompilationUnitTree(String fileName, List<StatementTree> statements) {
		this.fileName = fileName;
		this.statements = statements;
	}

	@Override
	public Location getLocation() {
		return new Location(-1,-1,-1,-1,new File(fileName));
	}
}

