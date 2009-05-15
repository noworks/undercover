package undercover.metric;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import undercover.support.ObjectSupport;

public class MethodMetric extends ObjectSupport implements Serializable {
	private static final long serialVersionUID = 4471359617355848867L;

	private String name;
	private String descriptor;
	private List<BlockMetric> blocks;
	private int conditionalBranches;

	public MethodMetric(String name, String descriptor) {
		this.name = name;
		this.descriptor = descriptor;
		blocks = new ArrayList<BlockMetric>();
		conditionalBranches = 0;
	}
	
	public String name() {
		return name;
	}

	public String descriptor() {
		return descriptor;
	}
	
	public void addBlock(BlockMetric blocksMetric) {
		blocks.add(blocksMetric);
	}
	
	public List<BlockMetric> blocks() {
		return blocks;
	}
	
	public void addConditionalBranch() {
		conditionalBranches++;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(name).append('{').append(descriptor).append(',');
		builder.append('[');
		for (BlockMetric each : blocks) {
			builder.append(each.toString()).append(',');
		}
		builder.append(']');
		builder.append('}');
		return builder.toString();
	}

	public int getConditionalBranches() {
		return conditionalBranches;
	}
}
