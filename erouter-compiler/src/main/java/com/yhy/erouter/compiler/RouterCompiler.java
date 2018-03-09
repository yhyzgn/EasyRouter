package com.yhy.erouter.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.yhy.erouter.annotation.Autowired;
import com.yhy.erouter.annotation.Router;
import com.yhy.erouter.common.EConsts;
import com.yhy.erouter.common.RouterMeta;
import com.yhy.erouter.common.RouterType;
import com.yhy.erouter.common.TypeExchanger;
import com.yhy.erouter.utils.EUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
 * time   : 2017-10-17 10:43
 * version: 1.0.0
 * desc   : 路由编译器
 */
@AutoService(Processor.class)
public class RouterCompiler extends AbstractProcessor {
    // 该编译器所支持的注解
    private static final Set<String> ROUTER_SUPPORTED_TYPES = new HashSet<>();

    private Filer mFilter;
    private Types mTypeUtils;
    private Elements mEltUtils;
    private String mModuleName;
    private TypeExchanger mExchanger;

    // 存储路由数据按分组分类后的集合
    private Map<String, Set<RouterMeta>> mGroupMap;

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

        // 获取模块名称
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options) && options.containsKey("moduleName")) {
            mModuleName = options.get("moduleName");
        }
        if (StringUtils.isEmpty(mModuleName)) {
            mModuleName = EConsts.DEF_MODULE_NAME;
        } else {
            mModuleName = EUtils.upCaseFirst(EUtils.line2Hump(mModuleName));
        }

        mExchanger = new TypeExchanger(mTypeUtils, mEltUtils);

        mGroupMap = new HashMap<>();

        // 设置支持的注解
        ROUTER_SUPPORTED_TYPES.clear();
        ROUTER_SUPPORTED_TYPES.add(Router.class.getCanonicalName());
        ROUTER_SUPPORTED_TYPES.add(Autowired.class.getCanonicalName());
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
            // 获取到@Router所注解的元素集合
            Set<? extends Element> eltRouter = roundEnv.getElementsAnnotatedWith(Router.class);
            // 解析路由
            parseRouter(eltRouter);
            return true;
        }
        return false;
    }

    /**
     * 解析路由
     *
     * @param elements 路由注解的元素集合
     */
    private void parseRouter(Set<? extends Element> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return;
        }

        // 定义几种解析的类型
        TypeMirror tmActivity = mEltUtils.getTypeElement(EConsts.ACTIVITY).asType();
        TypeMirror tmFragment = mEltUtils.getTypeElement(EConsts.FRAGMENT).asType();
        TypeMirror tmFragmentV4 = mEltUtils.getTypeElement(EConsts.FRAGMENT_V4).asType();
        TypeMirror tmService = mEltUtils.getTypeElement(EConsts.SERVICE).asType();

        TypeMirror tm;
        Router router;
        RouterMeta rMeta = null;
        // 遍历所有元素，一一解析
        for (Element el : elements) {
            tm = el.asType();
            router = el.getAnnotation(Router.class);

            if (mTypeUtils.isSubtype(tm, tmActivity)) {
                // 是Activity，支持自动注入参数
                rMeta = new RouterMeta(router, el, RouterType.ACTIVITY, genParamsType(el));
            } else if (mTypeUtils.isSubtype(tm, tmFragmentV4)) {
                // 是Fragment，支持自动注入参数
                rMeta = new RouterMeta(router, el, RouterType.FRAGMENT_V4, genParamsType(el));
            } else if (mTypeUtils.isSubtype(tm, tmFragment)) {
                // 是Fragment，支持自动注入参数
                rMeta = new RouterMeta(router, el, RouterType.FRAGMENT, genParamsType(el));
            } else if (mTypeUtils.isSubtype(tm, tmService)) {
                // 是Service， 不支持自动注入参数
                rMeta = new RouterMeta(router, el, RouterType.SERVICE, null);
            }

            // 排序并分组
            categories(rMeta);
        }

        // 生成分组类
        try {
            generate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Integer> genParamsType(Element el) {
        Map<String, Integer> paramsType = new HashMap<>();
        for (Element field : el.getEnclosedElements()) {
            if (field.getKind().isField() && null != field.getAnnotation(Autowired.class)) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                paramsType.put(StringUtils.isEmpty(autowired.value()) ? field.getSimpleName().toString() : autowired.value(), mExchanger.exchange(field));
            }
        }
        return paramsType;
    }

    /**
     * 路由映射器按分组生成Java类
     */
    private void generate() throws IOException {
        // 配置生成路由映射器中加载路由的方法参数
        ParameterizedTypeName groupMap = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(RouterMeta.class));
        ParameterSpec groupParams = ParameterSpec.builder(groupMap, EConsts.METHOD_ROUTER_LOAD_ARG).build();

        // 分组映射器需要实现的接口
        TypeElement teGroup = mEltUtils.getTypeElement(EConsts.ROUTER_GROUP_MAPPER);

        String group;
        Set<RouterMeta> metas;
        MethodSpec.Builder loadGroup;
        TypeSpec groupType;
        JavaFile groupFile;
        StringBuffer sb;
        Map<String, Integer> paramsType;
        String paramsBody;

        // 遍历每个分组，并生成相应的映射器
        for (Map.Entry<String, Set<RouterMeta>> et : mGroupMap.entrySet()) {
            group = et.getKey();
            metas = et.getValue();

            // 加载路由的方法
            loadGroup = MethodSpec.methodBuilder(EConsts.METHOD_ROUTER_LOAD)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("Router loader\r\n\r\n@param " + EConsts.METHOD_ROUTER_LOAD_ARG + " Map of saving router\r\n")
                    .addParameter(groupParams);

            // 遍历每个分组中的所有路由元素，将每个元素映射到加载路由的方法中
            for (RouterMeta meta : metas) {
                sb = new StringBuffer();
                paramsType = meta.getParamsType();
                if (MapUtils.isNotEmpty(paramsType)) {
                    for (Map.Entry<String, Integer> param : paramsType.entrySet()) {
                        sb.append("put(\"").append(param.getKey()).append("\", ").append(param.getValue()).append("); ");
                    }
                }
                paramsBody = sb.toString();

                // 这里生成自动注入参数集合的参数不参与格式化，因为有些地方不需要注入，此时该参数为null，无法按HashMap进行格式化，所以这里直接写成java.util.HashMap即可
                loadGroup.addStatement(EConsts.METHOD_ROUTER_LOAD_ARG + ".put($S, $T.build($S, $T.class, $T." + meta.getType() + ", $S, " + (StringUtils.isEmpty(paramsBody) ? null : "new java.util.HashMap(){{" + paramsBody + "}}") + "))",
                        meta.getUrl(),
                        ClassName.get(RouterMeta.class),
                        meta.getUrl().toLowerCase(),
                        ClassName.get((TypeElement) meta.getElement()),
                        ClassName.get(RouterType.class),
                        meta.getGroup().toLowerCase()
                );
            }

            // 映射器类
            // 类名为 固定前缀 + 首字母大写的分组名
            groupType = TypeSpec.classBuilder(EConsts.PREFIX_OF_GROUP + EUtils.upCaseFirst(group) + EConsts.SEPARATOR + mModuleName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(teGroup))
                    .addMethod(loadGroup.build())
                    .addJavadoc("Router mapper\r\n\r\n@author : " + EConsts.AUTHOR + "\r\n@e-mail : " + EConsts.E_MAIL + "\r\n@github : " + EConsts.GITHUB_URL + "\r\n")
                    .build();

            // Java文件 包名固定
            groupFile = JavaFile.builder(EConsts.GROUP_PACKAGE, groupType).build();

            // 生成Java文件
            groupFile.writeTo(mFilter);
        }
    }

    /**
     * 按分组将路由添加到集合中，并按url排序
     *
     * @param meta 路由数据
     */
    private void categories(RouterMeta meta) {
        if (varifyMeta(meta)) {
            Set<RouterMeta> metas = mGroupMap.get(meta.getGroup());
            if (null == metas) {
                metas = new TreeSet<>(new Comparator<RouterMeta>() {
                    @Override
                    public int compare(RouterMeta r1, RouterMeta r2) {
                        // 将每个分组按url排序
                        try {
                            return r1.getUrl().compareTo(r2.getUrl());
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                            return 0;
                        }
                    }
                });
                metas.add(meta);
                mGroupMap.put(meta.getGroup(), metas);
            } else {
                metas.add(meta);
            }
        }
    }

    /**
     * 检查路由有效性
     *
     * @param meta 路由数据
     * @return 是否有效
     */
    private boolean varifyMeta(RouterMeta meta) {
        if (null == meta) {
            return false;
        }
        String url = meta.getUrl();
        if (StringUtils.isEmpty(url) || !url.startsWith("/")) {
            throw new IllegalStateException("The url of " + meta.getElement().getSimpleName() + " can not be empty and must start with '/'.");
        }
        return true;
    }

    /**
     * 获取该编译器所支持的注解类
     *
     * @return 该编译器所支持的注解类
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ROUTER_SUPPORTED_TYPES;
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
