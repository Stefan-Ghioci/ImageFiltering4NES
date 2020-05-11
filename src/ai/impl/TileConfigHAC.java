package ai.impl;

import ai.HierarchicalAgglomerativeClustering;
import model.TileConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;
import static model.Constants.TILE_SIZE;

public class TileConfigHAC extends HierarchicalAgglomerativeClustering<TileConfig>
{
    private final Map<TileConfig, int[]> frequencyMap = new HashMap<>();


    @Override
    protected double distance(TileConfig tileConfig1, TileConfig tileConfig2)
    {
        int[] frequencyArray1 = frequencyMap.computeIfAbsent(tileConfig1, this::createFrequencyArray);
        int[] frequencyArray2 = frequencyMap.computeIfAbsent(tileConfig2, this::createFrequencyArray);

        return sqrt(IntStream.range(0, 4)
                             .mapToDouble(i -> (frequencyArray1[i] - frequencyArray2[i]) *
                                               (frequencyArray1[i] - frequencyArray2[i]))
                             .sum());
    }

    private int[] createFrequencyArray(TileConfig tileConfig)
    {
        int[] frequencyArray = new int[4];

        Integer[][] mapping = tileConfig.getMapping();

        for (int x = 0; x < TILE_SIZE; x++)
            for (int y = 0; y < TILE_SIZE; y++)
                frequencyArray[mapping[x][y]]++;

        return frequencyArray;
    }

}
