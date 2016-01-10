# lucene_index_files
建立索引文件

`org.voiddog.lucene.IndexFiles`是建立索引用  
用法:
```
    java IndexFiles [-index INDEX_PATH] [-docs DOCS_PATH] [-idFile ID_FILE_PATH] [-update]
```
其中`idFile`为爬虫建立的id到url和title的文件 格式为`id \t\t url \t\t title`  
'org.voiddog.lucene.SearchFiles'是测试索引用文件  
用法:
```
    java org.apache.lucene.demo.SearchFiles
    [-index dir] [-field f] [-repeat n]
    [-queries file] [-query string] [-raw]
    [-paging hitsPerPage]
```

# lucene_web
搜索引擎网页端  

`java web`框架为`spring-boot`，页面模板采用`thymeleaf`  
建立好工程后`gradle bootRun`（windows下为`gradlew boorRun`），默认端口开启在`8080`  

工程配置文件(`src/main/resources/application.properties`):  
`index.path:`为索引文件所在目录，索引文件采用`lucene_index_files`建立的索引  
`index.conent.field:`为主内容所用字段  

# 小型搜索引擎设计和构建

**小组成员：** 戚耿鑫 `12051124`、葛张鹏 `12108212`、刘金玲 `12051104`
**指导老师：** 郑秋华

@(实验报告)[搜索引擎]

[toc]

## 一、作业要求以及介绍
### 1. 搜索引擎简介以及原理阐述

搜索引擎（Search Engine）是指根据一定的策略、运用特定的计算机程序从互联网上搜集信息，在对信息进行组织和处理后，为用户提供检索服务，将用户检索相关的信息展示给用户的系统。搜索引擎包括全文索引、目录索引、元搜索引擎、垂直搜索引擎、集合式搜索引擎、门户搜索引擎与免费链接列表等  

主要步骤如下：
1. 爬行抓取网页
> 搜索引擎派出一个能够在网上发现新网页并抓文件的程序，这个程序通常称之为蜘蛛（Spider）。搜索引擎从已知的数据库出发，就像正常用户的浏览器一样访问这些网页并抓取文件。搜索引擎通过这些爬虫去爬互联网上的外链，从这个网站爬到另一个网站，去跟踪网页中的链接，访问更多的网页，这个过程就叫爬行。这些新的网址会被存入数据库等待搜索。所以跟踪网页链接是搜索引擎蜘蛛（Spider）发现新网址的最基本的方法，所以反向链接成为搜索引擎优化的最基本因素之一。搜索引擎抓取的页面文件与用户浏览器得到的完全一样，抓取的文件存入数据库  

2. 建立索引
> 蜘蛛抓取的页面文件分解、分析，并以巨大表格的形式存入数据库，这个过程即是索引（index).在索引数据库中，网页文字内容，关键词出现的位置、字体、颜色、加粗、斜体等相关信息都有相应记录  

3. 搜索词处理
> 用户在搜索引擎界面输入关键词，单击“搜索”按钮后，搜索引擎程序即对搜索词进行处理，如中文特有的分词处理，去除停止词，判断是否需要启动整合搜索，判断是否有拼写错误或错别字等情况。搜索词的处理必须十分快速  

4. 排序
> 对搜索词处理后，搜索引擎程序便开始工作，从索引数据库中找出所有包含搜索词的网页，并且根据排名算法计算出哪些网页应该排在前面，然后按照一定格式返回到“搜索”页面。再好的搜索引擎也无法与人相比，这就是为什么网站要进行搜索引擎优化。没有SEO的帮助，搜索引擎常常并不能正确的返回最相关、最权威、最有用的信息  

本系统主要在爬虫所抓下来的资源上建立一个小型搜索引擎，要求实现一个简单的网页，输入需要搜索的关键字，展现出搜索结果  

## 二、系统设计
### 1. 概述
和之前的两个实验不同的是，本系统采用`Java`实现。使用的是`Lucene`开源搜索引擎框架，web 页面使用`spring-boot`来搭建，页面模板引擎使用`thymeleaf`。

框架之多，导致构建系统的时候有点繁杂，本系统先使用`Lucene`建立索引，然后搭建 web 服务，根据用户输入的关键字，在索引文件中查找后，返回结果页面给用户。

