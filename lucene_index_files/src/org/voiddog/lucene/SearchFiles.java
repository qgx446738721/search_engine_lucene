package org.voiddog.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;
import org.lionsoul.jcseg.analyzer.v5x.JcsegAnalyzer5X;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 搜索文件
 * Created by qgx44 on 2016/1/8.
 */
public class SearchFiles {
    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava org.apache.lucene.demo.SearchFiles"
                + " [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\n"
                + "搜索所需要查询的字符串";

        if(args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);
            System.exit(0);
        }

        String index = "index";
        String field = "contents";
        String queries = null;
        int repeat = 0;
        boolean raw = false;
        String queryString = null;
        int hitsPerPage = 10;

        for(int reader = 0; reader < args.length; ++reader) {
            if("-index".equals(args[reader])) {
                index = args[reader + 1];
                ++reader;
            } else if("-field".equals(args[reader])) {
                field = args[reader + 1];
                ++reader;
            } else if("-queries".equals(args[reader])) {
                queries = args[reader + 1];
                ++reader;
            } else if("-query".equals(args[reader])) {
                queryString = args[reader + 1];
                ++reader;
            } else if("-repeat".equals(args[reader])) {
                repeat = Integer.parseInt(args[reader + 1]);
                ++reader;
            } else if("-raw".equals(args[reader])) {
                raw = true;
            } else if("-paging".equals(args[reader])) {
                hitsPerPage = Integer.parseInt(args[reader + 1]);
                if(hitsPerPage <= 0) {
                    System.err.println("There must be at least 1 hit per page.");
                    System.exit(1);
                }

                ++reader;
            }
        }

        DirectoryReader indexDir = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(indexDir);
        Analyzer analyzer = new JcsegAnalyzer5X(JcsegTaskConfig.COMPLEX_MODE);
        //非必须(用于修改默认配置): 获取分词任务配置实例
        JcsegAnalyzer5X jcseg = (JcsegAnalyzer5X) analyzer;
        JcsegTaskConfig config = jcseg.getTaskConfig();
        config.setAppendCJKSyn(true);
        BufferedReader in;
        if(queries != null) {
            in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
        } else {
            in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        }

        QueryParser parser = new QueryParser(field, analyzer);

        do {
            if(queries == null && queryString == null) {
                System.out.println("Enter query: ");
            }

            String line = queryString != null?queryString:in.readLine();
            if(line == null || line.length() == -1) {
                break;
            }

            line = line.trim();
            if(line.length() == 0) {
                break;
            }

            Query query = parser.parse(line);
            System.out.println("Searching for: " + query.toString(field));
            if(repeat > 0) {
                long startTime = System.currentTimeMillis();

                for(int end = 0; end < repeat; ++end) {
                    searcher.search(query, 100);
                }

                System.out.println("Time: " + (System.currentTimeMillis() - startTime) + "ms");
            }

            doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);
        } while(queryString == null);
    }

    public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, int hitsPerPage, boolean raw, boolean interactive) throws IOException, InvalidTokenOffsetsException {
        TopDocs results = searcher.search(query, 5 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");
        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);


        while(true) {
            if(end > hits.length) {
                System.out.println("Only results 1 - " + hits.length + " of " + numTotalHits + " total matching documents collected.");
                System.out.println("Collect more (y/n) ?");
                String quit = in.readLine();
                if(quit.length() == 0 || quit.charAt(0) == 110) {
                    break;
                }

                hits = searcher.search(query, numTotalHits).scoreDocs;
            }

            end = Math.min(hits.length, start + hitsPerPage);

            for(int i = start; i < end; ++i) {
                if(raw) {
                    System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
                } else {
                    Document line = searcher.doc(hits[i].doc);
                    String page = line.get("path");
                    if(page != null) {
                        System.out.println(i + 1 + ". " + page);
                        String url = line.get("url");
                        if(url != null) {
                            System.out.println("   Url: " + url);
                        }
                        String title = line.get("title");
                        if(title != null){
                            System.out.println("   Title: " + title);
                        }
                    } else {
                        System.out.println(i + 1 + ". " + "No path for this document");
                    }
                }
            }

            if(!interactive || end == 0) {
                break;
            }

            if(numTotalHits >= end) {
                boolean flag = false;

                while(true) {
                    System.out.print("Press ");
                    if(start - hitsPerPage >= 0) {
                        System.out.print("(p)revious page, ");
                    }

                    if(start + hitsPerPage < numTotalHits) {
                        System.out.print("(n)ext page, ");
                    }

                    System.out.println("(q)uit or enter number to jump to a page.");
                    String readLine = in.readLine();
                    if(readLine.length() == 0 || readLine.charAt(0) == 113) {
                        flag = true;
                        break;
                    }

                    if(readLine.charAt(0) == 112) {
                        start = Math.max(0, start - hitsPerPage);
                        break;
                    }

                    if(readLine.charAt(0) == 110) {
                        if(start + hitsPerPage < numTotalHits) {
                            start += hitsPerPage;
                        }
                        break;
                    }

                    int parseInt = Integer.parseInt(readLine);
                    if((parseInt - 1) * hitsPerPage < numTotalHits) {
                        start = (parseInt - 1) * hitsPerPage;
                        break;
                    }

                    System.out.println("No such page");
                }

                if(flag) {
                    break;
                }

                end = Math.min(numTotalHits, start + hitsPerPage);
            }
        }

    }
}
