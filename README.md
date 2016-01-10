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
