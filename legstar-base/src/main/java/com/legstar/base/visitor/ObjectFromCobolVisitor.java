package com.legstar.base.visitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.legstar.base.context.CobolContext;
import com.legstar.base.type.CobolType;
import com.legstar.base.type.ConversionException;
import com.legstar.base.type.composite.CobolArrayType;
import com.legstar.base.type.composite.CobolChoiceType;
import com.legstar.base.type.composite.CobolComplexType;
import com.legstar.base.type.primitive.CobolPrimitiveType;

/**
 * Convert a mainframe data to a java Object.
 * <p/>
 * COBOL Complex types are converted to java Maps.
 * 
 */
public class ObjectFromCobolVisitor extends FromCobolVisitor {

    /**
     * Set of unique handlers to receive notifications from
     * {@link FromCobolVisitor}
     */
    private final ObjectPrimitiveTypeHandler primitiveTypeHandler;
    private final ObjectChoiceTypeAlternativeHandler choiceTypeAlternativeHandler;

    /** Last java object produced by visiting an item. */
    private Object lastObject;

    public ObjectFromCobolVisitor(CobolContext cobolContext, byte[] hostData,
            int start) {
        this(cobolContext, hostData, start, null);
    }

    public ObjectFromCobolVisitor(CobolContext cobolContext, byte[] hostData,
            int start, FromCobolChoiceStrategy customChoiceStrategy) {
        super(cobolContext, hostData, start, customChoiceStrategy);
        primitiveTypeHandler = new ObjectPrimitiveTypeHandler();
        choiceTypeAlternativeHandler = new ObjectChoiceTypeAlternativeHandler();
    }

    public void visit(CobolComplexType type) throws ConversionException {
        final Map < String, Object > map = new LinkedHashMap < String, Object >();
        super.visitComplexType(type, new ObjectComplexTypeChildHandler(map));
        lastObject = map;
    }

    public void visit(CobolArrayType type) throws ConversionException {
        final List < Object > list = new ArrayList < Object >();
        super.visitCobolArrayType(type, new ObjectArrayTypeItemHandler(list));
        lastObject = list;
    }

    public void visit(CobolChoiceType type) throws ConversionException {
        super.visitCobolChoiceType(type, choiceTypeAlternativeHandler);
    }

    public void visit(CobolPrimitiveType < ? > type) throws ConversionException {
        super.visitCobolPrimitiveType(type, primitiveTypeHandler);
    }

    private class ObjectComplexTypeChildHandler implements
            ComplexTypeChildHandler {

        private final Map < String, Object > map;

        public ObjectComplexTypeChildHandler(Map < String, Object > map) {
            this.map = map;
        }

        public boolean postVisit(String fieldName, CobolType child) {
            map.put(fieldName, lastObject);
            return true;
        }
    }

    private class ObjectArrayTypeItemHandler implements ArrayTypeItemHandler {

        private final List < Object > list;

        public ObjectArrayTypeItemHandler(List < Object > list) {
            this.list = list;
        }

        public boolean postVisit(CobolType item) {
            list.add(lastObject);
            return true;
        }

    }

    private class ObjectChoiceTypeAlternativeHandler implements
            ChoiceTypeAlternativeHandler {

        public void postVisit(String alternativeName, CobolType alternative) {
            // Wrap the chosen alternative in a map
            final Map < String, Object > map = new LinkedHashMap < String, Object >();
            map.put(alternativeName, lastObject);
            lastObject = map;
        }

    }

    private class ObjectPrimitiveTypeHandler implements PrimitiveTypeHandler {

        public void postVisit(CobolType type, Object value) {
            lastObject = value;
        }

    };

    public Object getObject() {
        return lastObject;
    }

}