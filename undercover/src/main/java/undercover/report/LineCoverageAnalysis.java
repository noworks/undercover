package undercover.report;

import java.util.HashMap;
import java.util.Map;

import undercover.metric.BlockCoverage;
import undercover.metric.BlockMeta;

public class LineCoverageAnalysis {
	private final Map<Integer, LineCoverage> lines = new HashMap<Integer, LineCoverage>();
	
	public void analyze(BlockMeta blockMeta, BlockCoverage blockCoverage) {
		for (Integer each : blockMeta.lines()) {
			LineCoverage lineCoverage = lines.get(each);
			if (lineCoverage == null) {
				lineCoverage = new LineCoverage();
				lines.put(each, lineCoverage);
			}
			lineCoverage.addBlock(blockCoverage);
		}
	}

	public int getLineCount() {
		return lines.size();
	}

	public int getCoveredLineCount() {
		int result = 0;
		for (LineCoverage each : lines.values()) {
			if (each.isCompletelyCovered()) {
				result = result + 1;
			}
		}
		return result;
	}
	
	public LineCoverage getLine(int lineNumber) {
		return lines.get(lineNumber);
	}
}
