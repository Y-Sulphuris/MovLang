package com.ydo4ki.movlang.codegen;

import com.ydo4ki.movlang.ast.*;
import com.ydo4ki.movlang.lexer.UnexpectedTokenException;
import com.ydo4ki.movlang.preprocessor.PreprocessorInfo;
import com.ydo4ki.movlang.preprocessor.SegmentInfo;
import lombok.val;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Sulphuris
 * @since 03.10.2024 10:38
 */
public class Generator {

	private final PreprocessorInfo prepInf;

	private final long defaultSegSize = 0xFFFF;
	private final Map<String, Long> segments = new HashMap<>();

	public Generator(PreprocessorInfo prepInf) {
		this.prepInf = prepInf;
		for (SegmentInfo info : prepInf.getSegmentInfoList()) {
			segments.put(info.getName(), info.getSize());
		}
	}

	public void generate(CompilationUnitTree cu, File outputDir) {
		outputDir.mkdirs();
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		cw.visit(49, ACC_PUBLIC | ACC_SUPER | ACC_FINAL, "$program", null, "java/lang/Object", null);
		cw.visitSource(cu.getFileName(), cu.getFileName());
		//writeLabels(cw, cu);
		writeInstructions(cw, cu);
		writeConstants(cw, cu);

		try {
			File clFile = new File(outputDir, "$program.class");
			//noinspection ResultOfMethodCallIgnored
			clFile.createNewFile();
			OutputStream stream = Files.newOutputStream(clFile.toPath());
			stream.write(cw.toByteArray());
			stream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

		mv.visitInsn(RETURN);
	}

	private void writeStatement0(MethodVisitor mv, StatementTree statement) {
		Long size = statement.getBytesSize();
		Long bSize = statement.getBitSize();
		loadDereferensing(mv, statement.getDest());
		loadExpr(mv, statement.getSrc(), size);
		String method = "mov"+size;
		String la = typeBySize(size);
		mv.visitMethodInsn(INVOKESTATIC, "$runtime", method, "(JI"+la+")V");
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
		return "[B";
	}

	private void loadExpr(MethodVisitor mv, ExprTree src, Long size) {
		if (src instanceof DereferenceExprTree) {
			loadDereferensing(mv, (DereferenceExprTree) src);
			mv.visitMethodInsn(INVOKESTATIC, "$runtime", "getAddr", "(JI)I");
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
			mv.visitIntInsn(BIPUSH,value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			mv.visitIntInsn(SIPUSH,value);
		} else {
			mv.visitLdcInsn(value);
		}
	}

	private void loadCharConst(MethodVisitor mv, CharLiteralExprTree src, Long _Size) {
		long size = _Size;
		if (size == 8) {
			mv.visitLdcInsn((long)src.getCh().text.charAt(0));
		} else if (size == 4) {
			mv.visitLdcInsn((int)src.getCh().text.charAt(0));
		} else if (size == 2) {
			mv.visitIntInsn(SIPUSH, src.getCh().text.charAt(0));
		} else if (size == 1) {
			mv.visitIntInsn(BIPUSH, src.getCh().text.charAt(0));
		} else throw new UnsupportedOperationException(String.valueOf(_Size));
	}

	private void loadNumberConst(MethodVisitor mv, NumericLiteralExprTree src, Long _Size) {
		long size = _Size;
		if (size == 8) {
			mv.visitLdcInsn(src.getValue().longValueExact());
		} else if (size == 4) {
			mv.visitLdcInsn(src.getValue().intValue());
		} else if (size == 2) {
			mv.visitIntInsn(SIPUSH, src.getValue().shortValue());
		} else if (size == 1) {
			mv.visitIntInsn(BIPUSH, src.getValue().byteValue());
		} else throw new UnsupportedOperationException(String.valueOf(_Size));
	}

	private void loadDereferensing(MethodVisitor mv, DereferenceExprTree dest) {
		mv.visitFieldInsn(GETSTATIC, "$program", "$" + dest.getSegment().text, "J");
		loadExpr(mv, dest.getAddress(), 4L);
		val offset = dest.getOffset();
		if (offset != null) {
			loadExpr(mv, offset, 4L);
			mv.visitInsn(IADD);
		}

		String name = dest.getSegment().text;
		if (!segments.containsKey(name)) {
			segments.put(name, defaultSegSize);
		}
	}

	/*private void writeLabels(ClassWriter cw, CompilationUnitTree cu) {
		for (StatementTree statement : cu.getStatements()) {
			LabelTree lb = statement.getLabel();
			if (lb != null) writeLabel(cw, lb);
		}
	}

	private void writeLabel(ClassWriter cw, LabelTree lb) {
		FieldVisitor fv = cw.visitField(ACC_STATIC | ACC_FINAL, "_lb_" + lb.getName().text, "I", null, lb.getI());
		fv.visitEnd();
	}*/

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

		mv.visitInsn(RETURN);
		mv.visitEnd();
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
