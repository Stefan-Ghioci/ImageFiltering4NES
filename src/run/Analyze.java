package run;

import model.PixelColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ColorMathUtils;
import utils.ImageUtils;

public class Analyze
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Analyze.class);

    public static void main(String[] args)
    {
        String path1 = args[0];
        String path2 = args[1];

        PixelColor[][] image1 = ImageUtils.loadFile(path1);
        PixelColor[][] image2 = ImageUtils.loadFile(path2);


        double mse = ColorMathUtils.computeMeanSquaredError(image1, image2);
        double psnr = ColorMathUtils.getPeakSignal2NoiseRatio(mse);

        LOGGER.info("Peak signal-to-noise ratio - {}", psnr);
    }
}
