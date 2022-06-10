package fft_battleground.genetic.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Percentile {
	private int key;
	private double value;
	
	public Percentile(Entry<Integer, Double> entry) {
		this.key = entry.getKey();
		this.value = entry.getValue();
	}
	
	public static List<Percentile> createPercentileListFromMap(Map<Integer, Double> percentileMap) {
		List<Percentile> percentileList = percentileMap.entrySet().stream().map(Percentile::new)
				.collect(Collectors.toList());
		return percentileList;
	}
}
