import ai.KMeansClustering;
import ai.impl.BlockMappingClustering;
import model.BlockMapping;
import model.Constants;
import utils.ImageUtils;

import java.util.List;

public class Compress
{
    public static void main(String[] args)
    {
        String filename = args[0];
        int iterationCount = Integer.parseInt(args[1]);

        List<BlockMapping> blockMappingList = ImageUtils.loadGeneratedBlockMappings(filename);
        KMeansClustering<BlockMapping> algorithm = new BlockMappingClustering();

        List<List<BlockMapping>> clusteredBlockMappingList = algorithm.run(blockMappingList, Constants.CLUSTER_COUNT, iterationCount);

        ImageUtils.saveFile(blockMappingList, filename + "_compressed");
    }
}
