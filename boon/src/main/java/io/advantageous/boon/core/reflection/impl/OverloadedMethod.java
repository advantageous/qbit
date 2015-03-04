package io.advantageous.boon.core.reflection.impl;

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.boon.primitive.Arry;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 9/22/14.
 */
public class OverloadedMethod implements MethodAccess {

    List<MethodAccess> methodAccessList = new ArrayList<>();

    List<List<MethodAccess>> methodAccessListByArgNumber = new ArrayList<>();


    List<List<MethodAccess>> methodAccessListByArgNumberWithVarArg = new ArrayList<>();


    {
        for (int index =0; index < 25; index++) {
            methodAccessListByArgNumberWithVarArg.add(null);
            methodAccessListByArgNumber.add(null);
        }
    }

    private boolean lock;


    public OverloadedMethod add(MethodAccess methodAccess) {

        if (lock) {
            Exceptions.die();
        }

        methodAccessList.add(methodAccess);

        if (!methodAccess.method().isVarArgs()) {
            List<MethodAccess> methodAccesses = methodAccessListByArgNumber.get(methodAccess.parameterTypes().length);
            if (methodAccesses == null) {
                methodAccesses = new ArrayList<>();
                methodAccessListByArgNumber.set(methodAccess.parameterTypes().length, methodAccesses);
            }
            methodAccesses.add(methodAccess);
        } else {
            List<MethodAccess> methodAccesses = methodAccessListByArgNumberWithVarArg.get(methodAccess.parameterTypes().length);
            if (methodAccesses == null) {
                methodAccesses = new ArrayList<>();
                methodAccessListByArgNumberWithVarArg.set(methodAccess.parameterTypes().length, methodAccesses);
            }
            methodAccesses.add(methodAccess);
        }
        return this;
    }

    public OverloadedMethod init() {

        if (lock) {
            Exceptions.die();
        }

        for (List<MethodAccess> methodAccesses : methodAccessListByArgNumber) {
            java.util.Collections.sort(methodAccesses);
        }

        lock();
        return this;
    }

    public void lock() {


        lock = true;
    }

    @Override
    public Object invokeDynamic(Object object, Object... args) {

        final int length = args.length;


        final List<MethodAccess> methodAccesses = this.methodAccessListByArgNumber.get(length);


        int maxScore = Integer.MIN_VALUE;
        MethodAccess methodAccess = null;

        for (MethodAccess m : methodAccesses) {
            int score = 1;
            final List<TypeType> paramTypeEnumList = m.paramTypeEnumList();

            if (object == null && !m.isStatic()) {
                continue;
            }


            loop:
            for (int argIndex=0; argIndex < args.length; argIndex++) {

                TypeType type =paramTypeEnumList.get(argIndex);
                Object arg = args[argIndex];

                final TypeType instanceType = TypeType.getInstanceType(arg);

                if (instanceType == type) {
                    score += 2_000;
                    continue;
                }

                switch (type){
                    case BYTE_WRAPPER:
                    case BYTE:
                        score = handleByteArg(score, arg, instanceType);
                        break;

                    case SHORT_WRAPPER:
                    case SHORT:
                        score = handleShortArg(score, arg, instanceType);
                        break;

                    case INTEGER_WRAPPER:
                    case INT:
                        score = handleIntArg(score, arg, instanceType);
                        break;


                    case NULL:
                        score--;
                        break;

                    case LONG_WRAPPER:
                    case LONG:
                        score = handleLongArg(score, arg, instanceType);
                        break;

                    case FLOAT_WRAPPER:
                    case FLOAT:
                        score = handleFloatArg(score, arg, instanceType);
                        break;


                    case DOUBLE_WRAPPER:
                    case DOUBLE:
                        score = handleDoubleArg(score, arg, instanceType);
                        break;


                    case CHAR_WRAPPER:
                    case CHAR:
                        if (instanceType == TypeType.CHAR ||
                                instanceType == TypeType.CHAR_WRAPPER) {
                            score+=1000;
                        }
                        break;

                    case STRING:
                        if (instanceType == TypeType.STRING) {
                            score +=1_000;
                        } else if (instanceType == TypeType.CHAR_SEQUENCE
                                || arg instanceof CharSequence) {
                            score +=500;
                        }
                        break;


                    case INSTANCE:
                        if (instanceType == TypeType.INSTANCE) {
                            if (m.parameterTypes()[argIndex].isInstance(arg)){
                                score+=1000;

                            }
                        } else if (instanceType == TypeType.MAP) {
                            score +=1_000;
                        } else if (instanceType == TypeType.LIST) {
                            score +=500;
                        }
                        break;

                    default:
                        if (instanceType == type) {
                            score+=1000;
                        } else {
                            if (m.parameterTypes()[argIndex].isInstance(arg)){
                                score+=1000;

                            }
                        }

                }

            }

            if (score>maxScore) {
                maxScore = score;
                methodAccess = m;
            }
        }

        if (methodAccess!=null) {
            return methodAccess.invokeDynamic(object, args);
        } else {
            /* Place holder for now. */
            List<MethodAccess> varargMethods = this.methodAccessListByArgNumberWithVarArg.get(0);
            if (varargMethods!=null) {
                varargMethods.get(0).invokeDynamic(args);
            }
        }

        return null;
    }

