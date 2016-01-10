package org.voiddog.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.lionsoul.jcseg.analyzer.v5x.JcsegAnalyzer5X;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Hashtable;
import java.util.Map;

/**
 * 建立索引库
 * Created by qgx44 on 2016/1/7.
 */
public class IndexFiles {
    public static void main(String[] args){
        String usage = "java IndexFiles "
                +"[-index INDEX_PATH] [-docs DOCS_PATH] [-idFile ID_FILE_PATH] [-update]\n\n"
                +"创建一个lucene的索引文件\n\n其中id file的格式为filename url";
        String indexPath = "index";
        String docsPath = null;
        String idPath = null;
        boolean create = true;

        for(int docDir = 0; docDir < args.length; ++docDir) {
            if("-index".equals(args[docDir])) {
                indexPath = args[docDir + 1];
                ++docDir;
            } else if("-docs".equals(args[docDir])) {
                docsPath = args[docDir + 1];
                ++docDir;
            } else if("-update".equals(args[docDir])) {
                create = false;
            }
            else if("-idFile".equals(args[docDir])){
                idPath = args[++docDir];
            }
        }

        if(docsPath == null || idPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        Path pathDoc = Paths.get(docsPath);
        Path pathId = Paths.get(idPath);
        if(!Files.isReadable(pathDoc)){
            System.out.println("文档: " + pathDoc.toAbsolutePath() + "无效");
            System.exit(1);
        }
        if(!Files.isReadable(pathId)){
            System.out.println("id->url文件: " + pathId.toAbsolutePath() + "无效");
            System.exit(1);
        }

        long startTime = System.currentTimeMillis();

        //开始处理
        try{
            System.out.println("Indexing to directory \'" + indexPath + "\'...");
            FSDirectory index_dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new JcsegAnalyzer5X(JcsegTaskConfig.COMPLEX_MODE);
            JcsegAnalyzer5X jcseg = (JcsegAnalyzer5X) analyzer;
            JcsegTaskConfig config = jcseg.getTaskConfig();
            //追加同义词, 需要在 jcseg.properties中配置jcseg.loadsyn=1
            config.setAppendCJKSyn(true);
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            if(create) {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(index_dir, iwc);
            indexDocs(writer, pathDoc, pathId);
            writer.close();
            System.out.print("耗时: " + (System.currentTimeMillis() - startTime)/1000 + "s");
        }
        catch (IOException e){
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    static void indexDocs(final IndexWriter writer, Path pathDoc, Path pathId) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(pathId)));
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

        if(Files.isDirectory(pathDoc)) {
            Files.walkFileTree(pathDoc, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {

                        IndexFiles.indexDoc(writer
                                , file
                                , idToUrl.get(file.getFileName().toString()), attrs.lastModifiedTime().toMillis()
                        );

                    } catch (IOException ignore) {}
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {

            indexDoc(writer
                    , pathDoc
                    , idToUrl.get(pathDoc.getFileName().toString())
                    , Files.getLastModifiedTime(pathDoc).toMillis()
            );

        }
    }

    static void indexDoc(IndexWriter writer, Path file, IdContent idContent, long lastModified) throws IOException{
        if(idContent == null){
            return;
        }

        InputStream stream = Files.newInputStream(file);

        try {
            Document doc = new Document();
            StringField pathField = new StringField("path", file.toString(), Field.Store.YES);
            doc.add(pathField);
            doc.add(new LongField("modified", lastModified, Field.Store.NO));
            doc.add(new StringField("url", idContent.url, Field.Store.YES));
            doc.add(new StringField("title", idContent.title, Field.Store.YES));
            doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

            if(writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                System.out.println("adding " + file);
                writer.addDocument(doc);
            } else {
                System.out.println("updating " + file);
                writer.updateDocument(new Term("path", file.toString()), doc);
            }
        }finally {
            if(stream != null) {
                stream.close();
            }
        }
    }
}

class IdContent{
    public String url;
    public String title;

    public IdContent(){}

    public IdContent(String url, String title){
        this.url = url;
        this.title = title;
    }
}