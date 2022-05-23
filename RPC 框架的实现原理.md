所有的RPC框架，他们的总体架构和实现原理都是一样的。

以Dubbo为例，客户端和服务端的代码长这样：
```java
// 客户端
@Component
public class HelloClient {

    @Reference // dubbo注解
    private HelloService helloService;

    public String hello() {
      return helloService.hello("World");
    }
}

// 服务端
@Service // dubbo注解
@Component
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
```

可以看到，无论是客户端还是服务端，除了增加了两个注解以外，和实现一个进程内调用没有任何区别。Dubbo 看起来就像把服务端进程中的实现类“映射”到了客户端进程中一样。

<a name="TIhk7"></a>
#### 怎么做到的？

在客户端，业务代码得到的 HelloService 这个接口的实例其实是由 RPC 框架提供的一个代理类的实例。这个代理类有一个专属的名称，叫“桩（Stub）”。

HelloService 的桩，同样要实现 HelloServer 接口，客户端在调用 HelloService 的 hello 方法时，实际上调用的是桩的 hello 方法，在这个桩的 hello 方法里面，它会构造一个请求，这个请求就是一段数据结构，请求中包含两个重要的信息：

1. 请求的服务名，在我们这个例子中，就是 HelloService#hello(String)，也就是说，客户端调用的是 HelloService 的 hello 方法；
1. 请求的所有参数，在我们这个例子中，就只有一个参数 name， 它的值是“World”。然后，它会把这个请求发送给服务端，等待服务的响应。

服务端的 RPC 框架收到这个请求之后，先把请求中的服务名解析出来，然后找到 HelloService 真正的实现类 HelloServiceImpl。找到实现类之后，RPC 框架会调用这个实现类的 hello 方法，使用的参数值就是客户端发送过来的参数值。服务端的 RPC 框架在获得返回结果之后，再将结果封装成响应，返回给客户端。

客户端 RPC 框架的桩收到服务端的响应之后，从响应中解析出返回值，返回给客户端的调用方。这样就完成了一次远程调用。



![image.png](https://cdn.nlark.com/yuque/0/2022/png/1831883/1652354639085-80d69cd9-7cfc-4fec-a954-c5b783abbba5.png#clientId=ub31fc2c3-dd2b-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=ud7a2afda&margin=%5Bobject%20Object%5D&name=image.png&originHeight=1978&originWidth=4266&originalType=url&ratio=1&rotation=0&showTitle=false&size=976838&status=done&style=none&taskId=u7f39b275-73e9-4b3a-8dec-b79ced82327&title=)
