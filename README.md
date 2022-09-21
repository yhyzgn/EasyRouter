# EasyRouter

![router](https://img.shields.io/github/v/release/yhyzgn/EasyRouter?color=brightgreen&label=router&style=flat-square) ![router-compiler](https://img.shields.io/github/v/release/yhyzgn/EasyRouter?color=brightgreen&label=router-compiler&style=flat-square) ![router-annotation](https://img.shields.io/github/v/release/yhyzgn/EasyRouter?color=brightgreen&label=router-annotation&style=flat-square) [![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu) [![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)

> `EasyRouter`是专门针对`Android`开发的简易路由框架，支持路由分组，使用方便，功能全面。主要包含三大模块功能：路由转发、自动注入和路由拦截。

**☆ 注意 ☆**

> **`2.0.0` 开始 `API` 框架改动较大，且不兼容之前版本**
> **`2.0.0` 开始 `API` 框架改动较大，且不兼容之前版本**
> **`2.0.0` 开始 `API` 框架改动较大，且不兼容之前版本**

> **`1.2.0`开始，全面支持`AndroidX`，弃用`support`**

### 功能简介

* 简单路由转发
* 多模块之间路由转发
* 支持`MultiDex`和`InstantRun`
* 参数自动注入
* 路由拦截

### 效果展示

* `Demo`下载：

  ![download](imgs/download_qr.png)

### 基本用法

> 包括路由转发、自动注入和路由拦截。

#### 添加依赖

> 在项目主模块中添加如下依赖

```groovy
defaultConfig {
    //...
    javaCompileOptions {
        annotationProcessorOptions {
            // 多模块路由的基础，需要配置当前模块的名称
            // 如果不配置此项则使用'DefaultModule'作为默认模块名
            // project.name = app
            arguments = [moduleName: project.name]
        }
    }
}

dependencies {
    compile 'com.yhy.router:router:latestVersion'
    annotationProcessor 'com.yhy.router:router-compiler:latestVersion'
}
```

#### 初始化

> 在`Application`中进行初始化

```java
@Override
public void onCreate(){
        super.onCreate();

        // 初始化
        Router.getInstance()
        .init(this)
        // debug方法用来控制日志打印和InstantRun模式开关
        // 只有debug(true)时才打印日志，并启用InstantRun模式
        .debug(BuildConfig.DEBUG)
        .jsonConverter(new EJsonParser(){
final Gson gson=new Gson();
@Override
public<T> T fromJson(String json,Type type){
        return gson.fromJson(json,type);
        }

@Override
public<T> String toJson(T obj){
        return gson.toJson(obj);
        }
        });
        }
```

#### 给需要路由转发的对象注册路由

* 普通路由

  > `Activity`、`Fragment`和`Service`用法都相同

  ```java
  @Router(url = "/activity/normal")
  public class NormalActivity extends BaseActivity {
  }

  @Router(url = "/fragment/normal")
  public class NormalFragment extends BaseFragment {
  }

  @Router(url = "/service/normal")
  public class NormalService extends BaseService {
  }
  ```

* 分组路由

  > 支持路由分组，不指定分组时默认按路由的一级路径分组

  ```java
  @Router(url = "/activity/group", group = "acgp")
  public class GroupActivity extends BaseActivity {
  }
  ```

* 自动注入参数路由

  > 支持参数字段自动注入，包括私有成员
  >
  > 注：如果不手动设置参数名的话，需要保持这里的字段名和设置参数时的参数名相同
  >
  > 使用自动注入参数时不推荐`private`修饰（私有成员通过反射注入）

  ```java
  @Router(url = "/activity/autowried")
  public class AutowiredActivity extends BaseActivity {
      @Autowired
      public String defParam;
      @Autowired("changed")
      public String chgParam;
      @Autowired
      public User objParam;
      @Autowired
      private String privParam;
      @Autowired
      private User privObjParam;
      @Autowired
      public SeriaEntity seriaParam;

      // 不自动注入
      private String param;

      private TextView tvDef;
      private TextView tvChg;
      private TextView tvObj;
      private TextView tvPriv;
      private TextView tvPrivObj;

      @Override
      protected void onCreate(@Nullable Bundle savedInstanceState) {
          // 注入当前对象，触发自动注入操作
          Router.getInstance().inject(this);
          super.onCreate(savedInstanceState);
      }

      @Override
      protected int getLayout() {
          return R.layout.activity_autowired;
      }

      @Override
      protected void initView() {
          tvDef = $(R.id.tv_def);
          tvChg = $(R.id.tv_chg);
          tvObj = $(R.id.tv_obj);
          tvPriv = $(R.id.tv_priv);
          tvPrivObj = $(R.id.tv_priv_obj);
          tvPrivSeria = $(R.id.tv_priv_seria);
      }

      @Override
      protected void initData() {
          tvDef.setText("默认参数：" + defParam);
          tvChg.setText("改变过参数：" + chgParam);
          tvObj.setText("对象参数：" + objParam.toString());
          tvPriv.setText("私有成员参数：" + privParam);
          tvPrivObj.setText("私有对象参数：" + privObjParam.toString());
          tvPrivSeria.setText("Serializable对象参数：" + seriaParam.toString());
      }
  }
  ```

* `Fragment`路由转发

  > 目前不支持直接跳转`Fragment`，路由只是获取到确定的`Fragment`实例，然后需要手动通过事务设置`Fragment`显示
  >
  > 为了方便起见，这里使用了一个`Fragment`简易开源库：[FragmentHelper](https://github.com/yhyzgn/FragmentHelper)

  ```java
  FmHelper helper = new FmHelper.Builder(this, R.id.fl_content).build();
  Fragment fm = Router.getInstance().with(this).to("/fragment/v4/normal").go();
  helper.open(fm);
  ```

#### 路由转发

* 普通路由

  ```java
  Router.getInstance()
      .with(MainActivity.this)
      .to("/activity/normal")
      .go();
  ```

* 分组路由

  ```java
  Router.getInstance()
      .with(MainActivity.this)
      .to("acgp", "/activity/group")
      .go();
  ```

* 带参自动注入转发

  > 注：如果目标中不手动设置参数名的话，需要保持设置的参数名与目标成员字段名相同

  ```java
  Router.getInstance()
      .with(MainActivity.this)
      .to("/activity/autowried")
      .param("defParam", "默认名称参数")
      .param("changed", "修改过名称参数")
      .param("objParam", new User("张三", 25, "男"))
      .param("privParam", "private参数")
      .param("privObjParam", new User("李四", 33, "女"))
      .param("seriaParam", new SeriaEntity("test-test"))
      .go();
  ```

* 带拦截器路由转发

  > 路由添加拦截器，执行顺序与设置顺序一致
  >
  > 拦截器名称与`@Interceptor`注解的名称一致，不设置名称时默认使用拦截器类名
  >
  > 如果中间某个拦截器中断了路由，操作将会被中断

  ```java
  // 多个拦截器按设置顺序执行
  Router.getInstance()
      .with(MainActivity.this)
      .to("/activity/interceptor")
      .interceptor("login")
      .interceptor("LastInterceptor")
      .go();
  ```

* `Activity`切换动画

  ```java
  Router.getInstance()
      .with(MainActivity.this)
      .to("/activity/transition")
      .transition(R.anim.slide_in_right, R.anim.slide_out_right)
      .go();
  ```

* `Activity`共享元素动画

  ```java
  Router.getInstance()
      .with(MainActivity.this)
      .to("/activity/make/anim")
      .animate("tvAnim", view)
      .go();
  ```

* `Service`中打开`Activity`

  > 这里主要说明针对`Intent`的`flag`方法

  ```java
  Router.getInstance()
      .with(this)
      .to("/activity/service")
      .flag(Intent.FLAG_ACTIVITY_NEW_TASK) // Service跳转Activity时最好加上改flag
      .go();
  ```

* 根据`Uri`打开`Activity`

  > 设置`Uri`后，不在根据`Url`跳转，切目标只能是`Activity`。不过设置参数、`flag`等功能还是支持的

  ```java
  Router.getInstance()
      .with(MainActivity.this)
      .uri(Uri.parse("http://www.baidu.com"))
      .action(Intent.ACTION_VIEW)
      .transition(R.anim.slide_in_right, R.anim.slide_out_right)
      .go(mCallback); // 设置回调
  ```

#### 定义拦截器

> 实现`TransferInterceptor`接口，在`execute(Transmitter transmitter)`方法中完成拦截操作
>
> 注：
> < 2.0.0 版本：默认返回`false`，表示不中断路由，如需要中断路由操作返回`true`即可
> >= 2.0.0 版本：与上述相反

```java

@Interceptor(name = "login")// 不指定名称时拦截器将以类名作为名称
public class LoginInterceptor implements TransferInterceptor {

    @Override
    public boolean execute(Transmitter transmitter) {
        // 拦截登录状态
        User user = App.getInstance().getUser();
        if (null == user) {
            Toast.makeText(poster.getContext(), "未登录，先去登录", Toast.LENGTH_SHORT).show();

            // 跳转到登录页面
            Router.getInstance()
                    .with(transmitter)
                    .to("/activity/login")
                    .param("nextRoute", transmitter.getUrl())
                    .go();

            // < 2.0.0 版本：返回 true 表示中断原本的路由
            // >= 2.0.0 版本：返回 false 表示中断原本的路由
            return false;
        }

        Toast.makeText(poster.getContext(), "已经登录，往下执行", Toast.LENGTH_SHORT).show();
        // < 2.0.0 版本：返回 false 表示继续往下执行
        // >= 2.0.0 版本：返回 true 表示继续往下执行
        return true;
    }
}
```

----

```tex
Copyright 2022 yhyzgn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

