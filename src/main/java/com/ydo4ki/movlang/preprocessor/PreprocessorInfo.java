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
	private final List<SegmentInfo> segmentInfoList;
	private SegmentInfo executable = new SegmentInfo("E",0xFF);
	@Deprecated
	private SegmentInfo stdout = new SegmentInfo("C", 0xFFFF);

	private final long default_seg_size;
	private final long address_size;
	private final boolean implicit_seg;

	public PreprocessorInfo(Stack<Token> tokens, List<SegmentInfo> segmentInfoList, long defaultSegSize, int addressSize, boolean implicitSeg) {
		this.tokens = tokens;
		this.segmentInfoList = segmentInfoList;
		default_seg_size = defaultSegSize;
		address_size = addressSize;
		implicit_seg = implicitSeg;

		segmentInfoList.add(executable);
	}
}

