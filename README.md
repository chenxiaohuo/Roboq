# Roboq: 简单易用的Android HTTP 客户端工具包!

-----

**Roboq**是一个为**Android**定制的HTTP Client客户端工具包，目的是为Android调用服务器端HTTP API或者REST API提供帮助，也含有简单上传和下载文件功能。

-----

**Roboq**内部使用Android中含有的Apache HTTPClient工具包，同时也附加上了org.apache.httpcomponents-httpmime-4.2中上传文件的功能。Roboq需要

-----

**目录**

* 安装及使用
* 开始！
* 创建请求
* HTTP方法
* 请求URL
* HTTP参数
* HTTP头
* 设置上传数据
* 提交表单数据与上传文件
* 执行请求与获取服务器响应
* 使用服务器响应
* 动态选项
* 预备选项
* Authors
* License
* Changelog

## 安装及使用

获取:

```
git clone https://github.com/gaorx/Roboq.git
```
安装:
然后将Roboq/Roboq/src中的源代码复制进你的Android进行编译即可。

-----

## 开始！

```
import me.code4fun.roboq.*;
import static me.code4fun.roboq.Request.*;

// ...

void helloRoboq() {
	new Request(GET, "http://www.baidu.com").execute(
                SimpleExecutor.instance, 
                new Callback() {
            @Override
            public void onResponse(Request req, Response resp, Exception error) {
                Log.d("Roboq.Response", resp.asText());
            }
    });
}

```

-----

## 创建请求

```
new Request(int method, String url);
new Request(int method, String url, Request.Options options);
new Request(int method, String url, Object… optionsKeyAndValues);
new Request(int method, String url).with(Request.Options options);
new Request(int method, String url).with(Object… optionsKeyAndValues);
new Request(int method, String url)
	.with(String optionKey, Object optionValue)
	.withIf(boolean pred, String optionKey, Object optionValue);
```

其中`method`为HTTP动词，`url`为要请求的URL，`options`为请求所附带的HTTP参数，HTTP头部信息，HTTP POST内容等。

-----


## HTTP方法

HTTP的动词定义在Request类中，分别为`GET`,`POST`,`PUT`,`DELETE`,`HEAD`,`TRACE`,`OPTIONS`

-----

## 请求URL

完整的URL为"http://"或者"https://"开头的URL，例如：

```
http://api.yourhost.com/call/api/name
```

URL中可以包含占位符，然后使用options中的值代替，例如：

```
new Request(GET, http://api.yourhost.com/users/${id})
	.with("id$", "10001");
```

在调用这个API时，实际访问的URL是：

```
http://api.yourhost.com/users/10001
```

在这里，`id`被称为PathVar，在使用实际的值进行替换时，必须使用`id$`的形式展开PathVar，`id`与`$`之间可以加入空格，但是`$`之后**不能**再加入空格，所以`id    $`与`id$`是等同的。

-----

## HTTP参数

HTTP参数就是附加在URL后面的k=v形式的参数，在Roboq中使用下面的例子加入参数：

```
new Request(GET, "http://api.yourhost.com/api/name")
	.with("p1=", "v1", "p2=", "v2");
```

在这种情况下最终被访问的URL是：

```
http://api.yourhost.com/api/name?p1=v1&p2=v2
```

在设置HTTP参数时，规则与展开PathVar类似，只是必须使用`=`而不是`$`。

-----

## HTTP头

HTTP头即是HTTP Headers，在Roboq中设置HTTP头的方式为：

```
new Request(GET, "http://www.baidu.com")
	.with("User-Agent    :", "Android app user agent");
```

在设置HTTP参数时，规则与展开PathVar类似，只是必须使用`:`而不是`$`。

-----

## 设置上传数据

在调用HTTP REST API时，需要使用PUT或者POST来修改数据，而数据经常会放置在JSON格式的请求体(Request body)中，下面的例子：