### 2. 系统构建流程
`Lucene`有提供建立索引的模板，根据模板修改。目录结构如下：
**索引工程目录**
```
-lib
  | - lexicon(存放字典文件)
  | - jcseg-analyzer-1.9.7.jar
  | - jcseg-core-1.9.7.jar
  | - lucene-analyzers-common-5.4.0.jar
  | - lucene-core-5.4.0.jar
  | - lucene-demo-5.4.0.jar
  | - lucene-highlighter-5.4.0.jar
  | - lucene-queryparser-5.4.0.jar
-src
  | - org.voiddog.lucene
					 | - IndexFiles.java
					 | - SearchFiles.java
```
**web工程目录**
```
-lib
  | - 同索引工程目录
-src
  | - text
  | - main
	    | - java (java 源码文件)
		      | - org.voiddog.lucene
								 | - controller
								 | - model
								 | - WebApplication.java
	    | - resource (网页资源文件)
		      | - static
		      | - templates
		      | - application.properties
```
**分词器的选择**
`Lucene`内自带的`Standanalysis`支持对中文分词处理，但是使用过后我发现，这个分词器是对中文一个字一个字切分的，这不是我想要的结果。  

经过搜索发现有第三方的开源分析器[`jcseg`](http://git.oschina.net/lionsoul/jcseg)只是需要自己编译，它采用的构建系统是`ant`，在构建的时候，windows 平台可能会出现一点问题，首先需要修改编译的编码为`utf-8`，其次因为本程序只需要使用`jsceg`中的核心模块，所以去掉`ant`构建文件`build.xml`中多余的项目，只留下`jcseg-core`和`jsceg-analyzer`。

然后在当前目录下执行
```
ant all
```
编程成功后会得到三个 jar 文件：`jcseg-core-1.9.7.jar`、`jcseg-analyzer-1.9.7.jar`、`jcseg-1.9.7-javadoc.jar`，我们只需第一个和第二个，第三个为自动生成的 api doc。

拷贝 jar 文件到 lib 目录下，添加 Library 的支持。

## 三、详细设计

### 1. 建立索引
本模块主要实现对文档文件建立索引，文档资源为 **python 爬虫爬取的网页资源**。`python`工程获取的网页资源已经放在了一个文件夹下，并且带有一个属性文件`id.txt`。本项目根据这个索引资源来建立索引文件。

先从`id.txt`文件中读取各个资源文件的属性到`Map`中。
```java
BufferedReader br = new BufferedReader(
	new InputStreamReader(Files.newInputStream(pathId))
);

final Map<String, IdContent> idToUrl = new Hashtable<>();
String line;

do{
    line = br.readLine();
    if(line != null) {
        String[] sp = line.split("(\t\t)");
        if (sp.length == 3) {
            idToUrl.put(sp[0], new IdContent(sp[1], sp[2]));
        }
    }
}while(line != null);
```

遍历当前文件的目录树，建立索引
```java
if(Files.isDirectory(pathDoc)) {
    Files.walkFileTree(pathDoc, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(
	        Path file, BasicFileAttributes attrs
        ) throws IOException {
            try {
                IndexFiles.indexDoc(
		                writer
                        , file
                        , idToUrl.get(file.getFileName().toString())
                        , attrs.lastModifiedTime().toMillis()
                );
            } catch (IOException ignore) {}
            return FileVisitResult.CONTINUE;
        }
    });
} else {
    indexDoc(
		    writer
            , pathDoc
            , idToUrl.get(pathDoc.getFileName().toString())
            , Files.getLastModifiedTime(pathDoc).toMillis()
    );
}
```
`writer`是`Lucene`中的`IndexWriter`类，创建该类的时候需要创建相应的分词器和配置信息，这里的分析器使用上述编译的`jsceg`
```java
Analyzer analyzer = new JcsegAnalyzer5X(JcsegTaskConfig.COMPLEX_MODE);
JcsegAnalyzer5X jcseg = (JcsegAnalyzer5X) analyzer;
JcsegTaskConfig config = jcseg.getTaskConfig();
//追加同义词, 需要在 jcseg.properties中配置jcseg.loadsyn=1
config.setAppendCJKSyn(true);
IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
IndexWriter writer = new IndexWriter(index_dir, iwc);
```
上述代码就是简单的遍历目录树，建立索引文件主要在`indexDoc`函数内，下面看看`indexDoc'`的函数
```java
static void indexDoc(
	IndexWriter writer, Path file
	, IdContent idContent, long lastModified) throws IOException{
	
    if(idContent == null){
        return;
    }

    InputStream stream = Files.newInputStream(file);

    try {
        Document doc = new Document();
        StringField pathField = new StringField(
	        "path", file.toString(), Field.Store.YES);
        
        doc.add(pathField);
        doc.add(new LongField(
	        "modified", lastModified, Field.Store.NO)
	    );
        doc.add(new StringField(
	        "url", idContent.url, Field.Store.YES)
	    );
        doc.add(new StringField(
	        "title", idContent.title, Field.Store.YES)
	    );
        doc.add(new TextField("contents"
        , new BufferedReader(
	        new InputStreamReader(stream, StandardCharsets.UTF_8))
	      )
	    );

        if(writer.getConfig().getOpenMode() 
	        == IndexWriterConfig.OpenMode.CREATE) {
            System.out.println("adding " + file);
            writer.addDocument(doc);
        } else {
            System.out.println("updating " + file);
            writer.updateDocument(new Term(
	            "path", file.toString()), doc);
        }
    }finally {
        if(stream != null) {
            stream.close();
        }
    }
}
```
索引的建立过程：  
建立一个文档`Document`，文档是存档到索引的，然后往文档中添加数据`Field`，添加的数据有：
1. "modified" 文件最后编辑时间
2. "url" 网页路径
3. "title" 网页标题
4. "content" 网页内容 （不存储）

至此，索引建立完毕

### 2. 搭建web服务器

#### 代码逻辑部分
web 服务器是使用`spring-boot`搭建的，相比于`spring`而言，`spring-boot`没有烦人的`xml`配置，采用构建系统是`gradle`，整体代码为 mvc 架构，接口的开放使用注解实行，开发快速。  
`gradle`配置文件
```
buildscript {
    repositories {
        jcenter()
        maven { url "http://repo.spring.io/snapshot" }
        maven { url "http://repo.spring.io/milestone" }
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:1.3.2.BUILD-SNAPSHOT")
        classpath("org.springframework:springloaded:1.2.4.RELEASE")
    }
}

group 'org.voiddog.lucene'
version '1.0-SNAPSHOT'

apply plugin: 'java'
apply plugin: 'idea'
apply plugin: 'spring-boot'

sourceCompatibility = 1.5

repositories {
    jcenter()
    maven { url "http://repo.spring.io/snapshot" }
    maven { url "http://repo.spring.io/milestone" }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-thymeleaf")
    compile("org.springframework.boot:spring-boot-starter-web")
    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile group: 'junit', name: 'junit', version: '4.11'
}
```

本项目只需要一个搜索页面和结果展示页面，所以我只用了一个控制器 `IndexController`  

控制器主要代码：
```java
**
 * 主页控制器
 * Created by qgx44 on 2016/1/9.
 */
@Controller
@RequestMapping("/")
public class IndexController {
	
	//索引文件所在路径
    @Value("${index.path:}")
    public String indexPath;
	
	//主内容字段名
    @Value("${index.conent.field:}")
    public String indexContentField;

	//访问主页 返回index为主页模板名
    @RequestMapping(method = RequestMethod.GET)
    public String index(){
        return "index";
    }
	
	//带参数的get访问 例：localhost/计算机/1
    @RequestMapping(
	    value = "search/{content}/{id}", method = RequestMethod.GET
	)
    public ModelAndView search(
	    @PathVariable String content,@PathVariable int id){
	    
	    //创建一个
        ModelAndView res = new ModelAndView("search_result");
        SearchMessage message = new SearchMessage();
        message.setPageId(id);
        message.setSearchString(content);
        res.addObject("message", message);

        //生成搜索结果
        res.addObject(
        "search_result_list"
	        , getSearchResult(id, message));
	        
        return res;
    }
	
	//产生搜索结果
    List<SearchResultItem> getSearchResult(
	    int pageId, SearchMessage message){
        List<SearchResultItem> itemList = new ArrayList<>();
        try {
	        //索引读取
            DirectoryReader indexDir = DirectoryReader.open(
	            FSDirectory.open(Paths.get(indexPath)));
	            
            IndexSearcher searcher = new IndexSearcher(indexDir);
            Analyzer analyzer = new JcsegAnalyzer5X(
	            JcsegTaskConfig.COMPLEX_MODE);
	            
            //非必须(用于修改默认配置): 获取分词任务配置实例
            JcsegAnalyzer5X jcseg = (JcsegAnalyzer5X) analyzer;
            JcsegTaskConfig config = jcseg.getTaskConfig();
            config.setAppendCJKSyn(true);

            QueryParser parser = new QueryParser(
	            indexContentField, analyzer);

            String line = message.getSearchString();
            if(line == null || line.length() == -1) {
                return itemList;
            }

            line = line.trim();
            if(line.length() == 0) {
                return itemList;
            }
			
			//查询结果
            Query query = parser.parse(line);
            System.out.println(
	            "Searching for: " + query.toString(indexContentField));
			
			//获取到匹配总数
            int totalHits = doPagingSearch(
	            searcher, query, itemList, pageId);
            int totalPage = (totalHits-1) / 20 + 1;
            //分页信息
            message.setStartPageId(Math.max(1, pageId-2));
            message.setEndPageId(
	            Math.min(totalPage, message.getStartPageId() + 4));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return itemList;
    }
}
```
其中`@Value("${index.path:}")`是获取工程目录`resources`文件夹下`application.properties`文件中的配置属性，文件内容如下：
```
# Allow Thymeleaf templates to be reloaded at dev time
spring.thymeleaf.cache: false
server.tomcat.access_log_enabled: true
server.tomcat.basedir: target/tomcat
index.path: C:/Users/...这里省略 太长了
index.conent.field: contents
```
关键代码部分就是`doPagingSearch`，代码如下
```java
private int doPagingSearch(
	IndexSearcher searcher, Query query
	, List<SearchResultItem> itemList, int pageId) throws IOException {
	
    TopDocs results = searcher.search(query, pageId*20);
    ScoreDoc[] hits = results.scoreDocs;
    int numTotalHits = results.totalHits;
    int start = (pageId-1)*20;
    int end = Math.min(numTotalHits, start + 20);
    for(int i = start; i < end; ++i){
        Document line = searcher.doc(hits[i].doc);
        String page = line.get("path");
        if(page != null) {
            SearchResultItem item = new SearchResultItem();
            item.setUrl(line.get("url"));
            item.setTitle(line.get("title"));
            itemList.add(item);
        }
    }
    return numTotalHits;
}
```
代码中`searcher.search(query, pageId*20)`返回索引的结果，其中`query`是查询的内容，`pageId`是当前的页码，因为每页20个结果，所以需要查询`pageId*20`条信息。

找到索引内容后，for 循环遍历，每页20条，那么当前页码的起始id为`(pageId - 1)*20`，结束页码为`Math.min(numTotalHits, start + 20)`，从每一条信息中获取一个文档对象，改文档对象是之前建立索引的时候的文档信息。所以可以提取`url`、`title`信息。获取到后添加到对应的`ArrayList`中返回结果。

#### 视图页面部分
本项目采用了视图模板，使用的是`thymeleaf`，在`spring-boot`中有默认继承，当然需要在`gradle`中添加`compile("org.springframework.boot:spring-boot-starter-thymeleaf")`。

**Thymeleaf 变量**

`Thymeleaf`模板引擎在进行模板渲染时，还会附带一个`Context`存放进行模板渲染的变量，在模板中定义的表达式本质上就是从`Context`中获取对应的变量的值：
```vbscript-html
<p>Today is: <span th:text="${today}">13 february 2011</span>.</p>
```
假设`today`的值为2015年8月14日，那么渲染结果为：`<p>Today is: 2015年8月14日.</p>`。可见`Thymeleaf`的基本变量和JSP一样，都使用`${.}`表示获取变量的值。

**Thymeleaf Url**

`URL`在Web应用模板中占据着十分重要的地位，**需要特别注意**的是`Thymeleaf`对于`URL`的处理是通过语法`@{...}`来处理的。`Thymeleaf`支持绝对路径`URL`：
```
<a th:href="@{http://www.thymeleaf.org}">Thymeleaf</a>
```
**Thymeleaf 循环**

渲染列表数据是一种非常常见的场景，例如现在有`n`条记录需要渲染成一个表格`<table>`，该数据集合必须是可以遍历的，使用`th:each`标签：
```
<body>
  <h1>Product list</h1>

  <table>
    <tr>
      <th>NAME</th>
      <th>PRICE</th>
      <th>IN STOCK</th>
    </tr>
    <tr th:each="prod : ${prods}">
      <td th:text="${prod.name}">Onions</td>
      <td th:text="${prod.price}">2.41</td>
      <td th:text="${prod.inStock}? #{true} : #{false}">yes</td>
    </tr>
  </table>

  <p>
    <a href="../home.html" th:href="@{/}">Return to home</a>
  </p>
</body>
```
可以看到，需要在被循环渲染的元素（这里是`<tr>`）中加入`th:each`标签，其中`th:each="prod : ${prods}"`意味着对集合变量`prods`进行遍历，循环变量是`prod`在循环体中可以通过表达式访问。

上述变量在java中是通过注解，或者通过`ModelAndView`绑定数据
```java
ModelAndView res = new ModelAndView("search_result");
//添加对象到视图中，可以根据${message.id}访问
res.addObject("message", message);
res.addObject("search_result_list", getSearchResult(id, message));
```

页面布局使用了前端框架`bootstrap`，首页的内容是用`jquery`来居中显示的：
```
$(window).resize(function(){
    $('.screen-center').css(
    "padding-top",
     ($(window).height() - $('.screen-center').outerHeight())/2 
     + $(document).scrollTop());
});
```

搜索结果的列表内容通过`Thymeleaf`的 `for` 循环来生成，关键代码如下：
```html
<!-- 搜素结果列表 -->
<div class="search-item row" th:each="item : ${search_result_list}">
    <h3>
	    <a href="#" th:href="${item.url}" th:text="${item.title}" 
		    target="_blank">
    这是标题
	    </a>
    </h3>
    <a href="#" th:text="${item.url}" th:href="${item.url}">链接</a>
</div>
```
分页导航是通过`jQuery`动态的添加实现，这就出现了与`js`的数据交互问题，解决办法就是使用`inline="javascript"`使得一个`script`模块中的`Thymeleaf`变量可以解析。介意使用全局变量先存储，数据和逻辑分开处理。不然会因为代码的`<`或者`>`操作符出现模板引擎解析错误。
**数据区:**
```javascript
<script th:inline="javascript">
/*<![CDATA[*/
    var content = /*[[${message.searchString}]]*/;
    var startId = /*[[${message.startPageId}]]*/;
    var endId = /*[[${message.endPageId}]]*/;
    var pageId = /*[[${message.pageId}]]*/;
/*]]>*/
</script>
```
**添加分页导航**
```javascript
String.format = function() {
    if( arguments.length == 0 )
        return null;

    var str = arguments[0];
    for(var i=1;i < arguments.length;i++) {
        var re = new RegExp('\\{' + (i-1) + '\\}','gm');
        str = str.replace(re, arguments[i]);
    }
    return str;
}

$(document).ready(function(){
    $('.pinned').pin();

    //添加向右的箭头
    if(pageId == startId){
        $('#pagin-list').append(
	        '<li class="disabled"><a href="#">&laquo;</a></li>'
	     );
    }
    else{
        $('#pagin-list').append(String.format(
	        '<li><a href="/search/{0}/{1}">&laquo;</a></li>'
	        , content
	        , pageId-1)
	     );
    }

    //绘制中间的链接
    var i = startId;
    while(i <= endId){
        console.log(i);
        if(i == pageId){
            $('#pagin-list').append(String.format(
	            '<li class="active"><a href='
	            +'"/search/{0}/{1}">{2}</a></li>'
	            , content
	            , i, i)
	         );
        }
        else{
            $('#pagin-list').append(String.format(
	            '<li><a href="/search/{0}/{1}">{2}</a></li>'
	            , content, i, i)
	         );
        }
        ++i;
    }


    //添加左箭头
    if(pageId == endId){
        $('#pagin-list').append(
	        '<li class="disabled"><a href="#">&raquo;</a></li>'
	     );
    }
    else{
        $('#pagin-list').append(String.format(
	        '<li><a href="/search/{0}/{1}">&raquo;</a></li>'
	        , content, pageId+1)
	     );
    }
});
```
