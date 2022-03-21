package fft_battleground.genetic.model.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fft_battleground.tournament.model.Tips;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class AbilityGeneAttributes extends GeneAttributes {

	public AbilityGeneAttributes(AtomicInteger idTracker, Tips tips) {
		List<String> names = new ArrayList<>();
		for (String elementName : tips.getAbility().keySet()) {
			if (!tips.getUserSkill().containsKey(elementName)) {
				names.add(elementName);
			}
		}
		this.populateMapsFromAttributeList(idTracker, names);
	}
}
