package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.tokenizer.Token;
import lombok.val;

import java.util.Map;

/**
 * @author Sulphuris
 * @since 02.10.2024 14:51
 */
public class LabelReferenceExprTree implements ExprTree {
	private final Token at;
	private final Token labelName;
	private final Map<String, LabelTree> allLabelsRef;

	public LabelReferenceExprTree(Token at, Token labelName, Map<String, LabelTree> allLabels) {
		this.at = at;
		this.labelName = labelName;
		this.allLabelsRef = allLabels;
	}

	public LabelTree getLabel() {
		return allLabelsRef.get(labelName.text);
	}

	@Override
	public Location getLocation() {
		return Location.between(at, labelName);
	}

	@Override
	public String toString() {
		val label = getLabel();
		return '@' + (label == null ? "<null>" : label.getName().text);
	}
}
