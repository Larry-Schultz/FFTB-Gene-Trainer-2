package fft_battleground.exception;

public class BotConfigException extends BattleGroundException {

	/**
	 *
	 */
	private static final long serialVersionUID = -5570712457901964672L;

	public BotConfigException(String msg) {
		super(msg);
	}

	public BotConfigException(String msg, Throwable e) {
		super(msg, e);
	}
}
