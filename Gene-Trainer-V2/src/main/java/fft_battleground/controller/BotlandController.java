package fft_battleground.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fft_battleground.botland.BotlandSimulationService;
import fft_battleground.botland.model.BotlandProgress;
import fft_battleground.botland.model.BotlandRunResult;

@RestController
public class BotlandController {

	@Autowired
	private BotlandSimulationService botlandSimulationService;

	//non-blocking, unlike /start: kicks off a simulation over the last N completed matches
	@GetMapping("/botland/start")
	public ResponseEntity<Map<String, Object>> start(@RequestParam("matches") int matches) {
		if(matches < 1) {
			return ResponseEntity.badRequest().body(this.responseMap(false, "matches must be a positive number"));
		}
		boolean accepted = this.botlandSimulationService.start(matches);
		if(accepted) {
			Map<String, Object> body = this.responseMap(true, null);
			body.put("matches", matches);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(this.responseMap(false, "a botland simulation is already running"));
	}

	@GetMapping("/botland/status")
	public Map<String, Object> status() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("running", this.botlandSimulationService.isRunning());
		BotlandProgress progress = this.botlandSimulationService.getStatus();
		body.put("phase", progress == null ? "idle" : progress.phase());
		body.put("percent", progress == null ? 0 : progress.percent());
		body.put("detail", progress == null ? "" : progress.detail());
		return body;
	}

	@GetMapping("/botland/last")
	public ResponseEntity<BotlandRunResult> last() {
		BotlandRunResult result = this.botlandSimulationService.getLastResult();
		if(result == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(result);
	}

	private Map<String, Object> responseMap(boolean accepted, String error) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("accepted", accepted);
		if(error != null) {
			body.put("error", error);
		}
		return body;
	}
}
