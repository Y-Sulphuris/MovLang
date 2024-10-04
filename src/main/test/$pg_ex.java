import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * @author Sulphuris
 * @since 02.10.2024 22:32
 */
public class $pg_ex {

	private static final MethodHandle[] instructions = $runtime.getInstructions($pg_ex.class, MethodHandles.lookup(), 5);

	static final int _lb_hello = 0;

	static final long $E = $runtime.segment(0xFFFFL);
	static final long $0 = $runtime.segment(0xFFFFL);
	static final long $C = $runtime.consoleSegment(0xFFFFL);
	static final long $S = $runtime.segment(0xFFFFL);

	public static void main(String[] args) throws Throwable {
		$runtime.run(instructions, $E);
	}

	static void $0() {
		$runtime.put($C, $runtime.getAddr($S, 0), (short)104);
	}

	static void $1() {
		$runtime.put($C, $runtime.getAddr($S, 0) + 0x1, (byte) 'e');
		$runtime.repaintConsole($C, 0xFFFFL);
	}

	static void $2() {
		$runtime.put($C, $runtime.getAddr($S, 0) + 0x2, (byte) 'l');
		$runtime.repaintConsole($C, 0xFFFFL);
	}

	static void $3() {
		$runtime.put($C, $runtime.getAddr($S, 0) + 0x3, (byte) 'l');
		$runtime.repaintConsole($C, 0xFFFFL);
	}

	static void $4() {
		$runtime.put($C, $runtime.getAddr($S, 0) + 0x4, (byte) 'o');
		$runtime.repaintConsole($C, 0xFFFFL);
	}

	static void $5() {
		$runtime.put($E, 0, _lb_hello);
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
