package com.mods.combatzak.boptweaks.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * Class Transformer that changes the BlockBOPSapling class to call an external method for certain sapling growth
 *  
 * @author CombatZAK
 *
 */
public class TransformBlockBOPSapling implements IClassTransformer {
	
	/**
	 * Transforms the BlockBOPSapling class by modifying the growTree method
	 * 
	 * @param target unmodified BlockBOPSapling class
	 * @return modified class with edited growTree method
	 */
	private byte[] patchGrowTree(byte[] target) {
		ClassNode clazz = new ClassNode(); //represents the class entry point
		ClassReader reader = new ClassReader(target); //reads from the bytestream
		reader.accept(clazz, 0);; //read and deserialize the class
		
		for (MethodNode method : clazz.methods) {
			if (!method.name.equals("func_149878_d")) continue; //looking for func_149878_d; skip any others
			
			AbstractInsnNode cursor = method.instructions.getFirst(); //start at the beginning of the method
			while (cursor.getType() != AbstractInsnNode.TABLESWITCH_INSN) cursor = cursor.getNext(); //jump down to the switch instruction
			cursor = ((TableSwitchInsnNode)cursor).labels.get(6).getNext(); //move past the the fir tree label (index = 0)
			
			//going to delete all instructions between the label and its jump(break) instruction
			while (cursor.getType() != AbstractInsnNode.JUMP_INSN) {
				cursor = cursor.getNext(); //advance the cursor
				method.instructions.remove(cursor.getPrevious());
			}
			
			//with the old instructions cleared out, we can inject our new instructions
			//first load up method arguments
			method.instructions.insertBefore(cursor, new VarInsnNode(Opcodes.ALOAD, 1)); //load the first parameter onto the stack (world)
			method.instructions.insertBefore(cursor, new VarInsnNode(Opcodes.ILOAD, 2)); //second parameter (x)
			method.instructions.insertBefore(cursor, new VarInsnNode(Opcodes.ILOAD, 3)); //third parameter (y)
			method.instructions.insertBefore(cursor, new VarInsnNode(Opcodes.ILOAD, 4)); //fourth parameter (z)
			method.instructions.insertBefore(cursor, new VarInsnNode(Opcodes.ALOAD, 5)); //fifth parameter (random)
			
			//invoke the helper method
			method.instructions.insertBefore(cursor, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mods/combatzak/boptweaks/helpers/BlockBOPSaplingHelper", "growFirSapling", "(Lnet/minecraft/world/World;IIILjava/util/Random;)Lnet/minecraft/world/gen/feature/WorldGenAbstractTree;", false));
			method.instructions.insertBefore(cursor, new VarInsnNode(Opcodes.ASTORE, 7)); //store the result to local variable obj and that's it
			
			break;
		}
		
		ClassWriter writer = new ClassWriter(0); //create a writer to generate new class bytestream
		clazz.accept(writer); //serialze the edited class
		return writer.toByteArray();
	}

	/// IClassTransformer Implementation
	
	/**
	 * Transforms a class based on its name
	 */
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals("biomesoplenty.common.blocks.BlockBOPSapling")) //if class name found...
			return patchGrowTree(basicClass); //modify the growTree method and return it
		
		return basicClass; //not the right class, return unmodified
	}
}
