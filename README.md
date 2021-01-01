### 测试环境搭建

测试网站：本地搭建Sqli-lib环境。如图4-1所示。

Sqli-lib是一款由PHP语言编写的SQL注入漏洞闯关游戏，包含了六十几个可注入页面，涵盖了几乎所有类型的SQL注入漏洞，同时不同的关卡自定义了不同的过滤规则，是SQL注入漏洞学习的首选靶场。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps1.jpg) 

图4-1  本地搭建Sqli-lib环境

Mysql版本：5.6.11。如图4-2所示。此外版本5.7.19与版本8.0.20也做了测试，结果与版本5.6.11测试结果相近。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps2.jpg) 

图4-2 Mysql版本信息

PHP版本：5.6.27-nt。如图4-3所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps3.jpg) 

图4-3 PHP版本信息

WAF：网站安全狗Apache版（版本：V4.0.28330），如图4-4所示。测试过程中，所有类型的拦截规则都已开启。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps4.jpg) 

图4-4 网站安全狗运行图

开发工具：IDEA（版本：2018.2.5终版），如图4-5所示。IDEA 全称 IntelliJ IDEA，是java编程语言开发的集成环境。其在智能代码助手、代码自动提示、重构、JavaEE支持、各类版本工具([git](https://baike.baidu.com/item/git/12647237)、[svn](https://baike.baidu.com/item/svn/3311103)等)、JUnit、CVS整合、代码分析、 创新的GUI设计等方面的功能当属超常。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps5.jpg) 

图4-5 IDEA 2018版

开发语言及版本：JAVA（版本：1.8.0_111）



### 程序流程

## 程序总体流程

程序的整体调用关系如图4-6所示，模块一独立运行，模块四和模块五均需要调用模块二和模块三。数据流的走向为模块一通过网页爬取及自身构造，传递给模块四一个injectable.json文件，模块四取得injectable.json文件通过探测waf规则和提取有效bypass，传递给模块五一个bypass.txt文件，模块五结合模块一的injectable.json文件和模块四的bypass.txt文件，便可进行注入类型探测和最后的注入实施，最后以注入结果进行展示输出。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps6.jpg) 

图4-6 程序总体流程图

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps25.jpg) 

图 SQLDD运行界面

## 网页爬取及可注入点筛选流程

网页爬取流程如图4-7所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps7.jpg) 

图4-7 网页爬取流程图

Spider作为程序入口，输入一个种子URL，种子URL进入待访问队列。调用HtmlDownloader下载页面，HtmlDownloader自带三种下载模式：无参下载、GET有参下载、POST有参下载，根据URL的请求模式和参数，调用MyRequestSet相应的请求访问并下载html页面，并调用UrlGet提取出需要的URL进入待访问队列。调用MyResourseChooser选择需要的URL并且构造完整URL，调用IfHasKey判断是否有键，即判断是否为可能的注入点。如果有键则按SqlddDomain<URL，请求方法，键名列表>实体保存进url.json文件。

完成以上步骤，种子URL弹出队列，再对待访问队列新的队首URL进行如上操作，即可以广度优先搜索的方式遍历完种子URL下的所有URL，并保存所有可能的注入点，为下一步骤提供所需数据。

爬取结果如图4-8所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps8.jpg) 

图4-8 网页爬取结果

TestOneSite循环遍历url.json文件，循环调用Ifinjection判断当前网页是否有可注入点。Ifinjection通过构造会触发页面报错的语句探测得到其闭合规则，以及可用注释符，无注释符则返回空字符串。

探测结果按InjectableDomain<SqlddDomain，注入键，前闭合符，后闭合符，注释符，正常键值1，正常键值2，错误键值>实体保存进injectable.json文件。

可注入点筛选流程如图4-9所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps9.jpg) 

图4-9 可注入点筛选流程图

筛选结果如图4-10所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps10.jpg) 

图4-10 可注入点筛选结果

## 量化页面差异流程

量化页面差异流程如图4-11所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps11.jpg) 

## 量化页面差异流程图

PageComparison里有两个方法getNormalPageComparison和getRate，其共同构成量化页面差异的算法。getNormalPageComparison传入多组正常键值对，获取到以这些正常键值为参数得到的html页面的交集和相似比，getRate传入getNormalPageComparison获取到的页面交集和相似比以及当前测试参数，获取当前测试参数下的得分结果，为1则代表当前参数下的网页为正常页面，为2则代表当前参数下的网页为错误页面，为3则代表当前参数下的网页为被WAF拦截时的页面。其公式在模块说明的量化页面差异模块中有具体解释。

