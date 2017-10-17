package com.yhy.erouter.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
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
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private static final Set<String> SUPPORTED_TYPES = new HashSet<>();
    private Filer mFilter;
    private Types mTypeUtils;
    private Elements mEltUtils;
    private Logger mLogger;

    private Map<String, Set<RouterMeta>> mRouterMap;

    @Override
    public synchronized void init(ProcessingEnvironment proEnv) {
        super.init(proEnv);

        mFilter = proEnv.getFiler();
        mTypeUtils = proEnv.getTypeUtils();
        mEltUtils = proEnv.getElementUtils();
        mLogger = new Logger(proEnv.getMessager());

        mRouterMap = new HashMap<>();

        SUPPORTED_TYPES.clear();
        SUPPORTED_TYPES.add(Router.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(set)) {
            Set<? extends Element> eltRouter = roundEnv.getElementsAnnotatedWith(Router.class);
            parseRouter(eltRouter);
            return true;
        }
        return false;
    }

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

        parse();
    }

    private void parse() {
        ParameterizedTypeName groupMap = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(RouterMeta.class));
        ParameterSpec groupPS = ParameterSpec.builder(groupMap, "metaMap").build();

        TypeElement tmGroup = mEltUtils.getTypeElement(EConsts.ROUTER_GROUP_INTERFACE);

        String group;
        Set<RouterMeta> metas;
        for (Map.Entry<String, Set<RouterMeta>> et : mRouterMap.entrySet()) {
            group = et.getKey();
            metas = et.getValue();

            MethodSpec.Builder loadGroup = MethodSpec.methodBuilder(EConsts.METHOD_LOAD)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(groupPS);

            for (RouterMeta meta : metas) {
                loadGroup.addStatement("metaMap.put($S, $T.build($S, $T.class, $T." + meta.getType() + ", $S))",
                        meta.getUrl(),
                        ClassName.get(RouterMeta.class),
                        meta.getUrl().toLowerCase(),
                        ClassName.get((TypeElement) meta.getElement()),
                        ClassName.get(RouterType.class),
                        meta.getGroup().toLowerCase()
                );
            }

            TypeSpec groupType = TypeSpec.classBuilder(EConsts.PREFIX_OF_GROUP + EUtils.upCaseFirst(group))
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(tmGroup))
                    .addMethod(loadGroup.build())
                    .build();

            JavaFile groupFile = JavaFile.builder(EConsts.PACKAGE_GROUP, groupType).build();

            try {
                groupFile.writeTo(mFilter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void categories(RouterMeta meta) {
        if (varifyMeta(meta)) {
            Set<RouterMeta> metas = mRouterMap.get(meta.getGroup());
            if (null == metas) {
                metas = new TreeSet<>(new Comparator<RouterMeta>() {
                    @Override
                    public int compare(RouterMeta r1, RouterMeta r2) {
                        try {
                            return r1.getUrl().compareTo(r2.getUrl());
                        } catch (NullPointerException npe) {
                            mLogger.error(npe.getMessage());
                            return 0;
                        }
                    }
                });
                metas.add(meta);
                mRouterMap.put(meta.getGroup(), metas);
            } else {
                metas.add(meta);
            }
        }
    }

    private boolean varifyMeta(RouterMeta meta) {
        if (null == meta) {
            return false;
        }
        String url = meta.getUrl();
        if (StringUtils.isEmpty(url) || !url.startsWith("/")) {
            return false;
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return SUPPORTED_TYPES;
    }
}
