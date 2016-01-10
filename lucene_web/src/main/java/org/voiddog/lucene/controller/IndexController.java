package org.voiddog.lucene.controller;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.lionsoul.jcseg.analyzer.v5x.JcsegAnalyzer5X;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.voiddog.lucene.model.SearchMessage;
import org.voiddog.lucene.model.SearchResultItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 主页控制器
 * Created by qgx44 on 2016/1/9.
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @Value("${index.path:}")
    public String indexPath;

    @Value("${index.conent.field:}")
    public String indexContentField;

    @RequestMapping(method = RequestMethod.GET)
    public String index(){
        return "index";
    }

    @RequestMapping(value = "search/{content}/{id}", method = RequestMethod.GET)
    public ModelAndView search(@PathVariable String content,@PathVariable int id){
        ModelAndView res = new ModelAndView("search_result");
        SearchMessage message = new SearchMessage();
        message.setPageId(id);
        message.setSearchString(content);
        res.addObject("message", message);

        //生成搜索结果
        res.addObject("search_result_list", getSearchResult(id, message));

        return res;
    }

    List<SearchResultItem> getSearchResult(int pageId, SearchMessage message){
        List<SearchResultItem> itemList = new ArrayList<>();
        try {
            DirectoryReader indexDir = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(indexDir);
            Analyzer analyzer = new JcsegAnalyzer5X(JcsegTaskConfig.COMPLEX_MODE);
            //非必须(用于修改默认配置): 获取分词任务配置实例
            JcsegAnalyzer5X jcseg = (JcsegAnalyzer5X) analyzer;
            JcsegTaskConfig config = jcseg.getTaskConfig();
            config.setAppendCJKSyn(true);

            QueryParser parser = new QueryParser(indexContentField, analyzer);

            String line = message.getSearchString();
            if(line == null || line.length() == -1) {
                return itemList;
            }

            line = line.trim();
            if(line.length() == 0) {
                return itemList;
            }

            Query query = parser.parse(line);
            System.out.println("Searching for: " + query.toString(indexContentField));

            int totalHits = doPagingSearch(searcher, query, itemList, pageId);
            int totalPage = (totalHits-1) / 20 + 1;
            message.setStartPageId(Math.max(1, pageId-2));
            message.setEndPageId(Math.min(totalPage, message.getStartPageId() + 4));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return itemList;
    }

    private int doPagingSearch(IndexSearcher searcher, Query query, List<SearchResultItem> itemList, int pageId) throws IOException {
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
}
