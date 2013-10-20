# Roboq: 简单易用的Android HTTP 客户端工具包!

**Roboq**是一个为**Android**定制的HTTP Client客户端工具包，目的是为Android调用服务器端HTTP API(包含REST API)提供帮助，也含有简单上传和下载文件功能。

<sub>**Roboq**内部使用Android中含有的Apache HTTPClient工具包，同时也附加上了org.apache.httpcomponents-httpmime-4.2中上传文件的功能。Roboq需要Android 2.2+</sub>

-----

**目录**

* [安装及使用](#安装及使用)
* [开始](#开始)
* [创建请求](#创建请求)
* [HTTP方法](#http方法)
* [请求URL](#请求URL)
* [HTTP参数](#http参数)
* [HTTP头](#http头)
* [设置上传数据](#设置上传数据)
* [提交表单数据与上传文件](#提交表单数据与上传文件)
* [动态选项](#动态选项)
* [设置请求](#设置请求)
* [执行请求与获取服务器响应](#执行请求与获取服务器响应)
* [使用服务器响应](#使用服务器响应)
* [预备选项](#预备选项)
* [深度定制](#深度定制)
* [异步回调](#异步回调)
* [例子](#例子)
* [Authors](#authors)
* [License](#license)
* [Changelog](#changelog)


## 安装及使用

获取:

```
git clone https://github.com/gaorx/Roboq.git
```
安装:
然后将Roboq/Roboq/src中的源代码复制进你的Android进行编译即可。


## 开始

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


## HTTP方法

HTTP的动词定义在Request类中，分别为`GET`,`POST`,`PUT`,`DELETE`,`HEAD`,`TRACE`,`OPTIONS`


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


## HTTP头

HTTP头即是HTTP Headers，在Roboq中设置HTTP头的方式为：

```
new Request(GET, "http://www.baidu.com")
	.with("User-Agent    :", "Android app user agent");
```

在设置HTTP参数时，规则与展开PathVar类似，只是必须使用`:`而不是`$`。


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
* `InputStream`：内容流，将其读取后上传，必须使用`Request.contentBody(..)`来创建 
* `File`：读取这个文件的内容进行上传，直接送入`File`实例或使用`Request.fileBody(..)`来创建
* `其他`：直接送入任意类型实例，或使用`Request.textBody(..)`来创建


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
* `InputStream`：内容流，将其读取后上传，必须使用`Request.contentBody(..)`来创建 
* `File`：读取这个文件的内容进行上传，直接送入`File`实例或使用`Request.fileBody(..)`来创建
* `其他`：直接送入任意类型实例，或使用`Request.textBody(..)`来创建


**注意**

Roboq支持`application/x-www-form-urlencoded`和`multipart/form-data`两种类型的POST请求，设置方式与条件是：

* 使用`Request.setMultipart(Boolean multipart)`直接设置，如果`multipart`为true，则使用`multipart/form-data`提交数据，否则使用`application/x-www-form-urlencoded`
* 如果从没调用过`Request.setMultipart(Boolean multipart)`或者`multipart`参数为`null`，那么则由Roboq自动决定使用那种方式提交数据，其策略是：如果提交的值的类型包含`byte[]`、`InputStream`、`File`之一，使用`multipart/form-data`，否则使用`application/x-www-form-urlencoded`

一般情况下无需设置`multipart`，Roboq可以正确处理。

**注意**

如果指定了直接设置上传数据，则不能使用`application/x-www-form-urlencoded`与`multipart/form-data`进行上传，例如：

```
new Request(POST, "http://..")
   .with("@", "text") // ok，直接设置上传数据 
   .with("file@", new File(..)) // 错误，"file@"与"@"冲突  
``` 

## 动态选项

有时，有些选项是无法直接指定值，因为它们依赖于其他选项的值，例如某些API调用时候的`sign`等选项，它的值以来与所有其他选项的名称或值，在Roboq中可以使用动态选项的功能进行处理。动态选项需要继承自`Request.Modifier`类，使用方法如下：

```
new Request(GET, "http://api.yourhost.com/call/api/name")
	.with("p1=", "v1").with("p2=", "v2")
	.setModifier(new Request.Modifier() {
		@Override
		public Options modifyOptions(Options options) {
       	Map<String, Object> params = options.getParams();
       	// params 为 {"p1"->"v1", "p2"->"v2"}
       	String sign = getSign(params); // 然后根据params内容计算sign
       	options.put("sign=", sign); // 再把sign也加入选项中
       	return options;
      }
	});
	
```

**备注**

实际上，`Request.Modifier`还可以修改被请求的method和URL，只要覆盖`int modifyMethod(int method)`和`String modifyUrl(String url)`即可。


## 设置请求

除了method、URL、options外，Request还可以进行一些其他设置：

```

// 在HTTP参数中的字符串进行编码的字符集，默认为UTF-8
Request.setParamsEncoding(String paramsEncoding); 

// 在提交表单时，字段中的文本编码的字符集，默认为UTF-8
Request.setFieldsEncoding(String fieldsEncoding);

// 尝试请求数量，默认为3次
Request.setRetryCount(Integer retryCount);

// 设置是否接受重定向，如果为false服务器返回3xx状态码时也不进行重定向
Request.setRedirect(Boolean redirect);

// 设置连接超时（毫秒）
Request.setConnectionTimeout(Integer timeout);

// 设置SOCKET超时（毫秒）
Request.setSoTimeout(Integer timeout);
```


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



## 使用服务器响应

下面的例子说明如何获取HTTP状态码和HTTP头：

```
Response.httpResponse; // 获取HttpClient的HttpResponse实例

int Response.statusCode(); // 200
String Response.contentType(); // text/html;charset=UTF-8
long Response.contentLength(); // 102400 bytes
Map<String, String> Response.headers(); // "Server" -> "nginx"
String header(String name); // 获取First Header，如不存在此header，返回null 
String header(String name, String def); // 获取First Header，如不存在此header，返回def
boolean hasHeader(String name); // 是否包含指定的header 
```

**注意**：

`Response.header`中的操作使用Header只使用First Header，如果存在同名的多个Header，可以直接使用`Response.httpResponse`中方法进行获取。


下面的例子说明如何获取HTTP Body：

```
Response resp = ..;
InputStream in = resp.content(); // 获取stream形式的content
byte[] bytes = resp.asBytes(); // 获取缓冲形式的content
String text = resp.asText("UTF-8"); // 将文本形式的content
String text = resp.asText(); // 试图从contentType()中解析出charset，然后使用其作为字符集转换为文本，如果没找到charset，则使用UTF-8进行转换
Object json = resp.asJson(); // 将text()的返回值解析成JSON格式，如果不是合法JSON格式则会抛出异常
JSONObject jsonObj = resp.asJsonObject(); // 将asJson()的返回值转换成JSONObject，如果不是JSONObject则抛出异常
JSONArray jsonArr = resp.asJsonArray(); // 将asJson()的返回值转换成JSONArray，如果不是JSONArray则抛出异常
resp.write(OutputStream out); // 将content内容转写到out中
resp.writeFile(File f[, boolean append]); // 将content内容存储到文件中，可以选择是否追加

// 另外，还可以通过转换器将其转换为特定类型
public <R> R Response.as(Response.ResultMapper<R> rm);

// 使用起来的例子，其中responseMapper为一个转换器Response.ResultMapper的实现
User user = resp.as(User.responseMapper); 
```

**注意**

对于`Response.asText()`等方法，执行过一次后，第二次则会抛出异常，因为content数据已经被读取完毕

```
String text = resp.asText(); // OK
String text1 = resp.asText(); // 抛出数据已经读取完成的异常
```

## 预备选项

在实际的应用中，很多选项是每个HTTP请求都必须附带的，例如HTTP头`User-Agent`和关于身份认证的信息等，如果在每个Request中都设置这些难免有些繁琐，这种情况下，可以使用Roboq的预备选项功能。例如：

```
// 定义预备选项
Request.Prepared prepared = new Request.Prepared("http://api.yourhost.com")
	.with("User-Agent:", "Android client");
	
new Request(prepared, GET, "/v1/users/${id}") // 使用预备选项
	.with("id@", "10001")
	.execute(..);
```

在定义好预备选项后，作为第一个参数送入`Request`的构造函数中，这样创建的`Request`实例就共享这个预备选项。`Request.Prepared`中定义了大部分`Request`中所能设置的状态，例如请求URL与各种选项，在`Request`进行对服务器请求时，会将自身的各种状态与预备选项中的装备进行**合并**，所以会在预备选项中放置一些所有请求都需要的选项和设置。

**备注** 如果`Request`与`Request.Prepared`中都设置了相同的选项，那么`Request`的设置会覆盖掉`Request.Prepared`的设置，这个规则叫做**`Reqeust`优先规则**。

下面说明了一些`Reqeust`与`Request.Prepared`合并的规则：

* URL：会将`Request.Prepared`中的URL与`Request`中的URL进行字符串连接，例如`Request.Prepared`的URL为`http://api.yourhost.com`，而`Request`的URL为`/api/name`，则最终合并为`http://api.yourhost.com/api/name`
* Options： 包括HTTP头，HTTP参数，HTTP表单字段，URL的PathVar，都会按照`Request`优先的规则进行合并
* 设置：包括`paramsEncoding`，`connectTimout`等，都会按照`Request`优先的规则进行合并
* 动态选项：`modifier`的合并规则为，先调用`Request.Prepared`中的`modifier`，然后把修改后的值再调用`Request`中的`modifier`。


## 深度定制

`Request`被设计成可以被继承的，其中有些`protected`的功能可以被重载，完成一些特殊用途的功能，包括：

* `Response createResponse(HttpResponse httpResp)`，可以创建出`Response`子类的实例
* `HttpClient createHttpClient(int method, String url, Options opts)`可用来定制生成`HttpClient`
* `String o2s(Object obj, String def)`可以定制一些`Object`转换成`String`的规则，例如将`Boolean`转成`1`或`0`，而不是`true`或`false`
* `OutputStream decorateBodyOutput(OutputStream out)`可以对上传body的OutputStream做装饰，用于统计上传数据量或显示进度等功能

同样，`Response`也被设计成可以被继承的，它包含的可被重载的功能为：

* `InputStream decorateContent(InputStream content)`可以对读取content做装饰，用来完成显示下载进度等功能


## 异步回调

`Request.Callback`的用法在


## 例子

TODO


## Authors

```
[{
	"name": "高 荣欣",
	"email": "rongxin.gao@gmail.com",
	"location": "北京"
}]
```
欢迎联系 **^_^**


## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


## Changelog

TODO


















