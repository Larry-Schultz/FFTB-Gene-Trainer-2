package fft_battleground.exception;

public class ViewerException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5795656752743064785L;

	public ViewerException(Exception e) {
		super(e);
	}

	public ViewerException(String msg, Throwable e) {
		super(msg, e);
	}

	public ViewerException(String string) {
		super(string);
	}

	public ViewerException() {
		super();
	}
}
