package com.itranswarp.shici.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.shici.bean.Hit;
import com.itranswarp.shici.bean.Poem;

/**
 * Search service powered by Lucene.
 * 
 * @author liaoxuefeng
 */
@Component
public class SearchService {

	final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	PoemService poemService;

	private Analyzer analyzer;
	private Directory index;

	@PostConstruct
	public void init() throws IOException {
		this.analyzer = new StandardAnalyzer();
		this.index = new RAMDirectory();
		addPoems(poemService.poems.values().iterator());
	}

	@PreDestroy
	public void close() throws IOException {
		this.index.close();
	}

	public Hit[] parseAndSearch(String q) throws ParseException {
		logger.info("parse {}...", q);
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(new QueryParser("name", this.analyzer).parse(q), Occur.SHOULD);
		builder.add(new QueryParser("poetName", this.analyzer).parse(q), Occur.SHOULD);
		builder.add(new QueryParser("content", this.analyzer).parse(q), Occur.SHOULD);
		return search(builder.build(), 25);
	}

	public Hit[] search(String q) {
		logger.info("search {}...", q);
		String[] ss = q.split("[\\s\\;\\,\\.\\?\\？\\，\\、\\。\\；]+");
		if (ss.length == 0) {
			return EMPTY_HITS;
		}
		if (ss.length > 5) {
			ss = Arrays.copyOf(ss, 5);
		}
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		for (String s : ss) {
			builder.add(buildMultiFieldQuery(s), Occur.SHOULD);
		}
		return search(builder.build(), 25);
	}

	static final Hit[] EMPTY_HITS = new Hit[0];

	Hit[] search(Query query, int max) {
		try (IndexReader reader = DirectoryReader.open(this.index)) {
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(query, max);
			if (docs.scoreDocs.length == 0) {
				return EMPTY_HITS;
			}
			Hit[] hits = new Hit[docs.scoreDocs.length];
			for (int i = 0; i < hits.length; i++) {
				int docId = docs.scoreDocs[i].doc;
				Document d = searcher.doc(docId);
				hits[i] = new Hit(Long.parseLong(d.get("id")), docs.scoreDocs[i].score);
			}
			return hits;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	Query buildMultiFieldQuery(String s) {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(buildQuery("name", s), Occur.SHOULD);
		builder.add(buildQuery("poetName", s), Occur.SHOULD);
		builder.add(buildQuery("content", s), Occur.SHOULD);
		return builder.build();
	}

	Query buildQuery(String field, String s) {
		if (s.length() > 7) {
			s = s.substring(0, 7);
		}
		if (s.length() == 1) {
			return new TermQuery(new Term(field, s));
		}
		return new PhraseQuery(field, chars(s));
	}

	String[] chars(String s) {
		String[] ss = new String[s.length()];
		for (int i = 0; i < s.length(); i++) {
			ss[i] = String.valueOf(s.charAt(i));
		}
		return ss;
	}

	public void addPoems(Poem... poems) throws IOException {
		logger.info("index {} poems...", poems.length);
		IndexWriterConfig config = new IndexWriterConfig(this.analyzer);
		try (IndexWriter w = new IndexWriter(this.index, config)) {
			for (Poem poem : poems) {
				w.addDocument(toDocument(poem));
			}
		}
	}

	public void addPoems(Iterator<Poem> it) throws IOException {
		logger.info("index poems...");
		IndexWriterConfig config = new IndexWriterConfig(this.analyzer);
		try (IndexWriter w = new IndexWriter(this.index, config)) {
			while (it.hasNext()) {
				w.addDocument(toDocument(it.next()));
			}
		}
	}

	Document toDocument(Poem poem) throws IOException {
		Document doc = new Document();
		doc.add(new StringField("id", String.valueOf(poem.getId()), Field.Store.YES));
		doc.add(new StringField("form", poem.getForm(), Field.Store.NO));
		doc.add(new StringField("dynastyName", poem.getPoet().getDynasty().getName(), Field.Store.NO));

		doc.add(new TextField("tags", String.join(", ", poem.getTags()), Field.Store.NO));
		doc.add(new TextField("poetName", poem.getPoet().getName(), Field.Store.NO));
		doc.add(new TextField("name", poem.getName(), Field.Store.NO));
		doc.add(new TextField("content", poem.getContent(), Field.Store.NO));
		return doc;
	}

	public String[] suggest(String q) throws IOException {
		System.out.println("suggest " + q + "...");
		if (q.length() > 5) {
			return EMPTY_STRING_ARRAY;
		}
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		builder.add(new PhraseQuery("name", chars(q)), Occur.SHOULD);
		builder.add(new PhraseQuery("poetName", chars(q)), Occur.SHOULD);
		builder.add(new PhraseQuery("content", chars(q)), Occur.SHOULD);
		Query query = builder.build();
		Hit[] hits = search(query, 20);
		String[] words = Arrays.stream(hits).map(hit -> {
			Poem poem = poemService.getPoem(hit.id);
			String s = null;
			s = suggest(q, poem.getName());
			if (s != null) {
				return s;
			}
			s = suggest(q, poem.getPoet().getName());
			if (s != null) {
				return s;
			}
			s = suggest(q, poem.getContent());
			return s;
		}).filter(s -> s != null && !s.equals(q)).distinct().limit(10).toArray(String[]::new);
		Arrays.stream(words).forEach(System.out::println);
		return words;
	}

	String suggest(String q, String text) {
		int n = text.indexOf(q);
		if (n < 0) {
			return null;
		}
		int end = n + q.length();
		if (end >= text.length()) {
			return null;
		}
		if (stopChars.indexOf(text.charAt(n + q.length())) >= 0) {
			return null;
		}
		int max = Math.min(text.length(), n + 7);
		for (int i = end; i < max; i++) {
			char ch = text.charAt(i);
			if (stopChars.indexOf(ch) >= 0) {
				break;
			}
			q = q + ch;
		}
		return q;
	}

	static String[] EMPTY_STRING_ARRAY = new String[0];
	static String stopChars = " \r\n\"\'()[]{}“”‘’.,:;!?·！：？。，；、";
}
