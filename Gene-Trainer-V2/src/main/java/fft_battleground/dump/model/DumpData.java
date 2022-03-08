package fft_battleground.dump.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DumpData {
	private Long tournamentId;
	private Integer champStreak;
	private TournamentTeamValues tournamentTeamValues;
}
