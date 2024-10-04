package com.ydo4ki.movlang.preprocessor;

import com.ydo4ki.movlang.lexer.Token;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Sulphuris
 * @since 03.10.2024 23:34
 */
@Data
public class PreprocessorInfo {
	private final Stack<Token> tokens;
	private final long defaultSegSize = 0xFFFF;
	private final List<SegmentInfo> segmentInfoList = new ArrayList<>();
	private SegmentInfo executable = new SegmentInfo("E",0xFF);
	private SegmentInfo stdout = new SegmentInfo("C", 0xFFFF);
	{
		segmentInfoList.add(executable);
		segmentInfoList.add(stdout);
	}
}

