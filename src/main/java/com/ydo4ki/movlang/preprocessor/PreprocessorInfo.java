package com.ydo4ki.movlang.preprocessor;

import com.ydo4ki.movlang.tokenizer.Token;
import lombok.Data;

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
	private final List<SegmentInfo> segmentInfoList;
	private SegmentInfo executable = new SegmentInfo("E",0xFF);
	@Deprecated
	private SegmentInfo stdout = new SegmentInfo("C", 0xFFFF);

	private long default_seg_size = 0xFFFF;
	private int address_size = 0x4;
	private boolean implicit_seg = true;

	public PreprocessorInfo(Stack<Token> tokens, List<SegmentInfo> segmentInfoList) {
		this.tokens = tokens;
		this.segmentInfoList = segmentInfoList;

		segmentInfoList.add(executable);
	}
}

