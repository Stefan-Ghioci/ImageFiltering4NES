package ai.impl;

import ai.KMeansClusteringAlgorithm;
import model.BlockConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static model.Constants.BLOCK_SIZE;

public class BlockConfigAlgorithm extends KMeansClusteringAlgorithm<BlockConfig>
{

    @Override
    protected BlockConfig center(List<BlockConfig> cluster)
    {
        Integer[][] mapping = new Integer[BLOCK_SIZE][BLOCK_SIZE];

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
            {
//                double avgValue = 0;
//                for (BlockConfig blockConfig : cluster)
//                    avgValue = avgValue + blockConfig.getMapping()[x][y];
//                avgValue /= cluster.size();
//
//                mapping[x][y] = Math.toIntExact(Math.round(avgValue));

                List<Integer> values = new ArrayList<>();
                for (BlockConfig blockConfig : cluster)
                    values.add(blockConfig.getMapping()[x][y]);


                // noinspection OptionalGetWithoutIsPresent
                mapping[x][y] = values.stream()
                                      .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                                      .entrySet()
                                      .stream()
                                      .max(Map.Entry.comparingByValue())
                                      .get()
                                      .getKey();
            }

        return new BlockConfig(null, null, mapping, null);
    }

    @Override
    protected double calculateDistance(BlockConfig blockConfig, BlockConfig centroid)
    {
        int distance = 0;

        Integer[][] mapping = blockConfig.getMapping();
        Integer[][] centroidMapping = centroid.getMapping();

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
            {
                if (!mapping[x][y].equals(centroidMapping[x][y]))
                    distance++;
//                distance += (mapping[x][y] - centroidMapping[x][y]) * (mapping[x][y] - centroidMapping[x][y]);
            }

        return distance;
    }

    @Override
    protected BlockConfig generateCentroid()
    {
        Integer[][] mapping = new Integer[BLOCK_SIZE][BLOCK_SIZE];

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
            {
                mapping[x][y] = (int) (Math.random() * 4);
            }

        return new BlockConfig(null, null, mapping, null);
    }
}
