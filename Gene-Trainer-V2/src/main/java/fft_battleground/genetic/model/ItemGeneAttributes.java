package fft_battleground.genetic.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import fft_battleground.tournament.model.Tips;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public final class ItemGeneAttributes extends GeneAttributes {
	public static final List<String> itemsToPrefix = Arrays.asList(new String[] {"Bracer", "Elixir", "Kiyomori", "Soft", "X-Potion", "Remedy", "Muramasa",
			"Maiden's Kiss", "Masamune", "Murasame", "Eye Drop", "Shuriken", "Antidote", "Hi-Potion", "Heaven's Cloud", "Bizen Boat",
			"Hi-Ether", "Chirijiraden", "Kikuichimoji", "Potion", "Holy Water", "Spear", "Echo Grass", "Phoenix Down", "Ether"});
	public static final String itemSuffix = "-Item";
	
	public ItemGeneAttributes(AtomicInteger idTracker, Tips tips) {
		List<String> itemNames = this.itemNames(tips);
		this.populateMapsFromAttributeList(idTracker, itemNames);
	}
	
	public List<String> itemNames(Tips tips) {
		List<String> names = new ArrayList<>();
		for(String elementName: tips.getItem().keySet()) {
			List<String> duplicates = itemsToPrefix;
			if(!duplicates.contains(elementName)) {
				names.add(elementName);
			} else {
				names.add(elementName + itemSuffix);
			}
		}
		
		return names;
	}
}
