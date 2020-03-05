import ai.KMeansClustering;
import ai.impl.BlockMappingClustering;
import model.BlockMapping;
import model.PixelColor;
import processing.ImageProcessing;
import utils.ColorMathUtils;
import utils.ImageUtils;

import java.io.File;
import java.util.List;

public class Compress
{
    public static void main(String[] args)
    {
        String filename = args[0];
        int iterationCount = Integer.parseInt(args[1]);

        File textFile = new File("generated/" + filename + ".txt");
        PixelColor[][] image = ImageUtils.loadFile("generated/" + filename + ".bmp");

        List<BlockMapping> blockMappingList = ImageUtils.loadGeneratedBlockMappings(textFile, image);


        KMeansClustering<BlockMapping> algorithm = new BlockMappingClustering();

        List<List<BlockMapping>> clusteredBlockMappingList = algorithm.run(blockMappingList, iterationCount);
        List<BlockMapping> compressedBlockMappingList = ImageProcessing.compress(clusteredBlockMappingList, image);
        PixelColor[][] compressedImage = ImageUtils.convertBlockMappingsToPixelArray(compressedBlockMappingList);

        double avgDiff = ColorMathUtils.calculateAvgDiff(compressedImage, image);

        ImageUtils.saveFile(compressedImage, "solution/" + filename + "_compressed_" + (int) avgDiff + ".bmp");
    }
}
