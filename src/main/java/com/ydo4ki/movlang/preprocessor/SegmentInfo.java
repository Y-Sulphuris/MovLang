package com.ydo4ki.movlang.preprocessor;

import lombok.Data;

@Data
public class SegmentInfo {
	private final String name;
	private final long size;

	@Override
	public String toString() {
		return name;
	}
}
