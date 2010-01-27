package org.jf.dexlib.Code.Analysis;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.TypeIdItem;
import static org.jf.dexlib.Code.Analysis.ClassPath.ClassDef;
import static org.jf.dexlib.Code.Analysis.ClassPath.PrimitiveClassDef;
import static org.jf.dexlib.Code.Analysis.ClassPath.ArrayClassDef;
import java.util.HashMap;

public class RegisterType {
    private final static HashMap<RegisterType, RegisterType> internedRegisterTypes =
            new HashMap<RegisterType, RegisterType>();

    public final Category category;
    public final ClassDef type;

    private RegisterType(Category category, ClassDef type) {
        assert ((category == Category.Reference || category == Category.UninitRef) && type != null) ||
               ((category != Category.Reference && category != Category.UninitRef) && type == null);

        this.category = category;
        this.type = type;
    }

    @Override
    public String toString() {
        return "(" + category.name() + (type==null?"":("," + type.getClassType())) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterType that = (RegisterType) o;

        if (category != that.category) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = category.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public static enum Category {
        //the Unknown category denotes a register type that hasn't been determined yet
        Unknown,
        Null,
        One,
        Boolean,
        Byte,
        PosByte,
        Short,
        PosShort,
        Char,
        Integer,
        Float,
        LongLo,
        LongHi,
        DoubleLo,
        DoubleHi,
        //the UninitRef category is used after a new-instance operation, and before the corresponding <init> is called
        //it is also used for the "this" register inside an <init> method, before the superclass' <init> method is
        //called
        UninitRef,
        Reference,
        //This is used when there are multiple incoming execution paths that have incompatible register types. For
        //example if the register's type is an Integer on one incomming code path, but is a Reference type on another
        //incomming code path. There is no register type that can hold either an Integer or a Reference.
        Conflicted;

        //this table is used when merging register types. For example, if a particular register can be either a Byte
        //or a Char, then the "merged" type of that register would be Integer, because it is the "smallest" type can
        //could hold either type of value.
        protected static Category[][] mergeTable  =
        {
                /*             Unknown      Null        One,        Boolean     Byte        PosByte     Short       PosShort    Char        Integer,    Float,      LongLo      LongHi      DoubleLo    DoubleHi    UninitRef   Reference   Conflicted*/
                /*Unknown*/    {Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown,    Unknown},
                /*Null*/       {Unknown,    Null,       Conflicted, Boolean,    Byte,       PosByte,    Short,      PosShort,   Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Conflicted},
                /*One*/        {Unknown,    Conflicted, One,        Boolean,    Byte,       PosByte,    Short,      PosShort,   Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Boolean*/    {Unknown,    Boolean,    Boolean,    Boolean,    Byte,       PosByte,    Short,      PosShort,   Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Byte*/       {Unknown,    Byte,       Byte,       Byte,       Byte,       Byte,       Short,      Short,      Integer,    Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*PosByte*/    {Unknown,    PosByte,    PosByte,    PosByte,    Byte,       PosByte,    Short,      PosShort,   Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Short*/      {Unknown,    Short,      Short,      Short,      Short,      Short,      Short,      Short,      Integer,    Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*PosShort*/   {Unknown,    PosShort,   PosShort,   PosShort,   Short,      PosShort,   Short,      PosShort,   Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Char*/       {Unknown,    Char,       Char,       Char,       Integer,    Char,       Integer,    Char,       Char,       Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Integer*/    {Unknown,    Integer,    Integer,    Integer,    Integer,    Integer,    Integer,    Integer,    Integer,    Integer,    Integer,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*Float*/      {Unknown,    Float,      Float,      Float,      Float,      Float,      Float,      Float,      Float,      Integer,    Float,      Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted},
                /*LongLo*/     {Unknown,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, LongLo,     Conflicted, LongLo,     Conflicted, Conflicted, Conflicted, Conflicted},
                /*LongHi*/     {Unknown,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, LongHi,     Conflicted, LongHi,     Conflicted, Conflicted, Conflicted},
                /*DoubleLo*/   {Unknown,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, LongLo,     Conflicted, DoubleLo,   Conflicted, Conflicted, Conflicted, Conflicted},
                /*DoubleHi*/   {Unknown,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, LongHi,     Conflicted, DoubleHi,   Conflicted, Conflicted, Conflicted},
                /*UninitRef*/  {Unknown,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, UninitRef,  Conflicted, Conflicted},
                /*Reference*/  {Unknown,    Reference,  Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Reference,  Conflicted},
                /*Conflicted*/ {Unknown,    Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted, Conflicted}
        };

        //this table is used to denote whether a given value type can be assigned to a "slot" of a certain type. For
        //example, to determine if you can assign a Boolean value to a particular array "slot", where the array is an
        //array of Integers, you would look up assignmentTable[Boolean.ordinal()][Integer.ordinal()]
        //Note that not all slot types in the table are expected to be used. For example, it doesn't make sense to
        //check if a value can be assigned to an uninitialized reference slot - because there is no such thing.
        protected static boolean[][] assigmentTable =
        {
                /*             Unknown      Null        One,        Boolean     Byte        PosByte     Short       PosShort    Char        Integer,    Float,      LongLo      LongHi      DoubleLo    DoubleHi    UninitRef   Reference   Conflicted  |slot type*/
                /*Unknown*/    {false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false},
                /*Null*/       {false,      true,       false,      true,       true,       true,       true,       true,       true,       true,       true,       false,      false,      false,      false,      false,      true,       false},
                /*One*/        {false,      false,      true,       true,       true,       true,       true,       true,       true,       true,       true,       false,      false,      false,      false,      false,      false,      false},
                /*Boolean*/    {false,      false,      false,      true,       true,       true,       true,       true,       true,       true,       true,       false,      false,      false,      false,      false,      false,      false},
                /*Byte*/       {false,      false,      false,      false,      true,       false,      true,       true,       false,      true,       true,       false,      false,      false,      false,      false,      false,      false},
                /*PosByte*/    {false,      false,      false,      false,      true,       true,       true,       true,       true,       true,       true,       false,      false,      false,      false,      false,      false,      false},
                /*Short*/      {false,      false,      false,      false,      false,      false,      true,       false,      false,      true,       true,       false,      false,      false,      false,      false,      false,      false},
                /*PosShort*/   {false,      false,      false,      false,      false,      false,      true,       true,       true,       true,       true,       false,      false,      false,      false,      false,      false,      false},
                /*Char*/       {false,      false,      false,      false,      false,      false,      false,      false,      true,       true,       true,       false,      false,      false,      false,      false,      false,      false},
                /*Integer*/    {false,      false,      false,      false,      false,      false,      false,      false,      false,      true,       true,       false,      false,      false,      false,      false,      false,      false},
                /*Float*/      {false,      false,      false,      false,      false,      false,      false,      false,      false,      true,       true,       false,      false,      false,      false,      false,      false,      false},
                /*LongLo*/     {false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      true,       false,      true,       false,      false,      false,      false},
                /*LongHi*/     {false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      true,       false,      true,       false,      false,      false},
                /*DoubleLo*/   {false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      true,       false,      true,       false,      false,      false,      false},
                /*DoubleHi*/   {false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      true,       false,      true,       false,      false,      false},
                /*UninitRef*/  {false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false},
                /*Reference*/  {false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      true,       false},
                /*Conflicted*/ {false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false,      false}
                /*----------*/
                /*value type*/
        };

    }

    public static RegisterType getRegisterTypeForType(String type) {
        switch (type.charAt(0)) {
            case 'V':
                throw new ValidationException("The V type can only be used as a method return type");
            case 'Z':
                return getRegisterType(Category.Boolean, null);
            case 'B':
                return getRegisterType(Category.Byte, null);
            case 'S':
                return getRegisterType(Category.Short, null);
            case 'C':
                return getRegisterType(Category.Char, null);
            case 'I':
                return getRegisterType(Category.Integer, null);
            case 'F':
                return getRegisterType(Category.Float, null);
            case 'J':
                return getRegisterType(Category.LongLo, null);
            case 'D':
                return getRegisterType(Category.DoubleLo, null);
            case 'L':
            case '[':
                return getRegisterType(Category.Reference, ClassPath.getClassDef(type));
            default:
                throw new RuntimeException("Invalid type: " + type);
        }
    }

    public static RegisterType getRegisterTypeForTypeIdItem(TypeIdItem typeIdItem) {
        return getRegisterTypeForType(typeIdItem.getTypeDescriptor());
    }

    public static RegisterType getWideRegisterTypeForTypeIdItem(TypeIdItem typeIdItem, boolean firstRegister) {
        if (typeIdItem.getRegisterCount() == 1) {
            throw new RuntimeException("Cannot use this method for non-wide register type: " +
                    typeIdItem.getTypeDescriptor());
        }

        switch (typeIdItem.getTypeDescriptor().charAt(0)) {
            case 'J':
                if (firstRegister) {
                    return getRegisterType(Category.LongLo, null);
                } else {
                    return getRegisterType(Category.LongHi, null);
                }
            case 'D':
                if (firstRegister) {
                    return getRegisterType(Category.DoubleLo, null);
                } else {
                    return getRegisterType(Category.DoubleHi, null);
                }
            default:
                throw new RuntimeException("Invalid type: " + typeIdItem.getTypeDescriptor());
        }
    }

    public static RegisterType getRegisterTypeForLiteral(long literalValue) {
        if (literalValue < -32768) {
            return getRegisterType(Category.Integer, null);
        }
        if (literalValue < -128) {
            return getRegisterType(Category.Short, null);
        }
        if (literalValue < 0) {
            return getRegisterType(Category.Byte, null);
        }
        if (literalValue == 0) {
            return getRegisterType(Category.Null, null);
        }
        if (literalValue == 1) {
            return getRegisterType(Category.One, null);
        }
        if (literalValue < 128) {
            return getRegisterType(Category.PosByte, null);
        }
        if (literalValue < 32768) {
            return getRegisterType(Category.PosShort, null);
        }
        if (literalValue < 65536) {
            return getRegisterType(Category.Char, null);
        }
        return getRegisterType(Category.Integer, null);
    }

    public RegisterType merge(RegisterType type) {
        if (type == null || type == this) {
            return this;
        }

        Category mergedCategory = Category.mergeTable[this.category.ordinal()][type.category.ordinal()];
        if (mergedCategory == Category.Conflicted) {
            throw new ValidationException("Incompatible register types." +
                    " Category1: " + this.category.name() +
                    (this.type==null?"":" Type1: " + this.type.getClassType()) +
                    " Category2: " + type.category.name() +
                    (type.type==null?"":" Type2: " + type.type.getClassType()));
        }

        ClassDef mergedType = null;
        if (mergedCategory == Category.Reference) {
            mergedType = ClassPath.getCommonSuperclass(this.type, type.type);
        }
        return RegisterType.getRegisterType(mergedCategory, mergedType);
    }

    public boolean canBeAssignedTo(RegisterType slotType) {
        if (Category.assigmentTable[this.category.ordinal()][slotType.category.ordinal()]) {
            if (this.category == Category.Reference && slotType.category == Category.Reference) {
                if (!slotType.type.isInterface()) {
                    return this.type.extendsClass(slotType.type);
                }
                //for verification, we assume all objects implement all interfaces, so we don't verify the type if
                //slotType is an interface
            }
            return true;
        }
        return false;
    }

    public static RegisterType getRegisterType(Category category, ClassDef classType) {
        RegisterType newRegisterType = new RegisterType(category, classType);
        RegisterType internedRegisterType = internedRegisterTypes.get(newRegisterType);
        if (internedRegisterType == null) {
            internedRegisterTypes.put(newRegisterType, newRegisterType);
            return newRegisterType;
        }
        return internedRegisterType;
    }
}