import lombok.val;
import sun.misc.Unsafe;

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

	static long getSegment(String name) {
		return segments.computeIfAbsent(name,
				(s) -> u.allocateMemory(Short.toUnsignedLong((short) -1))
		);
	}

	static void mov(String _SegmentName, int _Addr, long _Value, int _BitSize) {
		long seg = getSegment(_SegmentName);
		if (_BitSize == Long.SIZE) {
			u.putLong(seg + _Addr, _Value);
		} else {
			long virtual = seg + _Addr;
			long oldValue = u.getLong(virtual);
			u.putLong(virtual, oldValue | (mask(_BitSize) & _Value));
		}
	}

	static long mask(int _Bits1) {
		return 0xFFFFFFFF_FFFFFFFFL << (64 - _Bits1);
	}

	static long mask(int _Bits1, int _Offset) {
		return (0xFFFFFFFF_FFFFFFFFL << (64 - _Bits1)) >>> _Offset;
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
