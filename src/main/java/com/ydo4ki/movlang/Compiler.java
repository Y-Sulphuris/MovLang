package com.ydo4ki.movlang;

import com.ydo4ki.movlang.lexer.Token;
import com.ydo4ki.movlang.lexer.Tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

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
		if (true) return;
		if (outputFile.exists()) {
			System.out.println("Starting process...\n");
			proc("java -cp out/jvm/; $mov");
		} else {
			System.out.println("Compilation error");
		}
	}

	private void compile(File outputFile) throws IOException {
		StringBuilder sourceb = new StringBuilder();
		for (String str : Files.readAllLines(srcFile.toPath())) {
			sourceb.append(str).append('\n');
		}
		String source = sourceb.toString();
		for (Token token : new Tokenizer().tokenize(source, srcFile)) {
			System.out.println(token);
		}
	}


	private static void proc(String cmd) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(cmd);


		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		String s = null;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}
		// errors
		while ((s = stdError.readLine()) != null) {
			System.out.println(s);
		}
	}
}
