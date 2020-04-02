package model;

import java.util.List;

public class BlockConfig
{
    private final Integer row;
    private final Integer column;
    private final Integer[][] mapping;
    private final List<PixelColor> subpalette;

    public BlockConfig(Integer row, Integer column, Integer[][] mapping, List<PixelColor> subpalette)
    {
        this.row = row;
        this.column = column;
        this.mapping = mapping;
        this.subpalette = subpalette;
    }

    public List<PixelColor> getSubpalette()
    {
        return subpalette;
    }

    public Integer getRow()
    {
        return row;
    }

    public Integer getColumn()
    {
        return column;
    }

    public Integer[][] getMapping()
    {
        return mapping;
    }


}
