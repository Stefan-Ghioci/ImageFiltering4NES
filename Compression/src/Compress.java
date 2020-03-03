import model.BlockMapping;
import utils.ImageUtils;

import java.util.List;

public class Compress
{
    public static void main(String[] args)
    {
        String filename = args[0];

        List<BlockMapping> blockMappingList = ImageUtils.loadGeneratedBlockMappings(filename);

        //TODO: K means clustering 960 block mappings -> 256 block mappings

        ImageUtils.saveFile(blockMappingList, filename + "_compressed");
    }
}
