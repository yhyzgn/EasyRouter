package com.yhy.erouter.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.yhy.erouter.annotation.Autowired;
import com.yhy.erouter.common.EConsts;
import com.yhy.erouter.common.Logger;
import com.yhy.erouter.common.TypeExchanger;
import com.yhy.erouter.common.TypeKind;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 11:01
 * version: 1.0.0
 * desc   :
 */
@AutoService(Processor.class)
public class AutowiredCompiler extends AbstractProcessor {
    // 该编译器所支持的注解
    private static final Set<String> AUTOWIRED_SUPPORTED_TYPES = new HashSet<>();
    private static final ClassName AndroidLog = ClassName.get("android.util", "Log");

    private Filer mFilter;
    private Types mTypeUtils;
    private Elements mEltUtils;
    private Logger mLogger;
    private TypeExchanger mExchanger;

    private Map<TypeElement, List<Element>> mTypeMap;

    @Override
    public synchronized void init(ProcessingEnvironment proEnv) {
        super.init(proEnv);

        // 从环境中获取
        mFilter = proEnv.getFiler();
        mTypeUtils = proEnv.getTypeUtils();
        mEltUtils = proEnv.getElementUtils();
        mLogger = new Logger(proEnv.getMessager());
        mExchanger = new TypeExchanger(mTypeUtils, mEltUtils);

        mTypeMap = new HashMap<>();

        // 设置支持的注解
        AUTOWIRED_SUPPORTED_TYPES.clear();
        AUTOWIRED_SUPPORTED_TYPES.add(Autowired.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(set)) {
            // 获取到@Router所注解的元素集合
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Autowired.class);
            // 分类
            categories(elements);
            try {
                // 解析
                parseAutowired(elements);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private void parseAutowired(Set<? extends Element> elements) throws IllegalAccessException, IOException {
        if (CollectionUtils.isEmpty(elements)) {
            return;
        }

        // 获取到路由入口对象
        TypeMirror tmRouter = mEltUtils.getTypeElement(EConsts.E_ROUTER).asType();

        // 定义几种解析的类型
        TypeMirror tmActivity = mEltUtils.getTypeElement(EConsts.ACTIVITY).asType();
        TypeMirror tmFragment = mEltUtils.getTypeElement(EConsts.FRAGMENT).asType();
        TypeMirror tmFragmentV4 = mEltUtils.getTypeElement(EConsts.FRAGMENT_V4).asType();
        TypeMirror tmService = mEltUtils.getTypeElement(EConsts.SERVICE).asType();

        TypeElement teAutowired = mEltUtils.getTypeElement(EConsts.AUTO_WIRED_MAPPER);
        TypeElement teJsonParser = mEltUtils.getTypeElement(EConsts.JSON_PARSER);

        //
        ParameterSpec targetParams = ParameterSpec.builder(TypeName.OBJECT, EConsts.METHOD_AUTOWIRED_INJECT_ARG).build();

        TypeElement type;
        List<Element> fields;
        String qualifiedName;
        String packageName;
        String className;
        FieldSpec jsonParser;
        MethodSpec.Builder inject;
        TypeSpec.Builder clazz;
        Autowired autowired;
        String fieldName;
        String defValue;
        String statement;
        boolean isActivity;
        if (MapUtils.isNotEmpty(mTypeMap)) {
            for (Map.Entry<TypeElement, List<Element>> et : mTypeMap.entrySet()) {
                type = et.getKey();
                fields = et.getValue();

                if (mTypeUtils.isSubtype(type.asType(), tmService)) {
                    // 服务不支持参数自动注入
                    throw new UnsupportedOperationException("Service [" + type.getQualifiedName() + "] unsurpported autowired arguments.");
                }

                qualifiedName = type.getQualifiedName().toString();
                packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
                className = type.getSimpleName() + EConsts.SUFFIX_AUTOWIRED;

                jsonParser = FieldSpec.builder(ClassName.get(teJsonParser.asType()), "mJsonParser")
                        .addModifiers(Modifier.PRIVATE)
                        .addJavadoc("Json parser\r\n")
                        .build();

                // 加载自动注入的方法
                inject = MethodSpec.methodBuilder(EConsts.METHOD_AUTOWIRED_INJECT)
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addJavadoc("Inject parameters\r\n\r\n@param " + EConsts.METHOD_AUTOWIRED_INJECT_ARG + " Current environment\r\n")
                        .addParameter(targetParams);

                clazz = TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(ClassName.get(teAutowired))
                        .addJavadoc("Autowired injecter\r\n\r\n@author : " + EConsts.AUTHOR + "\r\n@e-mail : " + EConsts.E_MAIL + "\r\n@github : " + EConsts.GITHUB_URL + "\r\n");

                inject.addStatement("mJsonParser = $T.getInstance().getJsonParser()", ClassName.get(tmRouter));
                inject.addStatement("$T instance = ($T)target", ClassName.get(type), ClassName.get(type));

                for (Element elt : fields) {
                    autowired = elt.getAnnotation(Autowired.class);
                    fieldName = elt.getSimpleName().toString();

                    if (elt.getModifiers().contains(Modifier.PRIVATE)) {
                        // 如果字段为private，就抛出异常
                        throw new IllegalAccessException("The autowired fields can not be 'private'!!! please check field [" + elt.getSimpleName() + "] in class [" + type.getQualifiedName() + "]");
                    } else {
                        defValue = "instance." + fieldName;
                        statement = "instance." + fieldName + " = instance.";

                        isActivity = false;
                        if (mTypeUtils.isSubtype(type.asType(), tmActivity)) {
                            // 是Activity
                            isActivity = true;
                            statement += "getIntent().";
                        } else if (mTypeUtils.isSubtype(type.asType(), tmFragment) || mTypeUtils.isSubtype(type.asType(), tmFragmentV4)) {
                            statement += "getArguments().";
                        }

                        statement = buildStatement(isActivity, defValue, statement, mExchanger.exchange(elt));
                        if (statement.startsWith(EConsts.JSON_PARSER_NAME)) {
                            // 普通类型转换
                            inject.beginControlFlow("if(null != $T.JSON_PARSER_NAME)", ClassName.get(EConsts.class));
                            inject.addStatement("instance." + fieldName + " = " + statement, StringUtils.isEmpty(autowired.value()) ? elt.getSimpleName().toString() : autowired.value(), ClassName.get(elt.asType()));
                            inject.nextControlFlow("else");
                            inject.addStatement("$T.e(\"" + EConsts.PREFIX_OF_LOGGER + "\", \"If you want to autowired the field '" + fieldName + "' in class '$T', you must set EJsonParser in initialization of ERouter!\")", AndroidLog, ClassName.get(type));
                            inject.endControlFlow();
                        } else {
                            inject.addStatement(statement, StringUtils.isEmpty(autowired.value()) ? elt.getSimpleName().toString() : autowired.value());
                        }
                    }
                }

                clazz.addField(jsonParser);
                clazz.addMethod(inject.build());

                JavaFile.builder(packageName, clazz.build()).build().writeTo(mFilter);
            }
        }
    }

    private String buildStatement(boolean isActivity, String defValue, String statement, int type) {
        if (type == TypeKind.BOOLEAN.ordinal()) {
            statement += (isActivity ? ("getBooleanExtra($S, " + defValue + ")") : ("getBoolean($S)"));
        } else if (type == TypeKind.BYTE.ordinal()) {
            statement += (isActivity ? ("getByteExtra($S, " + defValue + "") : ("getByte($S)"));
        } else if (type == TypeKind.SHORT.ordinal()) {
            statement += (isActivity ? ("getShortExtra($S, " + defValue + ")") : ("getShort($S)"));
        } else if (type == TypeKind.INT.ordinal()) {
            statement += (isActivity ? ("getIntExtra($S, " + defValue + ")") : ("getInt($S)"));
        } else if (type == TypeKind.LONG.ordinal()) {
            statement += (isActivity ? ("getLongExtra($S, " + defValue + ")") : ("getLong($S)"));
        } else if (type == TypeKind.CHAR.ordinal()) {
            statement += (isActivity ? ("getCharExtra($S, " + defValue + ")") : ("getChar($S)"));
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statement += (isActivity ? ("getFloatExtra($S, " + defValue + ")") : ("getFloat($S)"));
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statement += (isActivity ? ("getDoubleExtra($S, " + defValue + ")") : ("getDouble($S)"));
        } else if (type == TypeKind.STRING.ordinal()) {
            statement += (isActivity ? ("getStringExtra($S)") : ("getString($S)"));
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
            statement += (isActivity ? ("getParcelableExtra($S)") : ("getParcelable($S)"));
        } else if (type == TypeKind.OBJECT.ordinal()) {
            statement = EConsts.JSON_PARSER_NAME + ".fromJson(instance." + (isActivity ? "getIntent()." : "getArguments().") + (isActivity ? "getStringExtra($S)" : "getString($S)") + ", $T.class)";
        }

        return statement;
    }

    private void categories(Set<? extends Element> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return;
        }

        TypeElement eltType;
        List<Element> temp;
        for (Element elt : elements) {
            eltType = (TypeElement) elt.getEnclosingElement();

            if (mTypeMap.containsKey(eltType)) {
                mTypeMap.get(eltType).add(elt);
            } else {
                temp = new ArrayList<>();
                temp.add(elt);
                mTypeMap.put(eltType, temp);
            }
        }
    }

    /**
     * 获取该编译器所支持的注解类
     *
     * @return 该编译器所支持的注解类
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return AUTOWIRED_SUPPORTED_TYPES;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
