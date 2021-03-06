package fft_battleground.dump.model;

import lombok.Data;

@Data
public class BattlegroundRetryState {
	private Integer retryCount;
	
	public BattlegroundRetryState() {
		this.retryCount = 0;
	}
	
	public void incrementCount() {
		this.retryCount = this.retryCount + 1;
	}
}