package java.lang.invoke;

/** Yalnızca derleme için saplama; d8 lambdaları desugar eder, bu sınıf APK'ye girmez. */
public final class LambdaMetafactory {
    public static final int FLAG_SERIALIZABLE = 1, FLAG_MARKERS = 2, FLAG_BRIDGES = 4;
    public static CallSite metafactory(MethodHandles.Lookup caller, String name, MethodType invokedType, MethodType samMethodType,
                                       MethodHandle implMethod, MethodType instantiatedMethodType) throws LambdaConversionException { return null; }
    public static CallSite altMetafactory(MethodHandles.Lookup caller, String name, MethodType invokedType, Object... args) throws LambdaConversionException { return null; }
}
