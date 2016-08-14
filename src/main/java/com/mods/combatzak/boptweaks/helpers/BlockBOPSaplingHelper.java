package com.mods.combatzak.boptweaks.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.common.world.features.trees.WorldGenBOPTaiga2;
import biomesoplenty.common.world.features.trees.WorldGenBOPTaiga3;
import net.minecraft.block.Block;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

/**
 * Contains helper operations to assist the growing of a "big" fir tree if appropriate
 * 
 * @author CombatZAK
 *
 */
public class BlockBOPSaplingHelper {
	
	/**
	 * Creates a tree generator to grow the proper fir tree sapling. Assumes that the argument coordinates point to a valid fir sapling
	 * 
	 * @param world World/Dimension where growth is happening
	 * @param x x-coord for sapling
	 * @param y y-coord for sapling
	 * @param z z-coord for sapling
	 * @param random seeded RNG used for random calculations
	 * @return Generator used to grow appropriately sized tree
	 */
	public static WorldGenAbstractTree growFirSapling(World world, int x, int y, int z, Random random) {
		if (isBigFirPattern(world, x, y, z)) //check to see if the pattern on the ground forms a + shape around the coordinate
			return new WorldGenBOPTaiga3(BOPCBlocks.logs1, BOPCBlocks.leaves2, 3, 1, false, 35, 10, 0, 4); //make a big tree if so
		else if (isBigFirNeighbor(world, x, y, z)) //check to see if the sapling in question is a neighbor to a + pattern sapling
			return null; //these saplings should remain dormant till their "parent" grows
		//after the above, it is assumed that any fir sapling is a "normal" 1x1 trunk
		else if (random.nextInt(10) == 0) //1 in 10 of these saplings should grow into a "tall" fir tree
			return new WorldGenBOPTaiga2(BOPCBlocks.logs1, BOPCBlocks.leaves2, 3, 1, false, 20, 15, 4, 4);
		else //this is the default growth
			return new WorldGenBOPTaiga2(BOPCBlocks.logs1, BOPCBlocks.leaves2, 3, 1, false, 10, 10, 5, 4);
	}
	
	/**
	 * Indicates whether the block/meta combination is a Biomes O Plenty Fir Sapling
	 * 
	 * @param b block to check
	 * @param meta block metadata
	 * @return true if the block is a fir sapling; false otherwise
	 */
	private static boolean isFirSapling(Block b, int meta) {
		return BOPCBlocks.saplings.equals(b) && (meta & 15) == 6;
	}
	
	/**
	 * Indicates whether the specified coordinates occupy the center of '+' of fir saplings
	 * 
	 * @param world dimension to check
	 * @param x xcoord of block
	 * @param y ycoord of block
	 * @param z zcoord of block
	 * @return true if the pattern matches a "big" fir tree; false otherwise
	 */
	private static boolean isBigFirPattern(World world, int x, int y, int z) {
		if (world == null) return false; //if there is no dimension, bail out
		if (!isFirSapling(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z))) return false; //if the block being checked isn't a fir sapling, bail out
		List<Tuple> neighborBlocks = getNeighbors(world, x, y, z); //get all of the neighboring blocks in the x-z plane
		for (Tuple t : neighborBlocks) {
			if (t == null) return false; //null test each neighbor
			Block nBlock = (Block)t.getFirst();
			int nMeta = (Integer)t.getSecond();
			
			if (!isFirSapling(nBlock, nMeta)) return false; //if the neighbor isn't a fir sapling, then this doesn't fit the pattern
		}
		
		return true; //all negative-tests passed, so this is a big fir pattern
	}
	
	/**
	 * Indicates whether the specified coordinates occupy part of a '+' pattern of fir saplings, but aren't necessarily the center
	 * @param world dimension to check
	 * @param x xcoord of block
	 * @param y ycoord of block
	 * @param z zcoord of block
	 * @return true if the block at the coord is part of a big fir pattern
	 */
	private static boolean isBigFirNeighbor(World world, int x, int y, int z) {
		if (world == null) return false; //bail out if there is no dimension
		
		//run the check on each of the 
		if (isBigFirPattern(world, x - 1, y, z)) return true;
		if (isBigFirPattern(world, x + 1, y, z)) return true;
		if (isBigFirPattern(world, x, y, z - 1)) return true;
		if (isBigFirPattern(world, x, y, z + 1)) return true;
		
		return false; //none of the neighbors match a big fir pattern
	}
	
	/**
	 * Gets all the neighboring blocks/metadata information for a particular block on the x-z plane
	 * @param world dimension to check
	 * @param x xcoord
	 * @param y ycoord
	 * @param z zcoord
	 * @return set of block/metadata tuples for each coordinate cardinally adjacent to the input coordinates
	 */
	private static List<Tuple> getNeighbors(World world, int x, int y, int z) {
		List<Tuple> result = new ArrayList<Tuple>(); //return object
		if (world == null) return result; //return empty list if no dimension
		
		//populate the list
		result.add(new Tuple(world.getBlock(x - 1, y, z), world.getBlockMetadata(x - 1, y, z)));
		result.add(new Tuple(world.getBlock(x + 1, y, z), world.getBlockMetadata(x + 1, y, z)));
		result.add(new Tuple(world.getBlock(x, y, z - 1), world.getBlockMetadata(x, y, z - 1)));
		result.add(new Tuple(world.getBlock(x, y, z + 1), world.getBlockMetadata(x, y, z + 1)));
		
		return result;
	}
}
