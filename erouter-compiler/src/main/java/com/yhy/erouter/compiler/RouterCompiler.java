package com.yhy.erouter.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.yhy.erouter.annotation.Router;
import com.yhy.erouter.common.EConsts;
import com.yhy.erouter.common.Logger;
import com.yhy.erouter.common.RouterMeta;
import com.yhy.erouter.common.RouterType;
import com.yhy.erouter.utils.EUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
    private static final Set<String> SUPPORTED_TYPES = new HashSet<>();

    private Filer mFilter;
    private Types mTypeUtils;
    private Elements mEltUtils;
    private Logger mLogger;

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
        mLogger = new Logger(proEnv.getMessager());

        mGroupMap = new HashMap<>();

        // 设置支持的注解
        SUPPORTED_TYPES.clear();
        SUPPORTED_TYPES.add(Router.class.getCanonicalName());
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
                // 是Activity
                rMeta = new RouterMeta(router, el, RouterType.ACTIVITY);
            } else if (mTypeUtils.isSubtype(tm, tmFragmentV4) || mTypeUtils.isSubtype(tm, tmFragment)) {
                // 是Fragment
                rMeta = new RouterMeta(router, el, RouterType.FRAGMENT);
            } else if (mTypeUtils.isSubtype(tm, tmService)) {
                // 是Service
                rMeta = new RouterMeta(router, el, RouterType.SERVICE);
            }

            // 排序并分组
            categories(rMeta);
        }

        // 生成分组类
        gemerate();
    }

    /**
     * 路由映射器按分组生成Java类
     */
    private void gemerate() {
        // 配置生成路由映射器中加载路由的方法参数
        ParameterizedTypeName groupMap = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(RouterMeta.class));
        ParameterSpec groupPS = ParameterSpec.builder(groupMap, EConsts.METHOD_LOAD_ARG).build();

        // 分组映射器需要实现的接口
        TypeElement tmGroup = mEltUtils.getTypeElement(EConsts.ROUTER_GROUP_MAPPER);

        String group;
        Set<RouterMeta> metas;
        // 遍历每个分组，并生成相应的映射器
        for (Map.Entry<String, Set<RouterMeta>> et : mGroupMap.entrySet()) {
            group = et.getKey();
            metas = et.getValue();

            // 加载路由的方法
            MethodSpec.Builder loadGroup = MethodSpec.methodBuilder(EConsts.METHOD_LOAD)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("Router loader\r\n\r\n@param " + EConsts.METHOD_LOAD_ARG + " Map of saving router\r\n")
                    .addParameter(groupPS);

            // 遍历每个分组中的所有路由元素，将每个元素映射到加载路由的方法中
            for (RouterMeta meta : metas) {
                loadGroup.addStatement(EConsts.METHOD_LOAD_ARG + ".put($S, $T.build($S, $T.class, $T." + meta.getType() + ", $S))",
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
            TypeSpec groupType = TypeSpec.classBuilder(EConsts.PREFIX_OF_GROUP + EUtils.upCaseFirst(group))
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(tmGroup))
                    .addMethod(loadGroup.build())
                    .addJavadoc("Router mapper\r\n\r\n@author : " + EConsts.AUTHOR + "\r\n@e-mail : " + EConsts.E_MAIL + "\r\n@github : " + EConsts.GITHUB_URL + "\r\n")
                    .build();

            // Java文件 包名固定
            JavaFile groupFile = JavaFile.builder(EConsts.PACKAGE_GROUP, groupType).build();

            // 生成Java文件
            try {
                groupFile.writeTo(mFilter);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                            mLogger.error(npe.getMessage());
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
        return SUPPORTED_TYPES;
    }
}
