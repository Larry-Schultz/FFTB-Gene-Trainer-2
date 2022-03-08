package fft_battleground.viewer.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotReceivedBets {
	private Map<String, Integer> bets;
}