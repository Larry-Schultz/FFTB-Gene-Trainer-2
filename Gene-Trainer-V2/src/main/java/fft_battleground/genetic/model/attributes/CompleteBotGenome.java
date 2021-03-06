package fft_battleground.genetic.model.attributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.BiMap;

import fft_battleground.tournament.model.Tips;
import io.jenetics.Chromosome;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.util.Factory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class CompleteBotGenome {	
	private MapGeneAttributes mapGeneAttributes;
	private ItemGeneAttributes itemGeneAttributes;
	private AbilityGeneAttributes abilityGeneAttributes;
	private UserSkillGeneAttributes userSkillGeneAttributes;
	private ClassGeneAttributes classGeneAttributes;
	private BraveFaithAttributes braveFaithAttributes;
	private PotGeneAttributes potGeneAttributes;
	private PlayerDataGeneAttributes playerDataGeneAttributes;
	private SideGeneAttributes sideGeneAttributes;
	//private BetGeneAttributes betGeneAttributes;
	private MissingGeneAttributes missingGeneAttributes;
	
	@JsonIgnore
	private transient ReadWriteLock geneClassAttributeMapLock = new ReentrantReadWriteLock();
	
	@JsonIgnore
	public transient List<GeneAttributes> geneAttributes;
	@JsonIgnore
	public transient Map<Class<? extends GeneAttributes>, GeneAttributes> geneClassGeneAttributeMap;
	@JsonIgnore
	private Map<String, Integer> attributeMapCache;
	
	public CompleteBotGenome(Tips tips) {
		AtomicInteger idTracker = new AtomicInteger(0);
		this.mapGeneAttributes = new MapGeneAttributes(idTracker, "Maps.txt");
		//this.betGeneAttributes = new BetGeneAttributes(idTracker);
		//this.itemGeneAttributes = new ItemGeneAttributes(idTracker, tips);
		//this.abilityGeneAttributes = new AbilityGeneAttributes(idTracker, tips);
		//this.userSkillGeneAttributes = new UserSkillGeneAttributes(idTracker);
		//this.classGeneAttributes = new ClassGeneAttributes(idTracker, tips);
		this.braveFaithAttributes = new BraveFaithAttributes(idTracker);
		this.potGeneAttributes = new PotGeneAttributes(idTracker);
		this.playerDataGeneAttributes = new PlayerDataGeneAttributes(idTracker);
		this.sideGeneAttributes = new SideGeneAttributes(idTracker);
		this.missingGeneAttributes = new MissingGeneAttributes(idTracker);
		
		this.geneAttributes = List.of(this.mapGeneAttributes, //this.betGeneAttributes, 
				//this.itemGeneAttributes, this.abilityGeneAttributes, this.userSkillGeneAttributes, this.classGeneAttributes, 
				this.braveFaithAttributes, this.potGeneAttributes, this.playerDataGeneAttributes, this.sideGeneAttributes, this.missingGeneAttributes);
		
		Collector<GeneAttributes, ?, Map<Class<? extends GeneAttributes>, GeneAttributes>> geneClassCollector = 
				Collectors.<GeneAttributes, Class<? extends GeneAttributes>, GeneAttributes>toMap(GeneAttributes::getClass, Function.identity());
		this.geneClassGeneAttributeMap = this.geneAttributes.stream()
				.filter(attribute -> attribute instanceof GeneAttributes)
				.collect(geneClassCollector);
	}
	
	public Factory<Genotype<DoubleGene>> generateGenome() {
		List<DoubleChromosome> chromosomes = this.generateChromosomes();
		return Genotype.of(chromosomes);
	}
	
	public List<DoubleChromosome> generateChromosomes() {
		List<DoubleChromosome> chromosomes = this.geneAttributes.stream()
				.map(attribute -> attribute.generateChromosome())
				.collect(Collectors.toList());
		return chromosomes;
	}
	
	@JsonIgnore
	public Map<String, Integer> getAttributeMap() {
		Lock readLock = this.geneClassAttributeMapLock.readLock();
		readLock.lock();
		Map<String, Integer> attributeMap;
		try {
			attributeMap = this.attributeMapCache;
		} finally {
			readLock.unlock();
		}
		return attributeMap;
	}
	
	public Map<String, Integer> generateAttributeMap() {
		Lock writeLock = this.geneClassAttributeMapLock.writeLock();
		writeLock.lock();
		Map<String, Integer> attributeMap = new HashMap<>();
		try {
			for(GeneAttributes attribute : this.geneAttributes) {
				attribute.getIdNameMap().forEach((key, value) -> attributeMap.put(value, key));
			}
			this.attributeMapCache = attributeMap;
		} finally {
			writeLock.unlock();
		}
		return attributeMap;
	}
	
	public void loadGeneData(Genotype<DoubleGene> genotype) {
		List<Chromosome<DoubleGene>> chromosomes = genotype.stream().collect(Collectors.toList());
		for(int i = 0; i < chromosomes.size(); i++) {
			Chromosome<DoubleGene> currentChromosome = chromosomes.get(i);
			GeneAttributes currentGeneAttribute = this.geneAttributes.get(i);
			
			List<DoubleGene> genes = currentChromosome.stream().collect(Collectors.toList());
			List<String> geneAttributeNames = currentGeneAttribute.getAttributeOrderArray();
			BiMap<String, Integer> geneAttributeIdInverse = currentGeneAttribute.getIdNameMap().inverse();
			for(int j = 0; j < genes.size(); j++) {
				Double geneValue = genes.get(j).allele();
				String attributeName = geneAttributeNames.get(j);
				Integer id = geneAttributeIdInverse.get(attributeName);
				currentGeneAttribute.getGeneAttributes().put(id, geneValue);
			}
		}
	}
	
	public int addMissingGene(String attributeName) {
		log.info("Found missing attribute {}, adding it to the genome", attributeName);
		int newAttributeId = this.missingGeneAttributes.addAttribute(attributeName);
		this.generateAttributeMap();
		
		return newAttributeId;
	}
	
	public int size() {
		int size = this.geneAttributes.stream()
				.mapToInt(geneAttribute -> geneAttribute.getGeneAttributes().size())
				.sum();
		return size;
	}
}
