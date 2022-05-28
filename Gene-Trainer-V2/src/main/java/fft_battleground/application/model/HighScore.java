package fft_battleground.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HighScore {
	private Long highScore;
	private Long perfectScore;
	private Integer botLeaderboardPlacement;
	private Integer matchesAnalyzed;
	private Integer numberOfUnitGenes;
	private Integer numberOfTotalGenes;
	private String updateDate;
}
