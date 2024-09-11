package mb.dnm.mapper;

public class MesimPlaceHolderParam {
    private static final String START_INDICATOR = "@{";
    private static final String END_INDICATOR = "}";
    private static PlaceHolderParamResolver resolver = new DefaultPlaceHolderParamResolver();

    protected MesimPlaceHolderParam() {}

    private static boolean isValidExpression(String expression) {
        if (expression.startsWith(START_INDICATOR) && expression.endsWith(END_INDICATOR)) {
            return true;
        }
        return false;
    }

    public static String toSeemless(String expression) throws PlaceHolderParamParsingException {
        int semiColIdx = expression.lastIndexOf(":");
        String valuePart = "";
        //expression = expression.replace(" ", "");
        expression = expression.trim();
        if (semiColIdx != -1) {
            valuePart = expression.substring(expression.lastIndexOf(":"));
            expression = expression.substring(0, expression.lastIndexOf(":")).toLowerCase();
        } else {
            expression = expression.toLowerCase();
        }
        if (!isValidExpression(expression))
            throw new PlaceHolderParamParsingException("Couldn't resolve the expression '" + expression + "' to placeHolderParameter");
        return expression + valuePart;
    }

    public static boolean isPropertyAvailable(String expression) {
        try {
            expression = toSeemless(expression);
        } catch (PlaceHolderParamParsingException pe) {
            return false;
        }
        return resolver.isPropertyAvailable(expression);
    }

    public static String getValueFrom(String expression, Object fromObj) throws Exception {
        expression = toSeemless(expression);
        return resolver.getValueFrom(expression, fromObj);
    }

    public static void setResolver(PlaceHolderParamResolver resolver0) {
        resolver = resolver0;
    }

}
