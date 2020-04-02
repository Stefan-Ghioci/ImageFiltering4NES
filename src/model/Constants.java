package model;

public class Constants
{

    public static final int STD_HEIGHT = 240;
    public static final int STD_WIDTH = 256;
    public static final int BLOCK_GROUP_SIZE = 16;
    public static final int BLOCK_SIZE = 8;
    public static final int CLUSTER_COUNT = 256;

    private Constants()
    {
        throw new IllegalStateException("Utility class");
    }
}
