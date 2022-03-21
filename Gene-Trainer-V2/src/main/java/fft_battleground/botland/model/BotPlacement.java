package fft_battleground.botland.model;

public record BotPlacement(String botName, Long gil) implements Comparable<BotPlacement> {

	@Override
	public int compareTo(BotPlacement o) {
		return this.gil.compareTo(o.gil());
	}

}
