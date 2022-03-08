package fft_battleground.genetic.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fft_battleground.tournament.model.Tips;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class ClassGeneAttributes extends GeneAttributes {
	
	public ClassGeneAttributes(AtomicInteger idTracker, Tips tips) {
		List<String> names = new ArrayList<>();
		for(String elementName: tips.getClassMap().keySet()) {
			names.add(elementName);
		}
		this.populateMapsFromAttributeList(idTracker, names);
	}


}
