package com.yhy.router.compiler.base;

import com.yhy.router.common.Constant;
import com.yhy.router.utils.EUtils;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;

/**
 * Created on 2023-05-12 10:45
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class BaseEasyRouterProcessor extends AbstractProcessor {
    private static final Set<String> SUPPORTED_OPTIONS = new HashSet<>();

    protected String mModuleName;

    protected void initOptions() {
        SUPPORTED_OPTIONS.clear();
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            if (options.containsKey(Constant.MODULE_NAME)) {
                mModuleName = options.get(Constant.MODULE_NAME);
            }
            if (options.containsKey(Constant.ENABLE_INCREMENTAL) && Objects.equals(options.get(Constant.ENABLE_INCREMENTAL), "true")) {
                // ROUTER_SUPPORTED_OPTIONS.add("org.gradle.annotation.processing.isolating")
                SUPPORTED_OPTIONS.add("org.gradle.annotation.processing.aggregating");
            }
        }
        if (StringUtils.isEmpty(mModuleName)) {
            mModuleName = Constant.DEF_MODULE_NAME;
        } else {
            mModuleName = EUtils.upCaseFirst(EUtils.line2Hump(mModuleName));
        }
    }

    @Override
    public Set<String> getSupportedOptions() {
        return SUPPORTED_OPTIONS;
    }
}
