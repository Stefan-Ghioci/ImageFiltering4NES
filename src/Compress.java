import ai.KMeansClusteringAlgorithm;
import ai.impl.BlockConfigAlgorithm;
import ai.impl.SubpaletteAlgorithm;
import ai.impl.SubpaletteConfig;
import model.BlockConfig;
import model.PixelColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.ImageProcessing;
import utils.ColorMathUtils;
import utils.ImageUtils;

import java.io.File;
import java.util.List;

public class Compress
{
    private final static Logger LOGGER = LoggerFactory.getLogger(Compress.class);

    public static void main(String[] args)
    {
        String filename = args[0];
        int iterationCount = Integer.parseInt(args[1]);

        File textFile = new File("generated/" + filename + ".txt");
        PixelColor[][] image = ImageUtils.loadFile("generated/" + filename + ".bmp");

        List<BlockConfig> blockConfigList = ImageUtils.loadGeneratedBlockConfigs(textFile, image);

        KMeansClusteringAlgorithm<BlockConfig> algorithm = new BlockConfigAlgorithm();

        List<List<BlockConfig>> clusteredBlockConfigList = algorithm.run(blockConfigList, iterationCount);

        List<BlockConfig> compressedBlockConfigList = ImageProcessing.compress(clusteredBlockConfigList, image);
        PixelColor[][] compressedImage = ImageUtils.convertBlockConfigsToPixelArray(compressedBlockConfigList);

        double avgDiff = ColorMathUtils.calculateAvgDiff(compressedImage, image);

        LOGGER.info("Compression finished on {} with avg diff {}", filename, (int) avgDiff);

        ImageUtils.saveFile(compressedImage, "solution/" + filename + "_compressed_" + (int) avgDiff + ".bmp");
    }
}
