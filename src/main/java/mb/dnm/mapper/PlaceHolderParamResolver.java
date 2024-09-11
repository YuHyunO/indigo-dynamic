package mb.dnm.mapper;

public interface PlaceHolderParamResolver {

    public boolean isPropertyAvailable(String expression);

    public String getValueFrom(String expression, Object fromObj) throws Exception;

}
