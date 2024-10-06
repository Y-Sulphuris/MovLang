package com.ydo4ki.movlang.preprocessor;

import com.ydo4ki.movlang.tokenizer.Token;
import lombok.Data;

import java.util.List;

// весь код был атличьна стырен с C32 и не переделан

/**
 * @author Sulphuris
 * @since 05.10.2024 16:21
 */
@Data
class Macro {
	private final Token name;
	private final List<Token> macroText;
	private final List<Token> unfolds;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("#macro ").append(name.text).append('\n');
		for (Token token : macroText) {
			builder.append(token.text).append(' ');
		}
		builder.append("\n#unfolds\n");
		for (Token unfold : unfolds) {
			builder.append(unfold.text).append(' ');
		}
		builder.append("\n#end_macro");
		return builder.toString();
	}
}
