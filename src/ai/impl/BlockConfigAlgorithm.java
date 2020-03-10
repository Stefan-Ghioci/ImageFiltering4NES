package ai.impl;

import ai.KMeansClusteringAlgorithm;
import model.BlockConfig;
import model.PixelColor;
import processing.ImageProcessing;
import utils.ColorMathUtils;
import utils.ImageUtils;

import java.util.List;

import static java.lang.Math.pow;
import static model.Constants.BLOCK_SIZE;

public class BlockConfigAlgorithm extends KMeansClusteringAlgorithm<BlockConfig>
{

    private PixelColor[][] image;

    public BlockConfigAlgorithm(PixelColor[][] image)
    {
        this.image = image;
    }

    @Override
    protected double computeCost(List<List<BlockConfig>> clusteredBlockConfigList)
    {
        List<BlockConfig> compressedBlockConfigList = ImageProcessing.compress(clusteredBlockConfigList, image);
        PixelColor[][] compressedImage = ImageUtils.convertBlockConfigsToPixelArray(compressedBlockConfigList);

        return ColorMathUtils.calculateAvgDiff(compressedImage, image);
    }

    @Override
    protected BlockConfig center(List<BlockConfig> cluster)
    {
        Integer[][] mapping = new Integer[BLOCK_SIZE][BLOCK_SIZE];

        for (int x = 0; x < BLOCK_SIZE; x++)
            for (int y = 0; y < BLOCK_SIZE; y++)
            {
                // standard average mapping

//                double avgValue = 0;
//                for (BlockConfig blockConfig : cluster)
//                    avgValue = avgValue + blockConfig.getMapping()[x][y];
//                avgValue /= cluster.size();
//
//                mapping[x][y] = Math.toIntExact(Math.round(avgValue));


                // geometric average mapping

                double geomAvgValue = 1.0;
                for (BlockConfig blockConfig : cluster)
                    geomAvgValue *= blockConfig.getMapping()[x][y];
                geomAvgValue = pow(geomAvgValue, 1.0 / cluster.size());

                mapping[x][y] = Math.toIntExact(Math.round(geomAvgValue));


                // average mapping on most frequent value

//                List<Integer> values = new ArrayList<>();
//                for (BlockConfig blockConfig : cluster)
//                    values.add(blockConfig.getMapping()[x][y]);
//
//
//                // noinspection OptionalGetWithoutIsPresent
//                mapping[x][y] = values.stream()
//                                      .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
//                                      .entrySet()
//                                      .stream()
//                                      .max(Map.Entry.comparingByValue())
//                                      .get()
//                                      .getKey();
            }

        return new BlockConfig(null, null, mapping, null);
    }

    @Override
    protected double calculateDistance(BlockConfig blockConfig1, BlockConfig blockConfig2)
    {
        Integer[][] mapping1 = blockConfig1.getMapping();
        Integer[][] mapping2 = blockConfig2.getMapping();

//        double blockNorm1 = norm(mapping1);
//        double blockNorm2 = norm(mapping2);

//        return abs(blockNorm1 - blockNorm2);


//        return meanSquaredError(mapping1, mapping2);

        return ColorMathUtils.structuralSimilarity(mapping1, mapping2);
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
