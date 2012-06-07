package st.redline.core.reflector;

import java.util.HashMap;
import java.util.Map;

public class ConstructorInspector implements InspectorVisitor {

    private static final Map<String, String> PRIMITIVE_TO_SIGNATURE_TYPE = new HashMap<String, String>();
    static {
        PRIMITIVE_TO_SIGNATURE_TYPE.put("long", "J");
        PRIMITIVE_TO_SIGNATURE_TYPE.put("int", "I");
        PRIMITIVE_TO_SIGNATURE_TYPE.put("char", "C");
        PRIMITIVE_TO_SIGNATURE_TYPE.put("byte", "B");
    }

    private final Reflector reflector;
    private String className;
    private String javaClassName;
    private String javaConstructorName;
    private StringBuilder javaArgumentSignature = new StringBuilder();
    private String[] javaArgumentTypes;
    private String classNameAdaptor;

    public ConstructorInspector(Reflector reflector) {
        this.reflector = reflector;
    }

    public void visitBegin(String className) {
        throw new IllegalStateException("This inspector should not be getting this.");
    }

    public void visitEnd(String className) {
        throw new IllegalStateException("This inspector should not be getting this.");
    }

    public void visitConstructorBegin(String className, String constructorName, int parameterCount) {
        this.className = className;
        this.classNameAdaptor = className.substring(className.lastIndexOf('.') + 1) + "Adaptor";
        this.javaClassName = className.replace(".", "/");
        this.javaConstructorName = constructorName.replace(".", "/");
        this.javaArgumentTypes = new String[parameterCount];
        reflector.append("\n")
                 .append(classNameAdaptor)
                 .append(" class atSelector: #");
    }

    public void visitConstructorEnd(String className, String constructorName, int parameterCount) {
        reflector.append("      dup;\n");
        loadConstructorArguments(parameterCount);
        reflector.append("      invokeSpecial: '")
            .append(javaConstructorName)
            .append("' method: '<init>' matching: '(")
            .append(javaArgumentSignature.toString())
            .append(")V';\n")
            .append("      invokeVirtual: 'st/redline/core/PrimObject' method: 'javaValue' matching: '(Ljava/lang/Object;)Lst/redline/core/PrimObject;'.\n")
            .append("  ^ obj.\n")
            .append("].\n");
        reflector.usePreviousVisitor();
    }

    private void loadConstructorArguments(int parameterCount) {
        for (int i = 0; i < parameterCount; i++) {
            reflector.append("      arg: 0 at: ")
                     .append(i + 1)
                     .append(";\n")
                     .append("      invokeVirtual: 'st/redline/core/PrimObject' method: 'javaValue' matching: '()Ljava/lang/Object;';\n");
            appendArgumentConversion(javaArgumentTypes[i]);
        }
    }

    private void appendArgumentConversion(String type) {
        if (type.startsWith("L")) {
            reflector.append("      checkcast: '")
                     .append(type)
                     .append("';\n");
        } else {
            // type is primitive so map from Redline internal type to java type.
            if (type.equals("I")) {
                reflector.append("      checkcast: 'java/math/BigDecimal';\n")
                         .append("      invokeVirtual: 'java/math/BigDecimal' method: 'intValue' matching: '()I';\n");
            } else if (type.equals("J")) {
                reflector.append("      checkcast: 'java/math/BigDecimal';\n")
                         .append("      invokeVirtual: 'java/math/BigDecimal' method: 'longValue' matching: '()J';\n");
            } else {
                throw new IllegalStateException("Need to cater for conversion of type '" + type + "'.");
            }
        }
    }

    public void visitParameterTypesBegin(int length) {
        reflector.append(length == 0 ? "new" : "with:")
            .append(" put: [")
            .append(length == 0 ? "" : " :args")
            .append(" || obj |\n  obj := ")
            .append(classNameAdaptor)
            .append(" new.\n")
            .append("  JVM temp: 0;\n")
            .append("      new: '")
            .append(javaClassName)
            .append("';\n");
    }

    public void visitParameterTypesEnd(int length) {
    }

    public void visitParameterType(String parameterType, int index) {
//        System.out.println("Parameter: " + parameterType + " @ " + index);
        String javaType = javaSignature(parameterType);
        javaArgumentTypes[index] = javaType;
        javaArgumentSignature.append(javaType);
    }

    private String javaSignature(String parameterType) {
        if (parameterType.indexOf('.') != -1)
            return "L" + parameterType.replace(".", "/") + ";";
        else
            return PRIMITIVE_TO_SIGNATURE_TYPE.get(parameterType);
    }
}
