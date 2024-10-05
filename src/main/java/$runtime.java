import lombok.SneakyThrows;
import lombok.val;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author Sulphuris
 * @since 01.10.2024 12:38
 */
public class $runtime {
	private $runtime() throws InstantiationException {
		throw new InstantiationException();
	}

	// This was supposed to be implemented via JNIDirect, but we are temporarily forced to use Unsafe due to hardware issues
	private static Unsafe getU() {
		try {
			val f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			return (Unsafe) f.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			System.err.println("This application is not supported on your virtual machine (sun.misc.Unsafe not found)");
			System.exit(0);
			return null;
		}
	}

	public static final Unsafe u = getU();


	static void mov(long _DstSeg, int _DstAddr, long _SrcSeg, int _SrcAddr, long _Bytes) {
		u.copyMemory(_SrcSeg + _SrcAddr, _DstSeg + _DstAddr, _Bytes);
	}

	static void mov1(long _DstSeg, int _DstAddr, long _SrcSeg, int _SrcAddr) {
		u.putByte(_DstSeg + _DstAddr, u.getByte(_SrcSeg + _SrcAddr));
	}

	static void mov2(long _DstSeg, int _DstAddr, long _SrcSeg, int _SrcAddr) {
		u.putShort(_DstSeg + _DstAddr, u.getShort(_SrcSeg + _SrcAddr));
	}

	static void mov4(long _DstSeg, int _DstAddr, long _SrcSeg, int _SrcAddr) {
		u.putInt(_DstSeg + _DstAddr, u.getInt(_SrcSeg + _SrcAddr));
	}

	static void mov8(long _DstSeg, int _DstAddr, long _SrcSeg, int _SrcAddr) {
		u.putLong(_DstSeg + _DstAddr, u.getLong(_SrcSeg + _SrcAddr));
	}


	static void put(long _Segment, int _Addr, byte _Value) {
		u.putByte(_Segment + _Addr, _Value);
	}

	static void put(long _Segment, int _Addr, short _Value) {
		u.putShort(_Segment + _Addr, _Value);
	}

	static void put(long _Segment, int _Addr, int _Value) {
		u.putInt(_Segment + _Addr, _Value);
	}

	static void put(long _Segment, int _Addr, long _Value) {
		u.putLong(_Segment + _Addr, _Value);
	}

	static void put(long _Segment, int _Addr, long _Value, int _BitSize) {
		if (_BitSize == Long.SIZE) {
			u.putLong(_Segment + _Addr, _Value);
		} else {
			long virtual = _Segment + _Addr;
			long oldValue = u.getLong(virtual);
			u.putLong(virtual, oldValue | (mask(_BitSize) & _Value));
		}
	}

	static void put(long _Segment, int _Addr, byte[] _Value, long _Bytes) {
		final int aLen = _Value.length;
		if (aLen < _Bytes) {
			u.copyMemory(_Value, u.arrayBaseOffset(byte[].class), null, _Segment + _Addr, aLen);
			u.setMemory(_Segment + _Addr + aLen, _Bytes - aLen, (byte) 0);
		} else {
			u.copyMemory(_Value, u.arrayBaseOffset(byte[].class), null, _Segment + _Addr, _Bytes);
		}
	}

	static void putNulls(long _Segment, int _Addr, long _Bytes) {
		u.setMemory(_Segment + _Addr, _Bytes, (byte) 0);
	}

	static int getAddr(long _Segment, int _Addr) {
		return u.getInt(_Segment + _Addr);
	}
	static byte get(long _Segment, int _Addr) {
		return u.getByte(_Segment + _Addr);
	}

	static long mask(int _Bits1) {
		return 0xFFFFFFFF_FFFFFFFFL << (64 - _Bits1);
	}

	static long mask(int _Bits1, int _Offset) {
		return (0xFFFFFFFF_FFFFFFFFL << (64 - _Bits1)) >>> _Offset;
	}

	static MethodHandle[] getInstructions(Class<?> program, MethodHandles.Lookup lookup, int size) {
		MethodHandle[] instructions = new MethodHandle[size];
		MethodType type = MethodType.methodType(void.class);
		for (int i = 0, _Len = instructions.length; i < _Len; i++) {
			try {
				instructions[i] = lookup.findStatic(program, "$" + Integer.toHexString(i), type);
			} catch (NoSuchMethodException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return instructions;
	}

	static long segment(long size) {
		long a = u.allocateMemory(size);
		u.setMemory(a, size, (byte) 0);
		return a;
	}

	static long consoleSegment(long size) {
		long a = u.allocateMemory(size);
		u.setMemory(a, size, (byte) ' ');
		return a;
	}

	static void run(MethodHandle[] instructions, long $E) throws Throwable {
		$runtime.u.putInt($E, 0);
		$runtime.u.putInt($E + 4, 0); // exit code
		for (; $runtime.u.getInt($E) < instructions.length; $runtime.u.putInt($E, $runtime.u.getInt($E) + 1)) {
			int i = $runtime.u.getInt($E);
			if (i > instructions.length || i < 0) System.exit(0x6C);
			instructions[i].invokeExact();
		}
		System.exit($runtime.u.getInt($E + 4));
	}

	private static final MethodHandle clr;

	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		val mt = MethodType.methodType(void.class);
		String methodName;
		if (System.getProperty("os.name").contains("Win")) {
			methodName = "clrWin";
		} else {
			methodName = "clrUnix";
		}
		try {
			clr = lookup.findStatic($runtime.class, methodName, mt);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace(System.err);
			System.exit(-1);
			throw new RuntimeException(e);
		}
	}

	@SneakyThrows
	private static void clrWin() {
		try {
			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		} catch (Exception ignored) {
		}
	}

	private static void clrUnix() {
		try {
			Runtime.getRuntime().exec("clear");
		} catch (Exception ignored) {
		}
	}

	@SneakyThrows
	static void clearConsole() {
		clr.invokeExact();
	}

	static void flush(long _ConsoleSeg, long _Size) {
		StringBuilder b = new StringBuilder();
		int lastSymbol = 0;
		for (int i = 0; i < _Size; i++) {
			char ch = (char) u.getByte(_ConsoleSeg + i);
			if (!Character.isWhitespace(ch)) lastSymbol = i;
			b.append(ch);
		}
		System.out.print(b.substring(0, lastSymbol + 1));
	}

	static void repaintConsole(long _ConsoleSeg, long _Size) {
		clearConsole();
		flush(_ConsoleSeg, _Size);
	}
/*
	static void shiftRight(long _StartAddress, long _Size, int n) {
		for (int i = 0; i < _Size; i++) {

		}
		long b1 = u.getLong(_StartAddress);
		long b2 = u.getLong(_StartAddress + Long.BYTES);
		u.putLong(_StartAddress, b1 << n);
		u.putLong(_StartAddress + Long.BYTES, (b2 << n) | (b1 << (Long.SIZE - n)));
	}
*/
}
