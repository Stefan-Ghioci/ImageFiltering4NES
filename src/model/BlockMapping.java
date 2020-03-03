package model;

public class BlockMapping
{
    Integer row;
    Integer column;
    Integer[][] mapping;

    public BlockMapping(Integer row, Integer column, Integer[][] mapping)
    {
        this.row = row;
        this.column = column;
        this.mapping = mapping;
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
