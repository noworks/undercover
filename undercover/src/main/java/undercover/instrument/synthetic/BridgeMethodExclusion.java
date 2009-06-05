package undercover.instrument.synthetic;

import static org.objectweb.asm.Opcodes.*;
import static undercover.instrument.synthetic.ExclusionUtils.*;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class BridgeMethodExclusion implements Exclusion {

	public boolean exclude(ClassNode classNode) {
		return false;
	}

	public boolean exclude(ClassNode classNode, MethodNode methodNode) {
		return hasAccess(methodNode.access, ACC_BRIDGE | ACC_SYNTHETIC);
	}
}
