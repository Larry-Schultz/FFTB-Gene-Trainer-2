package fft_battleground.genetic.model.attributes;

import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper=true)
@Slf4j
public final class MissingGeneAttributes extends GeneAttributes {
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private AtomicInteger idTrackerRef;
	
	public MissingGeneAttributes(AtomicInteger idTracker) {
		this.idTrackerRef = idTracker;
		this.addAttribute("-Item");
	}
	
	public int addAttribute(String attributeName) {
		int newId = idTrackerRef.getAndIncrement();
		super.getAttributeOrderArray().add(attributeName);
		try {
			super.getIdNameMap().put(newId, attributeName);
		} catch(IllegalArgumentException e) {
			log.error("The attribute {} is already in MissingGeneAttributes.", attributeName, e);
		}
		super.geneAttributes.put(newId, Double.valueOf(0));
		
		return newId;
	}
}
