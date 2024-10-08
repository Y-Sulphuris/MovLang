package com.ydo4ki.movlang;

import com.ydo4ki.movlang.ast.Parser;
import com.ydo4ki.movlang.ast.StatementTree;
import com.ydo4ki.movlang.codegen.Generator;
import com.ydo4ki.movlang.tokenizer.Token;
import com.ydo4ki.movlang.tokenizer.Tokenizer;
import com.ydo4ki.movlang.preprocessor.Preprocessor;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Sulphuris
 * @since 01.10.2024 11:33
 */
public class Compiler {
	private final File srcFile;

	public Compiler(File srcFile) {
		this.srcFile = srcFile;
	}

	private static void error(String msg) {
		System.err.println(msg);
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			error("File name expected");
		}
		String fileName = args[0];
		File file = new File(fileName);
		if (!file.isFile()) {
			error("File not found: " + file.getAbsolutePath());
		}
		System.out.println("Compiling...");
		long start = System.currentTimeMillis();
		Compiler compiler = new Compiler(file);
		File outputFile = new File("out/jvm/");
		compiler.compile(outputFile);


		long end = System.currentTimeMillis();
		System.out.println("Finished (total: " + (end - start) + "ms)\n");

		//if (outputFile.exists()) {
		//	System.out.println("Starting process...\n");
		//	proc("java -cp out/jvm/; $program");
		//} else {
		//	System.out.println("Compilation error");
		//}
	}

	public static String source;

	private static final Map<File, String> sources = new HashMap<>();
	public static String getSource(File srcFile) throws IOException {
		String src = sources.get(srcFile);
		if (src == null) {
			StringBuilder sourceb = new StringBuilder();
			for (String str : Files.readAllLines(srcFile.toPath())) {
				sourceb.append(str).append('\n');
			}
			src = sourceb.toString();
			sources.put(srcFile, src);
		}
		return src;
	}

	@Getter
	@Nullable
	private static Stack<Token> finalTokens;

	private void compile(File outputFile) throws IOException {
		source = getSource(srcFile);
		val prepInf = new Preprocessor(new Tokenizer().tokenize(source, srcFile)).preprocess();
		//for (Token token : tokens) {
		//	System.out.println(token);
		//}
		finalTokens = prepInf.getTokens();
		val cu = new Parser(finalTokens, srcFile.getName()).parse();
		//System.out.println("\nStatements:\n");
		//for (StatementTree statement : cu.getStatements()) {
		//	System.out.println(statement);
		//}
		new Generator(prepInf).generate(cu,outputFile);
	}


	private static void proc(String cmd) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(cmd);


		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		String s;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}
		// errors
		while ((s = stdError.readLine()) != null) {
			System.out.println(s);
		}
	}
}
