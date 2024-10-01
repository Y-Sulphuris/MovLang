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

	private static final HashMap<Character, Long> segments = new HashMap<>();

	static long getSegment(char name) {
		return segments.computeIfAbsent(name,
				(s) -> u.allocateMemory(Short.toUnsignedLong((short) -1))
		);
	}

	static void mov(char _Segment, int _Addr, long _Value, int _BitSize) {
		long seg = getSegment(_Segment);
		if (_BitSize == 64) {
			u.putLong(seg + _Addr, _Value);
		} else {
			long virtual = seg + _Addr;
			long oldValue = u.getLong(virtual);
			u.putLong(virtual, oldValue | (mask(_BitSize) & _Value));
		}
	}

	static long mask(int bits1) {

	}
}
