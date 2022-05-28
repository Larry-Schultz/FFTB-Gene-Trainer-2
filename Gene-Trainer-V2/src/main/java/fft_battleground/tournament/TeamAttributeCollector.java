package fft_battleground.tournament;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import fft_battleground.genetic.model.attributes.CompleteBotGenome;
import fft_battleground.tournament.model.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamAttributeCollector {
	private static final String newAttributeAddress = "/chain/attributes";
	
	private SimpMessagingTemplate template;
	
	public short[] attributes(Team team, CompleteBotGenome genome) {
		List<String> attributeNames = team.teamAbilityElements();
		short[] attributes = new short[attributeNames.size()];
		for(int i = 0; i < attributeNames.size(); i++) {
			String attributeName = attributeNames.get(i);
			Integer attributeId = genome.getAttributeMap().get(attributeName);
			if(attributeId == null) {
				attributeId = genome.addMissingGene(attributeName);
				String message = "Found missing attribute " + attributeName + ", adding it to the genome";
				template.convertAndSend(newAttributeAddress, message);
			}
			attributes[i] = attributeId.shortValue();
		}
		
		return attributes;
	}
}
