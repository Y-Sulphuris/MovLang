import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author Sulphuris
 * @since 02.10.2024 22:32
 */
public class $pg_ex {

	private static final MethodHandle[] instructions = new MethodHandle[6];
	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	static {
		MethodType type = MethodType.methodType(void.class);
		for (int i = 0, _Len = instructions.length; i < _Len; i++) {
			try {
				instructions[i] = lookup.findStatic($pg_ex.class, "$" + i, type);
			} catch (NoSuchMethodException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	static final int _lb_hello = 0;

	static final long $E = $runtime.segment(0xFFFFL);
	static final long $0 = $runtime.segment(0xFFFFL);
	static final long $C = $runtime.consoleSegment(0xFFFFL);
	static final long $S = $runtime.segment(0xFFFFL);

	public static void main(String[] args) throws Throwable {
		$runtime.u.putInt($E, 0);
		for (; $runtime.u.getInt($E) < instructions.length; $runtime.u.putInt($E, $runtime.u.getInt($E)+1)) {
			instructions[$runtime.u.getInt($E)].invokeExact();
		}
		$runtime.repaintConsole($C, 0xFFFFL);
	}

	static void $0() {
		//noinspection PointlessArithmeticExpression
		$runtime.mov1($C, $runtime.getAddr($S, 0) + 0x0, (byte) 'h');
	}

	static void $1() {
		$runtime.mov1($C, $runtime.getAddr($S, 0) + 0x1, (byte) 'e');
	}

	static void $2() {
		$runtime.mov1($C, $runtime.getAddr($S, 0) + 0x2, (byte) 'l');
	}

	static void $3() {
		$runtime.mov1($C, $runtime.getAddr($S, 0) + 0x3, (byte) 'l');
	}

	static void $4() {
		$runtime.mov1($C, $runtime.getAddr($S, 0) + 0x4, (byte) 'o');
	}

	static void $5() {
		$runtime.mov4($E, 0, _lb_hello);
	}


	public static void printMemory(PrintStream out, long mem, long size) {
		printMemory(out, mem, size, 4, 4);
	}

	public static void printMemory(PrintStream out, long mem, long size, int colSize, int colCount) {
		for (int i = 0; i < size; i++) {
			if (i % (colSize * colCount) == 0)
				out.print((i == 0 ? "" : "\n") + "[" + String.format("%08x", (mem + i)) + "] (+" + String.format("%04x", i) + ")\t");
			out.printf("%02x ", $runtime.u.getByte(mem + i));
			if (i % colSize == colSize - 1) out.print("  ");
		}
		out.println();
	}
}
