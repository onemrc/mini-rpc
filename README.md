# mini-rpc

基于SpringBoot + Netty 实现的简单版RPC框架。本项目只有RPC框架最基本的一些功能，目的是学习、理解RPC框架的实现原理和工作过程。


## Features：

- 服务端支持异步多线程处理RPC请求。
- 客户端支持异步调用，提供future、callback的能力。
- 支持不同的序列化/反序列化。
- 支持各种JDBC协议数据库的注册中心。


## 总体架构

1. **RPC 框架对外提供的服务**定义在一个接口 RpcAccessPoint 中。

```java
/**
 * RPC框架对外提供的服务接口
 */
public interface RpcAccessPoint extends Closeable{
    /**
     * 客户端获取远程服务的引用
     * @param uri 远程服务地址
     * @param serviceClass 服务的接口类的Class
     * @param <T> 服务接口的类型
     * @return 远程服务引用
     */
    <T> T getRemoteService(URI uri, Class<T> serviceClass);

    /**
     * 服务端注册服务的实现实例
     * @param service 实现实例
     * @param serviceClass 服务的接口类的Class
     * @param <T> 服务接口的类型
     * @return 服务地址
     */
    <T> URI addServiceProvider(T service, Class<T> serviceClass);

    /**
     * 服务端启动RPC框架，监听接口，开始提供远程服务。
     * @return 服务实例，用于程序停止的时候安全关闭服务。
     */
    Closeable startServer() throws Exception;
}
```


2. 一个**注册中心**的接口 NameService。

```java
/**
 * 注册中心
 */
public interface NameService {
    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param uri 服务地址
     */
    void registerService(String serviceName, URI uri) throws IOException;

    /**
     * 查询服务地址
     * @param serviceName 服务名称
     * @return 服务地址
     */
    URI lookupService(String serviceName) throws IOException;
}
```


### 使用方式

以调用以一个helloService为例。

客户端：

```java
// 1.注册中心，查询服务地址
URI uri = nameService.lookupService(serviceName);

// 2.获取远程服务的引用
HelloService helloService = rpcAccessPoint.getRemoteService(uri, HelloService.class);
String response = helloService.hello(name);
logger.info("收到响应: {}.", response);
```

服务端：

```java
// 1.服务端启动RPC框架
rpcAccessPoint.startServer();

// 2. 服务端注册服务的实现实例
URI uri = rpcAccessPoint.addServiceProvider(helloService, HelloService.class);

// 3.注册中心，注册服务
nameService.registerService(serviceName, uri);
```

### 现目结构说明

| **模块**    | **说明**               |
| ----------- | ---------------------- |
| client      | 客户端-使用例子        |
| server      | 服务端-使用例子        |
| rpc-api     | RPC 框架对外提供的接口 |
| rpc-netty   | RPC框架核心实现        |
| nameService | 注册中心               |

