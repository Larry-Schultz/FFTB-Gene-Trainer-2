package fft_battleground.genetic.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class PotGeneAttributes extends GeneAttributes {
	public static final String BET_COUNT = "betCount";
	public static final String CHAMP_STREAK = "champStreak";
	public static final List<String> potGeneAttributes = List.of(BET_COUNT, "potAmount", "odds", CHAMP_STREAK);
	
	public PotGeneAttributes(AtomicInteger idTracker) {
		this.populateMapsFromAttributeList(idTracker, potGeneAttributes);
	}
}
