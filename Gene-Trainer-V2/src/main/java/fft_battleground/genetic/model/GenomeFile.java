package fft_battleground.genetic.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import fft_battleground.botland.model.BotPlacement;
import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenomeFile {
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	private Date creationDate;
	
	private List<BotPlacement> botLeaderboard;
	private int matchesAnalyzed;
	long gilResult;
	long perfectGil;
	int geneCount;
	long generation;
	private CompleteBotGenome genome;
	
	public GenomeFile(List<BotPlacement> botLeaderboard, int matchesAnalyzed, long gilResult, long perfectGil, 
			int geneCount, long generation, CompleteBotGenome genome) {
		this.creationDate = new Date();
		this.botLeaderboard = botLeaderboard;
		this.matchesAnalyzed = matchesAnalyzed;
		this.gilResult = gilResult;
		this.perfectGil = perfectGil;
		this.geneCount = geneCount;
		this.genome = genome;
	}
}
