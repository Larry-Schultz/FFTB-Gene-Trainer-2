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
	
	public synchronized int addAttribute(String attributeName) {
		Integer existingId = super.getIdNameMap().inverse().get(attributeName);
		if(existingId != null) {
			//this name is already tracked; return the existing id so a duplicate never burns a gene slot
			return existingId;
		}
		int newId = idTrackerRef.getAndIncrement();
		super.getAttributeOrderArray().add(attributeName);
		super.getIdNameMap().put(newId, attributeName);
		super.geneAttributes.put(newId, Double.valueOf(0));

		return newId;
	}
}
