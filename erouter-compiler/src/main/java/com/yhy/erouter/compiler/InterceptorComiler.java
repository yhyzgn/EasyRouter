package com.yhy.erouter.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.yhy.erouter.annotation.Interceptor;
import com.yhy.erouter.common.EConsts;
import com.yhy.erouter.common.TypeExchanger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 10:25
 * version: 1.0.0
 * desc   : 拦截器编译器
 */
@AutoService(Processor.class)
public class InterceptorComiler extends AbstractProcessor {
    // 该编译器所支持的注解
    private static final Set<String> AUTOWIRED_SUPPORTED_TYPES = new HashSet<>();
    private static final ClassName AndroidLog = ClassName.get("android.util", "Log");

    private Filer mFilter;
    private Types mTypeUtils;
    private Elements mEltUtils;
    private TypeExchanger mExchanger;

    // 用来保存拦截器名称及其所注解元素的集合
    private Map<String, Element> mEltMap;

    /**
     * 初始化
     *
     * @param proEnv 编译起环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment proEnv) {
        super.init(proEnv);

        // 从环境中获取
        mFilter = proEnv.getFiler();
        mTypeUtils = proEnv.getTypeUtils();
        mEltUtils = proEnv.getElementUtils();
        mExchanger = new TypeExchanger(mTypeUtils, mEltUtils);

        mEltMap = new HashMap<>();

        // 设置支持的注解
        AUTOWIRED_SUPPORTED_TYPES.clear();
        AUTOWIRED_SUPPORTED_TYPES.add(Interceptor.class.getCanonicalName());
    }

    /**
     * 执行操作
     *
     * @param set      所有注解的元素
     * @param roundEnv 编译环境
     * @return 是否截断执行链
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(set)) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Interceptor.class);
            try {
                // 解析
                parse(elements);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 解析注解
     *
     * @param elements 所有被注解的元素
     * @throws IOException IO异常
     */
    private void parse(Set<? extends Element> elements) throws IOException {
        if (CollectionUtils.isEmpty(elements)) {
            return;
        }

        // 自定义拦截器需要实现的接口
        TypeElement teInter = mEltUtils.getTypeElement(EConsts.INTERCEPTOR_INTERFACE);
        // 拦截器映射器接口
        TypeElement teInterMapper = mEltUtils.getTypeElement(EConsts.INTERCEPTOR_MAPPER);

        Interceptor interceptor;
        String interName;

        // 遍历每个被注解元素，并将其保存到Map集合中
        for (Element elt : elements) {
            interceptor = elt.getAnnotation(Interceptor.class);

            // 检查元素是否实现了拦截器映射器接口
            if (!mTypeUtils.isSubtype(elt.asType(), teInter.asType())) {
                throw new IllegalStateException("The interceptor [" + ((TypeElement) elt).getQualifiedName() + "] must implement interface [" + EConsts.INTERCEPTOR_INTERFACE + "]");
            }

            // 拦截器名称，不设置的情况下默认为元素名称
            interName = StringUtils.isEmpty(interceptor.name()) ? elt.getSimpleName().toString() : interceptor.name();

            if (mEltMap.containsKey(interName)) {
                // 已经存在同名拦截器
                throw new IllegalStateException("It has been already exists same name of interceptor: " + interName);
            }
            mEltMap.put(interName, elt);
        }

        // 创建映射器参数列表
        ParameterizedTypeName paramType = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(teInter))));
        ParameterSpec interParam = ParameterSpec.builder(paramType, EConsts.INTERCEPTOR_MAPPER_LOAD_ARG).build();

        // 创建映射器加载映射关系的方法
        MethodSpec.Builder loadInter = MethodSpec.methodBuilder(EConsts.INTERCEPTOR_MAPPER_LOAD)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Load interceptors\r\n\r\n@param " + EConsts.INTERCEPTOR_MAPPER_LOAD_ARG + " Map of saving interceptors\r\n")
                .addParameter(interParam);

        // 添加映射关系
        if (MapUtils.isNotEmpty(mEltMap)) {
            for (Map.Entry<String, Element> et : mEltMap.entrySet()) {
                loadInter.addStatement(EConsts.INTERCEPTOR_MAPPER_LOAD_ARG + ".put(\"" + et.getKey() + "\", $T.class)", ClassName.get((TypeElement) et.getValue()));
            }
        }

        // 创建映射器类
        TypeSpec clazz = TypeSpec.classBuilder(teInterMapper.getSimpleName() + EConsts.SUFFIX_INTERCEPTOR_CLASS)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(teInterMapper))
                .addJavadoc("Interceptors loader\r\n\r\n@author : " + EConsts.AUTHOR + "\r\n@e-mail : " + EConsts.E_MAIL + "\r\n@github : " + EConsts.GITHUB_URL + "\r\n")
                .addMethod(loadInter.build())
                .build();

        // 创建Java文件
        JavaFile.builder(EConsts.INTERCEPTOR_PACKAGE, clazz).build().writeTo(mFilter);
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

    /**
     * 获取该编译器所支持的Java版本
     *
     * @return 该编译器所支持的Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
