package fft_battleground.viewer.util;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import fft_battleground.util.GenericPairing;
import fft_battleground.viewer.model.BotReceivedBets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BotReceivedBetsJsonDeserializer extends StdDeserializer<BotReceivedBets> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3962359949792620878L;
	
	public BotReceivedBetsJsonDeserializer() {
		super(BotReceivedBets.class);
	}
	
	protected BotReceivedBetsJsonDeserializer(Class<BotReceivedBets> vc) {
		super(vc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public BotReceivedBets deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String json = p.getText();
		ObjectMapper mapper = new ObjectMapper();
		json = "{ \"data\": " + json + "}";
		MapStringIntegerJsonDataObject obj = mapper.readValue(json, MapStringIntegerJsonDataObject.class);
		BotReceivedBets result = new BotReceivedBets(GenericPairing.convertGenericPairListToMap(obj.getData()));
		return result;
	}

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class MapStringIntegerJsonDataObject {
	private List<GenericPairing<String, Integer>> data;
}