```
User user = new User();
user.setName("Jack");
new Request(POST, "http://api.yourhost.com/users")
	.with("@", Request.textBody(user.toJson(), "applicaton/json"));
```

使用`@`作为名称，`user.toJson()`作为Request body进行POST。

可以作为Request body的类型有：

* `byte[]`：作为数据缓冲，直接送入`byte[]`实例或使用`Request.bytesBody(..)`来创建
* `java.io.InputStream`：内容流，将其读取后上传，必须使用`Request.contentBody(..)`来创建 
* `java.io.File`：读取这个文件的内容进行上传，直接送入`java.io.File`实例或使用`Request.fileBody(..)`来创建
* `其他`：直接送入任意类型实例，或使用`Request.textBody(..)`来创建

-----

## 提交表单数据与上传文件

提交数据的例子：

```
new Request(POST, "http://api.yourhost.com/posts")
	.with("json@", Request.textBody("{\"msg\":\"hello\"}", "applicaton/json"))
	.with("file@", new File("/path/to/file"));
```

在设置表单提交数据，规则与展开PathVar类似，只是必须使用`@`而不是`$`。

可以作为表单提交值的类型有：
* `byte[]`：作为数据缓冲，直接送入`byte[]`实例或使用`Request.bytesBody(..)`来创建
* `java.io.InputStream`：内容流，将其读取后上传，必须使用`Request.contentBody(..)`来创建 
* `java.io.File`：读取这个文件的内容进行上传，直接送入`java.io.File`实例或使用`Request.fileBody(..)`来创建
* `其他`：直接送入任意类型实例，或使用`Request.textBody(..)`来创建


**注意**

Roboq支持`application/x-www-form-urlencoded`和`multipart/form-data`两种类型的POST请求，设置方式与条件是：

* 使用`Request.setMultipart(Boolean multipart)`直接设置，如果`multipart`为true，则使用`multipart/form-data`提交数据，否则使用`application/x-www-form-urlencoded`
* 如果从没调用过`Request.setMultipart(Boolean multipart)`或者`multipart`参数为`null`，那么则由Roboq自动决定使用那种方式提交数据，其策略是：如果提交的值的类型包含`byte[]`、`java.io.InputStream`、`java.io.File`之一，使用`multipart/form-data`，否则使用`application/x-www-form-urlencoded`

一般情况下无需设置`multipart`，Roboq可以正确处理。

-----

## 执行请求与获取服务器响应

在构造与设置完成`Request`实例后，就需要执行请求才能获取服务器响应，执行请求分为：

* 同步请求：执行线程等待服务器回应后才继续执行
* 异步请求：执行线程不等待服务器响应，而是通过回调接口来处理响应，在Android上，这种调用应该是更为常用的方式

同步请求的例子：

```
Response resp = new Request(GET, "http://..").execute();
// 在这里服务器已经回应，回应信息保存在resp变量中
```

异步请求的例子：

```
new Request(GET, "http://..").execute(SimpleExecutor.instance, new Request.Callback() {
	@Override
            public void onResponse(Request req, Response resp, Exception error) {
                // 在这里才获取到服务器请求，在resp变量中
            }	
});
// 这里的服务器还未响应
```
**备注**

* 异步执行方法`Request.execute(Executor executor, Request.Callback cb)`需要一个`Executor`来异步执行，`Executor`的实现可以为最简单的`SimpleExecutor.instance`或者是各种线程池。
* 异步执行方法中，回调接口中德参数`Exception error`，用来描述请求执行时是否出现异常，如果请求正常执行，则`error`为`null`，否则说明有异常产生，一般的处理方式为：

```
public void onResponse(Request req, Response resp, Exception error) {
	if (error != null) {
		// 处理错误
	} else {
		// 正常流程
	}              
}

```

* 可以调用`Request.setTag(Object tag)`来为`Request`实例附加一个自定义对象实例，然后在回调中的`req`参数调用`req.getTag()`获取这个实例。

-----

## 使用服务器响应
todo















