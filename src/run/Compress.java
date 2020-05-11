package run;

import ai.HierarchicalAgglomerativeClustering;
import ai.impl.TileConfigHAC;
import model.PixelColor;
import model.TileConfig;
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

        List<TileConfig> tileConfigList = ImageUtils.loadTileConfigs(textFile, image);

        HierarchicalAgglomerativeClustering<TileConfig> hac = new TileConfigHAC();

        List<List<TileConfig>> clusteredTileConfigList = hac.run(tileConfigList, CLUSTER_COUNT);


        {
            List<TileConfig>
                    compressedTileConfigList =
                    ImageProcessing.compress(clusteredTileConfigList, image, false);
            PixelColor[][] compressedImage = ImageUtils.convertTileConfigsToPixelArray(compressedTileConfigList);

            double averageDistance = ColorMathUtils.computeAverageDistance(compressedImage, image);
            LOGGER.info("Compression finished on {} with avg dist {}", filename, (int) averageDistance);

            ImageUtils.saveFile(compressedImage,
                                "solution/" + filename + "_compressed_" + (int) averageDistance + ".bmp");
        }

        {
            List<TileConfig>
                    compressedTileConfigList =
                    ImageProcessing.compress(clusteredTileConfigList, image, true);
            PixelColor[][] compressedImage = ImageUtils.convertTileConfigsToPixelArray(compressedTileConfigList);

            double averageDistance = ColorMathUtils.computeAverageDistance(compressedImage, image);
            LOGGER.info("Compression (fine) finished on {} with avg dist {}", filename, (int) averageDistance);

            ImageUtils.saveFile(compressedImage,
                                "solution/" + filename + "_fine_compressed_" + (int) averageDistance + ".bmp");
        }
    }
}
