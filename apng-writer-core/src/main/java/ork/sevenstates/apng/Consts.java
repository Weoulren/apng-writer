package ork.sevenstates.apng;


public class Consts {
	public static final byte ZERO = 0;
	
	public static final int IHDR_SIG = 0x49484452;
	public static final int acTL_SIG = 0x6163544c;
	public static final int IDAT_SIG = 0x49444154;
	public static final int fdAT_SIG = 0x66644154;
	public static final int fcTL_SIG = 0x6663544c;
	public static final int IEND_TYPE = 0x49454e44;
	
	public static final int CHUNK_DELTA = 
			  4 //chunk len
			+ 4 //header
			+ 4;//CRC 
	
	public static final int IHDR_DATA_LEN = 13;
	public static final int IHDR_TOTAL_LEN = IHDR_DATA_LEN + CHUNK_DELTA;
	
	public static final int fcTL_DATA_LEN = 26;
	public static final int fcTL_TOTAL_LEN = fcTL_DATA_LEN + CHUNK_DELTA;
	
	private static final byte[] PNG_SIG = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}; //http://www.w3.org/TR/PNG/#5PNG-file-signature
	private static final byte[] IEND_ARR = new byte[] {
		0,	  0,   0,   0,
		'I', 'E', 'N', 'D',
		(byte) 0xae, 0x42,
		0x60, (byte) 0x82 //ae4260820
	};
	
	private static final byte[] acTL_ARR = new byte[] {
			 0,	  0,   0,   8,
			'a', 'c', 'T', 'L', 
			 0,   0,   0,   0, 
			 0,   0,   0,   0,
			 0,   0,   0,   0
		};
	
	public static byte[] getacTLArr() {
		return acTL_ARR.clone();
	}
	public static byte[] getIENDArr() {
		return IEND_ARR.clone();
	}
	
	public static byte[] getPNGSIGArr() {
		return PNG_SIG.clone();
	}
}
