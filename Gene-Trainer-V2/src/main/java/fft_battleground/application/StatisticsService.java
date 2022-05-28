package fft_battleground.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import org.springframework.stereotype.Service;

import fft_battleground.application.model.ApplicationStatistics;
import fft_battleground.application.model.HighScore;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

@Service
@Slf4j
public class StatisticsService {

    private SystemInfo si;

    public StatisticsService() {
        this.si = new SystemInfo();
    }

    public <T> ApplicationStatistics<T> getApplicationStatistics(long generation, T data) {
        HardwareAbstractionLayer hardware = this.si.getHardware();
        Double cpuLoad = this.getProcessorLoad(hardware.getProcessor());
        Double memoryUsage = this.getMemoryUsage(hardware.getMemory());

        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        String cpuLoadString = null;
        String memoryUsageString = null;
        String totalMarkovChainsString = null;

        if (cpuLoad != null && cpuLoad > 0) {
            cpuLoadString = decimalFormat.format(cpuLoad) + "%";
        }
        if (memoryUsage != null && memoryUsage > 0) {
            memoryUsageString = decimalFormat.format(memoryUsage) + "%";
        }

        DecimalFormat decimalAndCommaFormat = new DecimalFormat("#,###");
        ApplicationStatistics applicationStatistics = new ApplicationStatistics(cpuLoadString, memoryUsageString, 
        		generation, data, false);

        return applicationStatistics;
    }

    public Double getProcessorLoad(CentralProcessor processor) {
        double processorLoad = processor.getSystemCpuLoadBetweenTicks() * 100;
        if (processorLoad > 0) {
            return processorLoad;
        } else {
            return null;
        }
    }

    public Double getMemoryUsage(GlobalMemory memory) {
        long availableMemory = memory.getAvailable();
        long totalMemory = memory.getTotal();

        if (availableMemory > 0 && totalMemory > 0) {
            BigDecimal availableMemoryDouble = new BigDecimal(availableMemory);
            BigDecimal totalMemoryDouble = new BigDecimal(totalMemory);
            BigDecimal percentAvailable = availableMemoryDouble.divide(totalMemoryDouble, 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            BigDecimal percentUsed = (new BigDecimal(100)).subtract(percentAvailable);
            return percentUsed.doubleValue();
        } else {
            return null;
        }
    }

}