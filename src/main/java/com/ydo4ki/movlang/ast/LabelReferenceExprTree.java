package com.ydo4ki.movlang.ast;

import com.ydo4ki.movlang.Location;
import com.ydo4ki.movlang.lexer.Token;
import lombok.val;

import java.util.Map;
import java.util.Set;

/**
 * @author Sulphuris
 * @since 02.10.2024 14:51
 */
public class LabelReferenceExprTree implements ExprTree {
	private final Token labelName;
	private final Map<String, LabelTree> allLabels;

	public LabelReferenceExprTree(Token labelName, Map<String, LabelTree> allLabels) {
		this.labelName = labelName;
		this.allLabels = allLabels;
	}

	public LabelTree getLabel() {
		return allLabels.get(labelName.text.substring(1));
	}

	@Override
	public Location getLocation() {
		return labelName.getLocation();
	}

	@Override
	public String toString() {
		val label = getLabel();
		return '@' + (label == null ? "<null>" : label.getName().text);
	}
}
