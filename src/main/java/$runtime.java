import lombok.SneakyThrows;
import lombok.val;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;

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

	private static final HashMap<String, Long> segments = new HashMap<>();

	static void mov1(long _Segment, int _Addr, byte _Value) {
		u.putByte(_Segment + _Addr, _Value);
	}

	static void mov2(long _Segment, int _Addr, short _Value) {
		u.putShort(_Segment + _Addr, _Value);
	}

	static void mov4(long _Segment, int _Addr, int _Value) {
		u.putInt(_Segment + _Addr, _Value);
	}

	static void mov8(long _Segment, int _Addr, long _Value) {
		u.putLong(_Segment + _Addr, _Value);
	}

	static void movX(long _Segment, int _Addr, long _Value, int _BitSize) {
		if (_BitSize == Long.SIZE) {
			u.putLong(_Segment + _Addr, _Value);
		} else {
			long virtual = _Segment + _Addr;
			long oldValue = u.getLong(virtual);
			u.putLong(virtual, oldValue | (mask(_BitSize) & _Value));
		}
	}

	static void movX(long _Segment, int _Addr, byte[] _Value) {
		u.copyMemory(_Value, u.arrayBaseOffset(byte[].class), null, _Segment + _Addr, _Value.length);
	}

	static int getAddr(long _Segment, int _Addr) {
		return u.getInt(_Segment + _Addr);
	}

	static long mask(int _Bits1) {
		return 0xFFFFFFFF_FFFFFFFFL << (64 - _Bits1);
	}

	static long mask(int _Bits1, int _Offset) {
		return (0xFFFFFFFF_FFFFFFFFL << (64 - _Bits1)) >>> _Offset;
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

	private static void clrWin() {
		try {
			Runtime.getRuntime().exec("cls");
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
		System.out.print(b.substring(0, lastSymbol+1));
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
