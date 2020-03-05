package ai.impl;

import ai.KMeansClustering;
import model.BlockMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static model.Constants.BLOCK_SIZE;

public class BlockMappingClustering extends KMeansClustering<BlockMapping>
{

    @Override
    protected BlockMapping center(List<BlockMapping> cluster)
    {
        Integer[][] mapping = new Integer[BLOCK_SIZE][BLOCK_SIZE];

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
            {
//                double avgValue = 0;
//                for (BlockMapping blockMapping : cluster)
//                    avgValue = avgValue + blockMapping.getMapping()[x][y];
//                avgValue /= cluster.size();
//
//                mapping[x][y] = Math.toIntExact(Math.round(avgValue));

                List<Integer> values = new ArrayList<>();
                for (BlockMapping blockMapping : cluster)
                    values.add(blockMapping.getMapping()[x][y]);


                // noinspection OptionalGetWithoutIsPresent
                mapping[x][y] = values.stream()
                                      .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                                      .entrySet()
                                      .stream()
                                      .max(Map.Entry.comparingByValue())
                                      .get()
                                      .getKey();
            }

        return new BlockMapping(null, null, mapping, null);
    }

    @Override
    protected double calculateDistance(BlockMapping blockMapping, BlockMapping centroid)
    {
        int distance = 0;

        Integer[][] mapping = blockMapping.getMapping();
        Integer[][] centroidMapping = centroid.getMapping();

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
                if (!mapping[x][y].equals(centroidMapping[x][y]))
                    distance++;

        return distance;
    }

    @Override
    protected BlockMapping generateCentroid()
    {
        Integer[][] mapping = new Integer[BLOCK_SIZE][BLOCK_SIZE];

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
            {
                mapping[x][y] = (int) (Math.random() * 4);
            }

        return new BlockMapping(null, null, mapping, null);
    }
}
