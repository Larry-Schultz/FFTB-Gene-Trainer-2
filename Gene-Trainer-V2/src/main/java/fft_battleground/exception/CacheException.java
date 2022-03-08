package fft_battleground.exception;

public class CacheException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9055837649140254133L;
	
	public CacheException(Exception e) {
		super(e);
	}

	public CacheException(String msg, Throwable e) {
		super(msg, e);
	}

	public CacheException(String string) {
		super(string);
	}

	public CacheException() {
		super();
	}

}
