package fft_battleground.viewer.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TournamentListRequest {
	public List<Long> tournamentIds;
}
