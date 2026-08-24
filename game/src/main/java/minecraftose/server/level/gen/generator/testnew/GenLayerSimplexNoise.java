package minecraftose.server.level.gen.generator.testnew;

public class GenLayerSimplexNoise {
	int amplitude;
	int offset;
	SimplexNoise noisegen;
	public double yCoord = 0;
	
	
	public double scale = 512.0;

	public GenLayerSimplexNoise(long seed, int octaves, float persistence, int amplitude, int offset, double scale) {
		this.amplitude = amplitude;
		this.offset = offset;
		noisegen = new SimplexNoise(octaves, persistence, seed);
		this.scale = scale;
	}
	
	
	public GenLayerSimplexNoise(long seed, int octaves, float persistence, int amplitude, int offset) {
		this.amplitude = amplitude;
		this.offset = offset;
		noisegen = new SimplexNoise(octaves, persistence, seed);
	}
	
	public int[] getInts(int xCoord, int zCoord, int sizeX, int sizeZ) {
		int[] cache = new int[sizeX * sizeZ];

		for (int z = 0; z < sizeZ; ++z) {
			for (int x = 0; x < sizeX; ++x) {
				cache[x + z * sizeX] = Math.max(0, Math.min(255, offset + (int)(amplitude * (1f + noisegen.getNoise((xCoord + x) / scale, yCoord, (zCoord + z) / scale)))));
			}
		}

		return cache;
	}

}