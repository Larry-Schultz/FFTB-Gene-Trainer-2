package fft_battleground.genetic.model.attributes;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class PotGeneAttributes extends GeneAttributes {
	public static final String BET_COUNT = "betCount";
	public static final String ODDS = "odds";
	public static final String POT_AMOUNT = "potAmount";
	public static final List<String> potGeneAttributes = List.of(BET_COUNT, POT_AMOUNT, ODDS);
	
	public PotGeneAttributes(AtomicInteger idTracker) {
		this.populateMapsFromAttributeList(idTracker, potGeneAttributes);
	}
}