    private int handleLongArg(int score, Object arg, TypeType instanceType) {
        switch (instanceType) {


            case LONG:
                score += 1000;
                break;

            case LONG_WRAPPER:
                score += 1000;
                break;

            case INT:
                score += 800;
                break;

            case INTEGER_WRAPPER:
                score += 700;
                break;

            case DOUBLE:
                score += 700;
                break;

            case FLOAT:
            case SHORT:
            case BYTE:
                score += 600;
                break;



            case SHORT_WRAPPER:
            case BYTE_WRAPPER:
            case FLOAT_WRAPPER:
            case DOUBLE_WRAPPER:
                score += 500;
                break;



            case STRING:
                score += 400;
                try {
                    arg = Integer.valueOf(arg.toString());

                }catch (Exception ex) {
                    score = Integer.MIN_VALUE;
                }
                break;
        }
        return score;

    }

    private int handleByteArg(int score, Object arg, TypeType instanceType) {
        if (instanceType== TypeType.BYTE|| instanceType == TypeType.BYTE_WRAPPER) {
            return score + 1010;
        } else {
            return handleIntArg(score, arg, instanceType);
        }
    }

    private int handleShortArg(int score, Object arg, TypeType instanceType) {
        if (instanceType== TypeType.SHORT || instanceType == TypeType.SHORT_WRAPPER) {
            return score + 1010;
        } else {
            return handleIntArg(score, arg, instanceType);
        }
    }

    private int handleIntArg(int score, Object arg, TypeType instanceType) {
        switch (instanceType) {

            case INT:
                score += 1000;
                break;

            case INTEGER_WRAPPER:
                score += 900;
                break;

            case SHORT:
            case BYTE:
                score += 800;
                break;

            case LONG:
            case FLOAT:
            case DOUBLE:
                score += 700;
                break;


            case SHORT_WRAPPER:
            case BYTE_WRAPPER:
                score += 600;
                break;


            case LONG_WRAPPER:
            case FLOAT_WRAPPER:
            case DOUBLE_WRAPPER:
                score += 500;
                break;


            case STRING:
                score += 400;
                try {
                    arg = Integer.valueOf(arg.toString());

                }catch (Exception ex) {
                    score = Integer.MIN_VALUE;
                }
                break;
        }
        return score;
    }


    private int handleFloatArg(int score, Object arg, TypeType instanceType) {
        switch (instanceType) {

            case FLOAT:
                score += 1000;
                break;

            case FLOAT_WRAPPER:
                score += 900;
                break;

            case SHORT:
            case BYTE:
                score += 800;
                break;

            case DOUBLE:
                score += 700;
                break;


            case LONG:
            case INT:
            case SHORT_WRAPPER:
            case BYTE_WRAPPER:
                score += 600;
                break;


            case LONG_WRAPPER:
            case INTEGER_WRAPPER:
            case DOUBLE_WRAPPER:
                score += 500;
                break;


            case STRING:
                score += 400;
                try {
                    arg = Float.valueOf(arg.toString());

                }catch (Exception ex) {
                    score = Integer.MIN_VALUE;
                }
                break;
        }
        return score;
    }


    private int handleDoubleArg(int score, Object arg, TypeType instanceType) {
        switch (instanceType) {

            case DOUBLE:
                score += 1000;
                break;

            case DOUBLE_WRAPPER:
                score += 900;
                break;

            case SHORT:
            case BYTE:
                score += 800;
                break;

            case FLOAT:
                score += 700;
                break;


            case LONG:
            case INT:
            case SHORT_WRAPPER:
            case BYTE_WRAPPER:
                score += 600;
                break;


            case LONG_WRAPPER:
            case FLOAT_WRAPPER:
            case INTEGER_WRAPPER:
                score += 500;
                break;


            case STRING:
                score += 400;
                try {
                    arg = Double.valueOf(arg.toString());
                }catch (Exception ex) {
                    score = Integer.MIN_VALUE;
                }
                break;
        }
        return score;
    }

    @Override
    public Object invoke(Object object, Object... args) {
        return null;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public Class<?> declaringType() {
        return null;
    }

    @Override
    public Class<?> returnType() {
        return null;
    }

    @Override
    public boolean respondsTo(Class<?>... types) {
        return false;
    }

    @Override
    public boolean respondsTo(Object... args) {
        return false;
    }

    @Override
    public Object invokeStatic(Object... args) {
        return null;
    }

    @Override
    public MethodAccess bind(Object instance) {
        return null;
    }

    @Override
    public MethodHandle methodHandle() {
        return null;
    }

    @Override
    public MethodAccess methodAccess() {
        return null;
    }

    @Override
    public Object bound() {
        return null;
    }

    @Override
    public <T> ConstantCallSite invokeReducerLongIntReturnLongMethodHandle(T object) {
        return null;
    }

    @Override
    public Method method() {
        return null;
    }

    @Override
    public int score() {
        return 0;
    }

    @Override
    public List<TypeType> paramTypeEnumList() {
        return Collections.emptyList();
    }


    public Object invokeDynamicObject(final Object object, final Object args) {

        if (args instanceof List) {
            return invokeDynamicList(object, (List)args);
        } else {
            return invokeDynamic(object, args);
        }
    }

    @Override
    public List<List<AnnotationData>> annotationDataForParams() {
        return null;
    }

    public Object invokeDynamicList(final Object object, List<?> args) {

        return invokeDynamic(object, Arry.objectArray(args));
    }


    @Override
    public Class<?>[] parameterTypes() {
        return new Class<?>[0];
    }

    @Override
    public Type[] getGenericParameterTypes() {
        return new Type[0];
    }

    @Override
    public Iterable<AnnotationData> annotationData() {
        return null;
    }

    @Override
    public boolean hasAnnotation(String annotationName) {
        return false;
    }

    @Override
    public AnnotationData annotation(String annotationName) {
        return null;
    }

    @Override
    public int compareTo(MethodAccess o) {
        return 0;
    }
}
