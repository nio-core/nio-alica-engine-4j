package de.uniks.vs.jalica.engine.constrainmodule;


import de.uniks.vs.jalica.common.utils.CommonUtils;

import java.util.ArrayList;

/**
 *  23.6.19
 */
public class Variant {

    enum Type {
        TypeDouble, // blatant optimization for the gradient solver case
        TypeFloat,
        TypeBool,
        TypePtr,
        TypeInt,
        TypeIdent,
        TypeNone
    };

    class Data {
        public double asDouble;
        public float asFloat;
        public boolean asBool;
        public Object asPtr;
        public long asInt;
//        BBIdent asIdent;
//        uint8_t asRaw[kUnionSize];
    }

    Type type;
    Data value = new Data();
    // annoyingly, C++11 does not yet allow std::max in constexpr (nor does it have an initializer list version)
//    static constexpr size_t kUnionSize = sizeof(double) > sizeof(void*) ? sizeof(double) : sizeof(void*);
//    static constexpr size_t kVariantSize = kUnionSize + 1;

    Variant() {
        this.type = Type.TypeNone;
    }

     Variant(double d) {
         this.type = Type.TypeDouble;
         this.value.asDouble = d;
    }

     Variant(float f){
        this.type = Type.TypeFloat;
        this.value.asFloat = f;
    }

     Variant(long i){
            this.type = Type.TypeInt;
        this.value.asInt = i;
    }

     Variant(boolean b){
         this.type = Type.TypeBool;
        this.value.asBool = b;
    }

     Variant(Object ptr) {
         this.type = Type.TypePtr;
        this.value.asPtr = ptr;
    }

    // Test:
    boolean isSet()  { return this.type != Type.TypeNone; }
    boolean isDouble()  { return this.type == Type.TypeDouble; }
    boolean isFloat()  { return this.type == Type.TypeFloat; }
    boolean isInt()  { return this.type == Type.TypeInt; }
    boolean isBool()  { return this.type == Type.TypeBool; }
    boolean isPtr()  { return this.type == Type.TypePtr; }
    boolean isIdent()  { return this.type == Type.TypeIdent; }

    // Get:

    double getDouble() {
        assert(this.type == Type.TypeDouble);
        return this.value.asDouble;
    }
    float getFloat() {
        assert(this.type == Type.TypeFloat);
        return this.value.asFloat;
    }

    long getInt() {
        assert(this.type == Type.TypeInt);
        return this.value.asInt;
    }

    boolean getBool() {
        assert(this.type == Type.TypeBool);
        return this.value.asBool;
    }

    Object getPtr() {
        assert(this.type == Type.TypePtr);
        return this.value.asPtr;
    }

//    BBIdent getIdent() {
//        assert(this.type == TypeIdent);
//        return this.value.asIdent;
//    }

    // Set:
    void setDouble(double d) {
        this.type = Type.TypeDouble;
        this.value.asDouble = d;
    }
    void setFloat(float f) {
        this.type = Type.TypeFloat;
        this.value.asFloat = f;
    }
    void setInt(long i) {
        this.type = Type.TypeInt;
        this.value.asInt = i;
    }
    void setBool(boolean b)
    {
        this.type = Type.TypeBool;
        this.value.asBool = b;
    }
    void setPtr(Object ptr)
    {
        this.type = Type.TypePtr;
        this.value.asPtr = ptr;
    }
//    void setIDent(BBIdent id)
//    {
//        this.type = Type.TypeIdent;
//        this.value.asIdent = id;
//    }

    public void serializeTo(ArrayList<Integer> value) {
        value.add(this.type.ordinal());
        CommonUtils.aboutImplIncomplete("Solver Variant value serializeTo");
    }

//    int serializeTo(uint8_t* arr)
//    {
//        arr[0] = static_cast<uint8_t>(this.type);
//        memcpy(arr + 1, &this.value.asRaw, kUnionSize);
//        return static_cast<int>(kVariantSize);
//    }

    public int loadFrom(ArrayList<Integer> value) {
        this.type = Type.values()[value.get(0)];
//        this.value.asRaw = value.get(1);
//        return static_cast<int>(kVariantSize);
        CommonUtils.aboutImplIncomplete("Solver Variant value loadFrom");
        return 0;
    }
//    int loadFrom( uint8_t* arr)
//    {
//        this.type = static_cast<Type>(arr[0]);
//        memcpy(&this.value.asRaw, arr + 1, kUnionSize);
//        return static_cast<int>(kVariantSize);
//    }

}
