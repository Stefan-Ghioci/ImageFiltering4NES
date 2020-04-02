package run;

import ai.HierarchicalAgglomerativeClustering;
import ai.impl.BlockConfigHAC;
import model.BlockConfig;
import model.PixelColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.ImageProcessing;
import utils.ColorMathUtils;
import utils.ImageUtils;

import java.io.File;
import java.util.List;

import static model.Constants.CLUSTER_COUNT;

public class Compress
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Compress.class);

    public static void main(String[] args)
    {
        String filename = args[0];

        File textFile = new File("generated/" + filename + ".txt");
        PixelColor[][] image = ImageUtils.loadFile("generated/" + filename + ".bmp");

        List<BlockConfig> blockConfigList = ImageUtils.loadBlockConfigs(textFile, image);

        HierarchicalAgglomerativeClustering<BlockConfig> hac = new BlockConfigHAC();

        List<List<BlockConfig>> clusteredBlockConfigList = hac.run(blockConfigList, CLUSTER_COUNT);


        {
            List<BlockConfig>
                    compressedBlockConfigList =
                    ImageProcessing.compress(clusteredBlockConfigList, image, false);
            PixelColor[][] compressedImage = ImageUtils.convertBlockConfigsToPixelArray(compressedBlockConfigList);

            double avgDiff = ColorMathUtils.calculateAvgDiff(compressedImage, image);
            LOGGER.info("Compression finished on {} with avg diff {}", filename, (int) avgDiff);

            ImageUtils.saveFile(compressedImage, "solution/" + filename + "_compressed_" + (int) avgDiff + ".bmp");
        }

        {
            List<BlockConfig>
                    compressedBlockConfigList =
                    ImageProcessing.compress(clusteredBlockConfigList, image, true);
            PixelColor[][] compressedImage = ImageUtils.convertBlockConfigsToPixelArray(compressedBlockConfigList);

            double avgDiff = ColorMathUtils.calculateAvgDiff(compressedImage, image);
            LOGGER.info("Compression (fine) finished on {} with avg diff {}", filename, (int) avgDiff);

            ImageUtils.saveFile(compressedImage, "solution/" + filename + "_fine_compressed_" + (int) avgDiff + ".bmp");
        }
    }
}
