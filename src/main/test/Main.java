/**
 * @author Sulphuris
 * @since 05.10.2024 12:12
 */
public class Main {
	public static void main(String[] args) {
		for (byte i = 0; i != -1; i++) {
			System.out.println("0[inc_table + "+Integer.toHexString(Byte.toUnsignedInt(i))+"] "+Integer.toHexString(Byte.toUnsignedInt((byte) (i+1))));
		}
	}
}