举例如图4-12所示。输入正常值1时，得分为1，按照上述确定的规则，判定此参数下的网页为正常网页；输入错误值-1时，得分为2，判定此参数下的网页为错误网页；输入会被WAF拦截的值1’ order by 1--+时，得分为3，判定此参数下的网页为被WAF拦截的网页。判定结果与实际符合。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps12.jpg) 

图4-12 页面量化实例

4.2.3  启发式构造payload流程

启发式构造payload流程如图4-13所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps13.jpg) 

## 启发式构造payload流程

splitBoundary方法将payload按一定规则拆分成几个部分，heuristicDetection再对此拆分后的部分逐部分进行探测，当被拦截时首先判断是否当前部分被WAF拦截，是则调用bypass库里有效的单关键字bypass进行绕过，不是则回溯之前部分，直至再次被WAF拦截，则此时可判定被WAF拦截的几部分组合，调用多关键字bypass进行绕过即可。按照这个规则逐部分对payload进行检测并绕过，最终构造成有效的payload。

4.2.4  WAF规则探测及bypass生成流程

WAF规则探测及bypass生成流程如图4-14所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps14.jpg) 

## WAF规则探测及bypass生成流程图

遍历injectable.json，构造基本的SQL语句并作为参数探测WAF，被拦截则调用ByPassRule，若单个关键字或者字符被拦截，则调用SingleCharBypass，筛选可用的单字符bypass，若两个或两个以上关键字或者字符组合被拦截则，则调用DoubleCharBypass，筛选可用的双字符bypass，若空白符被拦截，则调用getBlankReplace，筛选可用的空白字符bypass。最终结果以txt的文件格式按条数保存。

bypass筛选过程如图4-15所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps15.jpg) 

图4-15 bypass筛选过程

bypass筛选结果如图4-16所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps16.jpg) 

图4-16 bypass筛选结果

## 注入类型探测及注入测试流程

DetectType遍历InjectableDomain实体，依次调用UnionQueryPayloads类的ifCouldBeInjected方法，ErrorPayloads类的makeErrorBoundarys方法，TimeBlindPayloads类的filterBoundarys方法。如果ifCouldBeInjected返回true则表示联合注入对此注入点有效，makeErrorBoundarys方法将可利用的boundary保存，如果最终保存的boundary不为空，则表示报错注入对此注入点有效，filterBoundarys方法将可利用的boundary保存，如果最终保存的boundary不为空，则表示时间盲注对此注入点有效。注入类型探测流程如图4-17所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps17.jpg) 

图4-17 注入类型探测流程

由于完整探测所有页面可注入类型耗时太长，这里只截取部分探测结果，并展示其中可用boundary样例。如图4-18所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps18.jpg) 

图4-18 注入类型探测结果

联合注入测试，按照流程化构造每一个注入阶段的注入过程，其中getColNum方法通过两种规则获取列数，getDisplayPlaces方法在getColNum获得列数的基础上获取显示位，getDatabase方法获取当前的数据库名，getAllDatabase方法获取所有的数据库名，getTableName方法获取表名，getColumnName方法获取列名，getData方法获取数据。每一个注入阶段都需要结合量化页面差异模块和启发式构造payload模块。

联合注入流程如图4-19所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps19.jpg) 

图4-19 联合注入流程

联合注入结果如图4-20所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps20.jpg) 

图4-20 联合注入结果

报错注入测试，报错注入的boundary的构造有两种方式，一种是函数报错，经过nestingQuery方法的嵌套层数选择，concatChoise方法的联合函数选择，再经过getErrorFunc方法的报错函数选择，构成一类函数报错boundary。另一种是通过固定句型报错，比如floor rand()的组合报错。两种构造方式共同构成报错注入的boundary集合。getDatas方法循环遍历boundary集合，调用getAllDatabases方法执行完整注入测试，直至注出结果，循环结束，返回结果。

报错注入流程如图4-21。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps21.jpg) 

图4-21 报错注入流程

报错注入结果如图4-22所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps22.jpg) 

图4-22 报错注入结果

时间盲注测试，通过subStrFuncChoise方法的子句函数选择，asciiFuncChoise方法的ASCII码函数选择，doWhats方法的延迟语句选择，judgeFuncChoise方法的判断语句选择后，构造一个时间盲注的boundary集合。通过getDatabaseAllIn循环遍历boundary集合，结合延迟判断方法triggerDelay，调用getDatabasePerBoundary方法获取当前库名，获取到后进入下一个获取表的循环获取到表名，依次获取如此得到最终数据。

时间盲注流程如图4-23所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps23.jpg) 

图4-23 时间盲注流程图

时间盲注结果如图4-24所示。

![img](file:///C:\Users\kenshin\AppData\Local\Temp\ksohtml12636\wps24.jpg) 

图4-24 时间盲注结果