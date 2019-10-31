# Asynchronous：线程调度执行器

## 以建造者模式来快捷的切换主线程和子线程，能够在两种不同的线程中来回切换数据发送数据处理数据的调度器模块。

### 一、依赖

![](https://jitpack.io/v/YuQianhao/Asynchronous.svg)

#### 1、Gradle

```java
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```java
dependencies {
	    implementation 'com.github.YuQianhao:Asynchronous:1.0.2'
}
```

#### 2、Maven

```java
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```

```java
<dependency>
	<groupId>com.github.YuQianhao</groupId>
	<artifactId>Asynchronous</artifactId>
	<version>1.0.2</version>
</dependency>
```

### 二、适应场景

####         适用于切换线程频繁以及需要执行多个异步任务并在主线程等待的模块。

### 三、使用方式

#### 1、获取一个调度器的实例

```java
public class Test{
    public static final void main(String[] args){
        Async async=Async.create();
    } 
}
```

使用**Async**类的**create**静态方法获取一个该类的实例。

#### 2、使用调度器在主线程中创建一个List并在子线程中打印

在这里展示的是使用Async发送数据和处理数据以及切换线程的例子。

```java
public class Test{
    public static final void main(String[] args){
        List<String> testStringList=new ArrayList<>();
        testStringList.add("Hello");
        testStringList.add(" ");
        testStringList.add("World!");
        Async.create()
            .ui()
            .sendList(testStringList)
            .io()
            .handleList(new Async.HandleList<String>(){
                @Override
                    public void list(List<String> list) {
                        for(String str:list){
                            System.out.print(str);
                        }
                    }
            });
    }
}
```

控制台输出：

```ja
>:Hello World!
```

我们来看这个Async做了什么：

1、调用**Async.create()**创建了一个Async实例。

2、调用Async实例的**ui()**方法将当前工作线程切换到主线程中，意味着**接下来的调用和操作都发生在主线程中**。

3、调用Async实例的**sendList()**方法将实现创建好的List实例发送到Async缓冲区中，方法的声明如下：

```java
public <_Tx> Async sendList(List<_Tx> txList);
public <_Tx> Async sendSet(Set<_Tx> txSet);
public <_Tx,_Ty> Async sendMap(Map<_Tx,_Ty> txMap);

public Async clearSet();
public Async clearList();
public Async clearMap();
```

在Async内部维护了三个数据结构对象用来作为缓冲对象：

* List	：存放List组织形式的数据

* Set         ：存放Set组织形式的数据

* Map       ：存放Map组织形式的数据
* Object   ：存放一个任意对象

可以调用**sendList()、sendSet()、sendMap()**方法将对应的实例对象发送到缓冲区中，当处理完之后调用对应的**clearList()、clearSet()、clearMap()**将缓冲区的数据释放，但这不是必须的，**因为在你下次send数据到缓冲区的时候，上次send的数据将会被覆盖**。

* 特殊的一个关于数据填充的方法：

```java
public <_Tx> Async push(_Tx o);
public <_Tx> Async send(_Tx o);
```

push方法将一个任意类型的对象发送到**List**的内存缓冲区中，**这个List和sendList使用的List是同一个List对象**，意味着你调用push发送的数据将会填充在List的后面。例如：

```java
Async.create()
            .ui()
            .sendList(testStringList)
    		.push("Test");
            .io()
            .handleList(new Async.HandleList<String>(){
                @Override
                    public void list(List<String> list) {
                        for(String str:list){
                            System.out.print(str);
                        }
                    }
            });
```

则控制台的输出如下：

```java
>:Hello World!Test
```

这就说明了如果要单独使用push填充数据的时候，**如果你确保当前Async环境下的List是空的，则调用push发送数据，如果不能保证长度为0，就可以调用clearList()来清理这个List的数据之后再调用push填充数据**。

send方法将一个任意对象填充道Object这个单一对象缓冲区，例如：

```java
Async.create()
    .ui()
    .send("Hello World!")
    .io()
    .handleObject(new Async.HandleObject<String>() {
                    @Override
                    public void obj(String obj) {
                        System.out.pritnln(obj);
                    }
                });
```

控制台输出：

```java
>:Hello World!
```

4、调用了Async实例的**io()**方法将工作线程切换到子线程中，意味着**接下来的调用和操作都发生在子线程中**。

5、调用了Async实例的**handleList()**方法来处理刚刚使用**sendList**发送的数据，使用send/push发送的数据使用handle开头的方法去处理，Async提供了4个handle处理数据的方法：

```java
public <_Tx> Async handleObject(HandleObject<_Tx> handleObject);
public <_Tx> Async handleList(HandleList<_Tx> handleList);
public <_Tx> Async handleSet(HandleSet<_Tx> handleSet)
public <_Tx,_Ty> Async handleMap(HandleMap<_Tx,_Ty> handleMap)
```

Async使用这四个方法来处理刚刚使用send和push发送的数据，其中四个方法对应的参数是一个接口类，**每一个接口类都包含一个或者两个泛型类型参数**，这个泛型类型参数用来表示要将缓冲区中对应的数据转换成的数据类型，例如：

```java
sendObject(Arrays.of(1,2,3,4,5,6));
//这里是发送了一个int[]数组类型，当在使用handleObject方法处理的时候应该将泛型参数类型转换成int[]：
handleObject(new Async.HandleObject<Integer[]>() {
                    @Override
                    public void obj(Integer[] intvalues) {
                        System.out.pritnln(Arrays.toString(intvalues));
                    }
                });
```

控制台打印如下：

```java
[1,2,3,4,5,6]
```

当调用handle方法的时候会立即将缓冲区的数据发送过来处理，如果没有使用send或者push发送数据可能会传过来null。

#### 3、创建任务事件以及获取任务结果

在这个例子中将展示Async处理任务以及捕获任务结果的方法

```java
public class Test{
    public static final void main(String[] args){
        Async.create()
            .call(ExecutorThread.IO, new AbsExecutor<String,String>() {
                    @Override
                    public String run(String... params) {
                        StringBuilder stringBuilder=new StringBuilder();
                        for(String tmp:params){
                            stringBuilder.append(tmp);
                        }
                        return stringBuilder.toString();
                    }

                    @Override
                    public int id() {
                        return 1;
                    }
                },"Hello"," ","World!")
                .ui()
                .capture(1, new IExecutorComplete<String>() {
                    @Override
                    public void onComplete(ExectuorValue exectuorValue) {
                        String value=exectuorValue.getRetValue();
                        System.out.println(value);
                    }
                });
    }
}
```

控制台输出：

```java
>:Hello World！
```

1、创建一个任务的方法定义如下：

```java
public <_Tx> Async call(ExecutorThread thread, IExecutor executor, _Tx ...params);
```

参数解析：

* ExecutorThread thread：	要在哪个线程环境下执行这个任务

* IExecutor executor：		执行任务执行的过程

* _Tx ...params：			  要向这个执行任务过程函数传送的参数

①ExecutorThread：这个枚举类用于表示线程环境，定义如下：

```java
public enum ExecutorThread {
    MAIN,
    IO
}
```

顾名思义，MAIN为主线程，IO为子线程。

②IExecutor：执行器的执行过程类，执行器执行的任务过程在这个接口中定义，接口定义如下：

```java
/**
 * 执行器接口,实现该接口可以创建一个执行者,该接口拥有两个泛型类型参数,
 * _Part 执行器需要的参数类型
 * _Rex  执行器的返回结果类型
 *
 */
public interface IExecutor<_Part, _Rex> {

    /**
     * 执行函数
     * @param params
     * @return
     */
    _Rex run(_Part...params);

    /**
     * 这个执行任务的id
     * @return
     */
    int id();

}
```

这个接口类相当于处理任务的核心类，方法run定义了要执行的内容，方法id定义了这个执行器的唯一ID，后期捕获执行结果的时候通过这个ID进行匹配。有些情况不需要捕获执行结果，自然也不需要ID，所以定义了一个类AbsExecutor<_Part, _Rex>类实现了IExecutor接口，这个抽象类只要求重写run方法，而id方法可以不进行重写。

③ Tx ...：变参，意味着可以有多个参数，这个Tx类型是在创建IExecutor<Part, Rex>时候的Part类型，Part类型参数表示的是run方法接收的参数类型，Rex类型参数表示的事run方法返回的数据类型。

例如：

```java
call(
    //在子线程中执行
    ExecutorThread.IO,
    //定义了一个执行器对象，这个执行器的执行过程需要String类型的参数，并且返回List<String>类型的参数
     new AbsExecutor<String,List<String>>() {
                    @Override
                    public List<String> run(String... params) {
                        List<String> stringList=new ArrayList<>();
                        for(String tmp:params){
                            stringList.add(tmp);
                        }
                        return stringList;
                    }

                    @Override
                    public int id() {
                        return 1;
                    }
                },
    //传给run方法三个字符串参数
     "Hello"," ","World!")
```

如果不需要捕获返回结果可以不重写id方法，例如：

```java
call(
    ExecutorThread.IO,
     new AbsExecutor<String,List<String>>() {
                    @Override
                    public List<String> run(String... params) {
                        List<String> stringList=new ArrayList<>();
                        for(String tmp:params){
                            stringList.add(tmp);
                        }
                        return stringList;
                    }
                },
     "Hello"," ","World!")
```

* call有一个重载方法，定义如下：

```java
public <_Tx> Async call(IExecutor executor, _Tx ...params);
```

这个方法相对于上一个方法来说少了一个线程环境，所以默认就在当前Async的线程环境中执行，如果上一个线程环境使用ui()切换则在主线程执行，否则在子线程，例如：

```java
public class Test{
    public static final void main(String[] args){
        Async.create()
            .ui()
            .call(new AbsExecutor<Void,Void>() {
                    @Override
                    public Void run(Void params) {
                        //在主线程中执行
                    }
                })
            .io()
            .call(new AbsExecutor<Void,Void>() {
                    @Override
                    public Void run(Void params) {
                        //在子线程中执行
                    }
                })
            .call(ExecutorThread.MAIN, AbsExecutor<Void,Void>() {
                    @Override
                    public Void run(Void params) {
                        //在主线程中执行，即便上面使用了io切换了子线程，但是
                        //方法参数中明确规定了使用ExecutorThread.MAIN线程
                        //环境，即在主线程中执行。
                    }
                });
    }
}
```

2、捕获执行器的结果的方法定义如下：

```java
public Async capture(ExecutorThread thread, 
                     int id, 
                     IExecutorComplete<_Tx> executorComplete);
```

参数解析：

* ExecutorThread thread：					  线程环境
* int id：								          执行器ID

* IExecutorComplete<_Tx> executorComplete：     执行器结果处理的定义接口

①ExecutorThread ：和call方法一样的线程环境枚举类

②id：执行器的ID，这里代表这个方法要捕获的执行器的id，**执行器的id在IExecutor或者AbsExecutor中重写id()方法设置**，例如：

```java
public static final int _ID=1;
call(new AbsExecutor<Void,Void>() {
             @Override
             public Void run(Void params) {
                 //在子线程中执行
             }
    		
             @Override
             public int id() {
                 return _ID;
             }
         })
capture(ExecutorThread.MAIN,
        _ID, 
        new IExecutorComplete() {
             @Override
             public void onComplete(ExectuorValue exectuorValue) {
             }
         })
```

③IExecutorComplete<_Tx>：执行器结果处理定义的接口类，定义如下：

```java
public interface IExecutorComplete<_Tx> {

    void onComplete(ExectuorValue<_Tx> exectuorValue);

}
```

执行器IExecutor的run()方法执行完毕的结果会传给捕获结果处理接口的onComplete()方法，这个方法接收一个ExectuorValue<_Tx>类型参数，这个参数代表执行器的执行结果，在这个执行结果类中定义的方法如下：

```java
public class ExectuorValue<_Tx> {

    //获取返回结果
    public _Tx getRetValue();

    //重新设置一个结果
    public void setRetValue(_Tx _retValue);

    //获取执行器ID
    public int getId();

    //将结果转换成_RetType类型的数据并返回
    public <_RetType> _RetType cast();

    //释放这个执行结果
    public final void finish();
}
```

其中注意到IExecutorComplete<Tx>接口使用了泛型类型参数，这个泛型类型和call方法中的IExecutor<Part, Rex>的Rex类型一直，例如IExecutor的run方法返回了一个String类型的返回结果，那么捕获这个执行器的结果处理器就需要一个String的类型参数，例如：

```java
IExecutor<String,Number>;
IExecutorComplete<Number>;
```

#### 4、常用案例

1、网络请求

```java
public class Test{
    public static final void main(String[] args){
        Async.create()
                .io()
                .call(new AbsExecutor<String,String>() {
                    @Override
                    public String run(String... params) {
                        return httpRequest.get(params[0]);;
                    }

                    @Override
                    public int id() {
                        return 1;
                    }
                },"https://www.baidu.com")
                .capture(ExecutorThread.MAIN, 1, new IExecutorComplete<String>() {
                    @Override
                    public void onComplete(ExectuorValue<String> exectuorValue) {
                        System.out.println(exectuorValue.getRetValue());
                    }
                });
    }
}
```

```java
>:百度的源代码
```

2、从本地读取数据并发送给TextView

```java
public class Test{
    public static final void main(String[] args){
        Aysnc async=Async.create();
        async.io()
             .call(new AbsExecutor<String,JsonObject>() {
                  @Override
                  public JsonObject run(String... params) {
                      return new JsonObject(Buffer.IOStream.readAll(params[0]));
                  }

                  @Override
                  public int id() {
                      return 1;
                  }
              },"/data/data/text/file/userinfo.json")
              .capture(ExecutorThread.MAIN, 1, new IExecutorComplete<JsonObject>() {
                  @Override
                  public void onComplete(ExectuorValue<JsonObject> exectuorValue) {
                      textView.setText(exectuorValue.getRetValue());
                  }
              });
    }
}
```

# END