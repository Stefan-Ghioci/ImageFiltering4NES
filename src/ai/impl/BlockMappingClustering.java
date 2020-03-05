package ai.impl;

import ai.KMeansClustering;
import model.BlockMapping;

import java.util.List;

public class BlockMappingClustering extends KMeansClustering<BlockMapping>
{
    @Override
    protected BlockMapping calculateAverage(List<BlockMapping> cluster)
    {
        //TODO
        return null;
    }

    @Override
    protected double calculateDistance(BlockMapping blockMapping, BlockMapping centroid)
    {
        //TODO
        return 0;
    }

    @Override
    protected BlockMapping generateCentroid()
    {
        //TODO
        return null;
    }
}
