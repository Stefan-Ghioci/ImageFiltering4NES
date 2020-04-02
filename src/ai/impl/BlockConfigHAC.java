package ai.impl;

import ai.HierarchicalAgglomerativeClustering;
import model.BlockConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;
import static model.Constants.BLOCK_SIZE;

public class BlockConfigHAC extends HierarchicalAgglomerativeClustering<BlockConfig>
{
    private Map<BlockConfig, int[]> histogramMap = new HashMap<>();


    @Override
    protected double distance(BlockConfig blockConfig1, BlockConfig blockConfig2)
    {
        int[] histogram1 = histogramMap.computeIfAbsent(blockConfig1, this::createHistogram);
        int[] histogram2 = histogramMap.computeIfAbsent(blockConfig2, this::createHistogram);

        return sqrt(IntStream.range(0, 4)
                             .mapToDouble(i -> (histogram1[i] - histogram2[i]) * (histogram1[i] - histogram2[i]))
                             .sum());
    }

    private int[] createHistogram(BlockConfig blockConfig)
    {
        int[] histogram = new int[4];

        Integer[][] mapping = blockConfig.getMapping();

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
                histogram[mapping[x][y]]++;

        return histogram;
    }

}
