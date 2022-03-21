package fft_battleground.viewer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotReceivedBet {
	@JsonProperty("player")
	private String player;
	@JsonProperty("bet")
	private int betAmount;
	@JsonProperty("balance")
	private int balanceAtTimeOfBet;
}
