package fft_battleground.genetic.model.attributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import fft_battleground.util.GenericPairing;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public abstract sealed class GeneAttributes
implements Cloneable 
permits AbilityGeneAttributes, BraveFaithAttributes, ClassGeneAttributes, ItemGeneAttributes, 
		MapGeneAttributes, PlayerDataGeneAttributes, PotGeneAttributes, SideGeneAttributes, 
		UserSkillGeneAttributes, BetGeneAttributes, MissingGeneAttributes  
{
	protected static final double DEFAULT_MIN_VALUE = 0;
	protected static final double DEFAULT_MAX_VALUE = 100;
	
	@JsonIgnore
	protected BiMap<Integer, String> idNameMap = HashBiMap.create();
	@JsonIgnore
	public double minValue = DEFAULT_MIN_VALUE;
	@JsonIgnore
	public double maxValue = DEFAULT_MAX_VALUE;
	@JsonIgnore
	protected List<String> attributeOrderArray = new ArrayList<>();
	
	protected Map<Integer, Double> geneAttributes = new HashMap<>();
	
	@JsonIgnore
	protected Map<Integer, Boolean> geneEnabled = new HashMap<>();
	
	public DoubleChromosome generateChromosome() {
		List<DoubleGene> genes = this.geneAttributes.entrySet().stream()
				.sorted(Comparator.comparing(Entry::getKey))
				.map(entrySet -> DoubleGene.of(this.geneAttributes.get(entrySet.getKey()), this.getMinValue(), this.getMaxValue()))
				.collect(Collectors.toList());
		DoubleChromosome chromosome = DoubleChromosome.of(genes);
		return chromosome;
	}
	
	@JsonGetter("geneAttributes")
	public GeneAttributeOutput getGeneAttributeStringMap() {
		Map<String, Double> geneAttributeNameValueMap = new HashMap<>();
		this.geneAttributes.keySet().stream()
				.filter(Objects::nonNull)
				.filter(key -> this.idNameMap.get(key) != null)
				.filter(key -> this.geneAttributes.get(key) != null)
				.forEach(key -> {
					String name = this.idNameMap.get(key);
					Double value = this.geneAttributes.get(key);
					geneAttributeNameValueMap.merge(name, value, (firstValue, secondValue) -> {
						Double kept = Math.abs(secondValue) > Math.abs(firstValue) ? secondValue : firstValue;
						Double discarded = kept.equals(firstValue) ? secondValue : firstValue;
						log.warn("Duplicate gene attribute key \"{}\" found during serialization; keeping larger absolute value {} and discarding {}",
								name, kept, discarded);
						return kept;
					});
				});
		Comparator<GenericPairing<String, Double>> pairSorting = Comparator.comparing(GenericPairing<String, Double>::getRight).reversed();
		List<GenericPairing<String, Double>> genericPairing = GenericPairing.convertMapToGenericPairList(geneAttributeNameValueMap)
				.stream().sorted(pairSorting)
				.collect(Collectors.toList());
		GeneAttributeOutput output = new GeneAttributeOutput(genericPairing);
		return output;
	}
	
	protected void populateMapsFromAttributeList(AtomicInteger attributeNumber, List<String> attributeNames) {
		for(String attribute: attributeNames) {
			if(this.idNameMap.inverse().containsKey(attribute)) {
				log.warn("Duplicate attribute name \"{}\" found while building {}; skipping it so no gene slot is wasted",
						attribute, this.getClass().getSimpleName());
				continue;
			}
			int attributeId = attributeNumber.getAndIncrement();
			this.idNameMap.put(attributeId, attribute);
			this.geneAttributes.put(attributeId, Double.valueOf(0));
			this.geneEnabled.put(attributeId, false);
		}
		this.attributeOrderArray = this.idNameMap.entrySet().stream()
				.sorted(Comparator.comparing(Entry::getKey))
				.map(Entry::getValue).collect(Collectors.toList());
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GeneAttributeOutput {
	private List<GenericPairing<String, Double>> attributes;
}
