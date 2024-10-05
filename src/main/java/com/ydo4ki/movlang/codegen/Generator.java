package com.ydo4ki.movlang.codegen;

import com.ydo4ki.movlang.CompilerException;
import com.ydo4ki.movlang.ast.*;
import com.ydo4ki.movlang.preprocessor.PreprocessorInfo;
import com.ydo4ki.movlang.preprocessor.SegmentInfo;
import lombok.val;
import org.objectweb.asm.*;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Sulphuris
 * @since 03.10.2024 10:38
 */
public class Generator {

	private final PreprocessorInfo prepInf;

	private final long defaultSegSize = 0xFFFF;
	private final Map<String, Long> segments = new HashMap<>();
	private final Map<String, byte[]> biConst = new HashMap<>();

	public Generator(PreprocessorInfo prepInf) {
		this.prepInf = prepInf;
		for (SegmentInfo info : prepInf.getSegmentInfoList()) {
			segments.put(info.getName(), info.getSize());
		}
	}

	public void generate(CompilationUnitTree cu, File outputDir) {
		outputDir.mkdirs();
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(49, ACC_PUBLIC | ACC_SUPER | ACC_FINAL, "$program", null, "java/lang/Object", null);
		cw.visitSource(cu.getFileName(), cu.getFileName());
		//writeLabels(cw, cu);
		writeInstructions(cw, cu);
		writeConstants(cw, cu);
		writeMain(cw, cu);


		cw.visitEnd();

		try {
			File clFile = new File(outputDir, "$program.class");
			//noinspection ResultOfMethodCallIgnored
			clFile.createNewFile();
			OutputStream stream = Files.newOutputStream(clFile.toPath());
			stream.write(cw.toByteArray());
			stream.close();

			val rtSrc = Thread.currentThread().getContextClassLoader().getResourceAsStream("$runtime.class");
			assert rtSrc != null;
			File rtDest = new File(outputDir, "$runtime.class");
			if (rtDest.exists()) {
				//noinspection ResultOfMethodCallIgnored
				rtDest.delete();
			}
			Files.copy(rtSrc, rtDest.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeMain(ClassWriter cw, CompilationUnitTree cu) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(-1, l);
		mv.visitFieldInsn(GETSTATIC, "$program", "instructions", "[Ljava/lang/invoke/MethodHandle;");
		mv.visitFieldInsn(GETSTATIC, "$program", "$" + prepInf.getExecutable(), "J");
		mv.visitMethodInsn(INVOKESTATIC, "$runtime", "run", "([Ljava/lang/invoke/MethodHandle;J)V");


		mv.visitInsn(RETURN);
		//args will be ignored
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void writeInstructions(ClassWriter cw, CompilationUnitTree cu) {
		List<StatementTree> statements = cu.getStatements();
		for (int i = 0; i < statements.size(); i++) {
			StatementTree statement = statements.get(i);
			writeStatement(cw, statement, i);
		}
	}

	private void writeStatement(ClassWriter cw, StatementTree statement, int i) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "$" + Integer.toHexString(i), "()V", null, null);
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(statement.getLocation().getEndLine(), l);
		writeStatement0(mv, statement);

		//mv.visitFieldInsn(GETSTATIC, "java/lang/System","out","Ljava/lang/PrintStream;");
		//mv.visitLdcInsn(Integer.toHexString(i));
		//mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/PrintStream", "println", "(Ljava/lang/PrintStream;Ljava/lang/String;)V");


		mv.visitInsn(RETURN);
		//args will be ignored
		mv.visitMaxs(1, 1);
	}

	private void writeStatement0(MethodVisitor mv, StatementTree statement) {
		Long size = statement.getBytesSize();
		if (size != null && size.equals(0L)) return;
		Long bSize = statement.getBitSize();
		String method;
		String sign;
		loadDereferensing(mv, statement.getDest());
		if (statement.getSrc() instanceof DereferenceExprTree) {
			loadDereferensing(mv, (DereferenceExprTree) statement.getSrc());
			method = movName(size);
			sign = movSign(size, bSize);
		} else {
			loadExpr(mv, statement.getSrc(), size);
			method = "put";
			String la = typeBySize(size);
			sign = "(JI" + la + ")V";
		}
		mv.visitMethodInsn(INVOKESTATIC, "$runtime", method, sign);

		if (statement.getDest().getSegment().text.equals(prepInf.getStdout().getName())) {
			mv.visitFieldInsn(GETSTATIC, "$program", "$" + prepInf.getStdout(), "J");
			mv.visitLdcInsn(prepInf.getStdout().getSize());
			mv.visitMethodInsn(INVOKESTATIC, "$runtime", "repaintConsole", "(JJ)V");
		}
	}

	private String movSign(long size, Long bSize) {
		if (size == 1 || size == 2 || size == 4 || size == 8) return "(JIJI)V";
		return "(JIJIJ)V";
	}

	private String movName(long size) {
		if (size == 1 || size == 2 || size == 4 || size == 8) return "mov" + size;
		return "mov";
	}

	private String typeBySize(long size) {
		switch ((int) size) {
			case 8:
				return "J";
			case 4:
				return "I";
			case 2:
				return "S";
			case 1:
				return "B";
		}
		return "[BJ";
	}

	private void loadExpr(MethodVisitor mv, ExprTree src, Long size) {
		if (src instanceof DereferenceExprTree) {
			loadDereferensing(mv, (DereferenceExprTree) src);
			if (size == 4) mv.visitMethodInsn(INVOKESTATIC, "$runtime", "getAddr", "(JI)I");
			else if (size == 1) {
				mv.visitMethodInsn(INVOKESTATIC, "$runtime", "get", "(JI)B");
			} else throw new CompilerException(src.getLocation(), "Illegal size: " + size);
		} else if (src instanceof NumericLiteralExprTree) {
			loadNumberConst(mv, (NumericLiteralExprTree) src, size);
		} else if (src instanceof CharLiteralExprTree) {
			loadCharConst(mv, (CharLiteralExprTree) src, size);
		} else if (src instanceof LabelReferenceExprTree) {
			loadLabelReference(mv, (LabelReferenceExprTree) src);
		} else {
			throw new UnsupportedOperationException(src.getClass().getSimpleName());
		}
	}

	private void loadLabelReference(MethodVisitor mv, LabelReferenceExprTree src) {
		loadIntValue(mv, src.getLabel().getI());
	}


	private void loadIntValue(MethodVisitor mv, int value) {
		if (value >= -1 && value <= 5) {
			mv.visitInsn(ICONST_0 + value);
		} else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			mv.visitIntInsn(BIPUSH, value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			mv.visitIntInsn(SIPUSH, value);
		} else {
			mv.visitLdcInsn(value);
		}
	}

	private void loadCharConst(MethodVisitor mv, CharLiteralExprTree src, Long _Size) {
		long size = _Size;
		if (size == 8) {
			mv.visitLdcInsn((long) src.getCh().text.charAt(0));
		} else if (size == 4 || size == 2 || size == 1) {
			loadIntValue(mv, src.getCh().text.charAt(0));
		} else {
			loadBIConst(mv, BigInteger.valueOf(src.getCh().text.charAt(0)), size);
			//throw new UnsupportedOperationException(String.valueOf(_Size));
		}
	}

	private void loadNumberConst(MethodVisitor mv, NumericLiteralExprTree src, Long _Size) {
		long size = _Size;
		if (size == 8) {
			mv.visitLdcInsn(src.getValue().longValueExact());
		} else if (size == 4 || size == 2 || size == 1) {
			loadIntValue(mv, src.getValue().intValue());
		} else {
			loadBIConst(mv, src.getValue(), size);
			//throw new UnsupportedOperationException(String.valueOf(_Size));
		}
	}

	private void loadBIConst(MethodVisitor mv, BigInteger value, long size) {
		String name = "bi" + value.toString(16) + "$" + size;
		byte[] data = value.toByteArray();
		if (!biConst.containsKey(name))
			biConst.put(name, data);
		mv.visitFieldInsn(GETSTATIC, "$program", name, "[B");
		mv.visitLdcInsn(size);
	}

	private void loadDereferensing(MethodVisitor mv, DereferenceExprTree dest) {
		mv.visitFieldInsn(GETSTATIC, "$program", "$" + dest.getSegment().text, "J");
		loadExpr(mv, dest.getAddress(), 4L);
		val offset = dest.getOffset();
		if (offset != null) {
			loadExpr(mv, offset, dest.getOffsetSize()); // default offset size
			mv.visitInsn(IADD);
		}

		String name = dest.getSegment().text;
		if (!segments.containsKey(name)) {
			segments.put(name, defaultSegSize);
		}
	}

	/*
	private void writeLabels(ClassWriter cw, CompilationUnitTree cu) {
		for (StatementTree statement : cu.getStatements()) {
			LabelTree lb = statement.getLabel();
			if (lb != null) writeLabel(cw, lb);
		}
	}

	private void writeLabel(ClassWriter cw, LabelTree lb) {
		FieldVisitor fv = cw.visitField(ACC_STATIC | ACC_FINAL, "_lb_" + lb.getName().text, "I", null, lb.getI());
		fv.visitEnd();
	}
	*/

	private void writeConstants(ClassWriter cw, CompilationUnitTree cu) {
		int size = cu.getStatements().size();

		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "<clinit>", "()V", null, null);
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(-1, l);


		FieldVisitor fv = cw.visitField(ACC_STATIC | ACC_FINAL,
				"instructions", "[Ljava/lang/invoke/MethodHandle;", null, null
		);
		fv.visitEnd();

		// instructions = $runtime.getInstructions($program.class, MethodHandles.lookup(), size);
		mv.visitLdcInsn(Type.getType("L$program;"));
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;");
		mv.visitLdcInsn(size);
		mv.visitMethodInsn(INVOKESTATIC, "$runtime", "getInstructions", "(Ljava/lang/Class;Ljava/lang/invoke/MethodHandles$Lookup;I)[Ljava/lang/invoke/MethodHandle;");
		mv.visitFieldInsn(PUTSTATIC, "$program", "instructions", "[Ljava/lang/invoke/MethodHandle;");

		for (Map.Entry<String, Long> entry : segments.entrySet()) {
			String name = entry.getKey();
			long segSize = entry.getValue();
			writeSegField(cw, name);

			String method;
			if (name.equals(prepInf.getStdout().getName()))
				method = "consoleSegment";
			else method = "segment";

			writeSegInit(mv, name, segSize, method);
		}

		for (Map.Entry<String, byte[]> entry : biConst.entrySet()) {
			String name = entry.getKey();
			byte[] content = entry.getValue();
			writeBiConstField(cw, mv, name, content);
		}

		mv.visitInsn(RETURN);
		//args will be ignored
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void writeBiConstField(ClassWriter cw, MethodVisitor mv, String name, byte[] content) {
		FieldVisitor fv = cw.visitField(ACC_STATIC | ACC_FINAL, name, "[B", null, null);
		fv.visitEnd();

		loadByteArray(mv, content);
		mv.visitFieldInsn(PUTSTATIC, "$program", name, "[B");
	}

	private void loadByteArray(MethodVisitor mv, byte[] content) {
		loadIntValue(mv, content.length);
		mv.visitIntInsn(NEWARRAY, T_BYTE);
		for (int i = 0; i < content.length; i++) {
			mv.visitInsn(DUP);
			loadIntValue(mv, i);
			loadIntValue(mv, content[i]);
			mv.visitInsn(BASTORE);
		}
	}

	private void writeSegInit(MethodVisitor mv, String name, long segSize, String method) {
		mv.visitLdcInsn(segSize);
		mv.visitMethodInsn(INVOKESTATIC, "$runtime", method, "(J)J");
		mv.visitFieldInsn(PUTSTATIC, "$program", '$' + name, "J");
	}

	private void writeSegField(ClassWriter cw, String name) {
		FieldVisitor fv = cw.visitField(ACC_STATIC | ACC_FINAL,
				'$' + name, "J", null, null
		);
		fv.visitEnd();
	}
}
