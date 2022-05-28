package fft_battleground.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatistics<T> {
    private String cpuUsage;
    private String ramUsage;
    private Long generation;
    private T data;
    private Boolean applicationComplete;
}
