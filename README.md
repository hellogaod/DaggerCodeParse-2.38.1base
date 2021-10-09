

# 前言 #

原先研究`Dagger2`源码是基于`2.16`（个人建议该版本代码不用看，太乱，导致阅读难度加大），后来切换到`2.38.1`，因为有了`2.16`的基础并且`2.38.1`版本源码做了一些分类处理，所以理解起来容易很多。

**所以Dagger源码解析是基于`2.38.1`版本**

# Dagger的作用 #



1. 我们正常使用一个对象，直接`new`即可：`Object obj = new Object()`,例如


		class A {
	
			B b;
			
			public A(){
				b = new B();
			}
			
		}

2. `IOC`（控制反转）的用法：

	class A{
		B b;

		@Inject
		public A(B b){
			this.b = b;
		} 
	}

看起来非常简单（好像看起来也挺头疼的哈），实际上有3难：

（1） 实际应用难，Dagger这些注解的规则和注解的实际意义不理解的情况下，很难使上全功

（2）Dagger源码理解难，例如2.16版本的源码一团糟，给源码阅读带来很大的不便，2.38.1版本使用了包名分类，但是也并不是那么轻松

（3）选择Dagger也很难，有必要使用Dagger吗？？？我个人的意见是根据团队和项目两方面考虑：a.项目角度，如果项目本身是中小型项目，没必要用Dagger；b.团队角度，如果团队整体实力不高或者没有对Dagger吃的很透的那么一个人也没必要使用（错误的使用，反而给开发带来非必要成本）。

**综上，在没必要的情况下使用Dagger反而会给项目带来一定风险。但是个人认为，Dagger未来可能会是Android开发非常重要的框架，因为Java已有各种Spring成熟的框架（一般情况下，没必要在引用Dagger），还有Dagger是google团队在维护（android也是google团队的啊！！！），为了降低Dagger使用难度，google团队又推出一个hilt（Dagger一个缩小版，后面会介绍到）**


# Dagger主要目录 #

1. android ：主要针对Android的使用

2. grpc ： dagger在grpc上的扩展

3. hilt : 缩小版Dagger，据说理解更加方便（源码还没看）

4. internal.codegen ： 内部逻辑代码，也是Dagger核心文件

 - base： 一些基础类
 - binding： 绑定逻辑处理，方法绑定，成员绑定等
 - componentgenerator： Component注解代码生成非常复杂，所以单独拎出来
 - validation： 理解Dagger核心点，主要用于校验注解的使用，也是理解Dagger各种注解的重要手段
 - writing： 注解校验正确情况下，该分类用于生成class文件

5. spi： 相当于api，区别在于spi是放在server端

# Dagger入口类 #

**ComponentProcessor**：处理核心注解-注解校验，生成逻辑代码

核心部件当然离不开@Component，@Inject，@Module，@Provider注解

最核心的是ProsessingStepModule类中针对不同注解的处理（校验或生成逻辑代码）：

- InjectProcessingStep：将Inject注解或AssistedInject注解元素注册到InjectBindingRegistry对象，会在ComponentProcessingStep集中处理该注册元素；
- ModuleProcessingStep： 处理Module注解的类及其方法
- ComponentProcessingStep:处理Component注解及其关联的Inject，Module，SubComponent注解逻辑。最关键，也是最难理解的类
- 其他各种Step后面文章中会提到，如果文章中没有，那么代码中会有具体注释

> 这里ProcessorComponent本身就是用到了自身逻辑注解，让我百思不得其姐，暂时理解为Bazel语言的一种福利

**AndroidProcessor**

该类主要针对android使用MapKey注解的校验，以及生成逻辑代码

	
# 最后 #

本篇之前还有一篇是`2.16`版本，也是2.16看了大概60%，但是该版本的源码实在是...。本想着自己对架构进行分类，但是无意间看到了2.38.1版本，所以就看起了2.38.1。

本着架构和细节都注重的原则：框架和重要思想在文章中描述，细节在代码中提现

目的：

1. 学习总结，为以后使用Dagger提供依据（大脑是内存，而不是硬盘，学会遗忘）

2. 学习中得到锤炼，尽可能静下来细细品味（浮躁的社会需要有一颗追求踏实的心，**后面也会自己开发一个App，一款细细品味自我的App**，哈哈哈~~~）

3. 让有必要的人群一起学习，并且对存在的问题给我指正，也是大家一起提升。


我是一个自信的人，窥视自己的内在，正视自己的缺点；我要让自己每个细胞都知道我的能干，提高它们的积极性。