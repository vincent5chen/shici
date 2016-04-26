package com.itranswarp.shici.search;

public class HitsResultWrapperSourceBuilder extends SourceBuilder {

	static final String TEMPLATE = "package ${package};                                         \n"
			+ "import ${package}.${name};                                                       \n"
			+ "import java.util.List;                                                           \n"
			+ "import                                   " + DocumentWrapper.class.getName() + ";\n"
			+ "import                                 " + HitsResultWrapper.class.getName() + ";\n"
			+ "import                                       " + HitsWrapper.class.getName() + ";\n"
			+ "public class ${name}HitsResultWrapper implements HitsResultWrapper<${name}> {    \n"
			+ "    public ${name}HitsWrapper hits;                                              \n"
			+ "    public HitsWrapper<${name}> getHitsWrapper() {                               \n"
			+ "        return hits;                                                             \n"
			+ "    }                                                                            \n"
			+ "    public Class<${name}> getSearchableClass() {                                 \n"
			+ "        return ${name}.class;                                                    \n"
			+ "    }                                                                            \n"
			+ "                                                                                 \n"
			+ "    public static class ${name}HitsWrapper implements HitsWrapper<${name}> {     \n"
			+ "        public int total;                                                        \n"
			+ "        public List<${name}DocumentWrapper> hits;                                \n"
			+ "        public int getTotal() {                                                  \n"
			+ "            return total;                                                        \n"
			+ "        }                                                                        \n"
			+ "        public List<? extends DocumentWrapper<${name}>> getDocumentWrappers() {  \n"
			+ "            return hits;                                                         \n"
			+ "        }                                                                        \n"
			+ "                                                                                 \n"
			+ "        public static class ${name}DocumentWrapper implements DocumentWrapper<${name}> { \n"
			+ "	           public double _score;                                                \n"
			+ "            public ${name} _source;                                              \n"
			+ "            public ${name} getDocument() {                                       \n"
			+ "                return _source;                                                  \n"
			+ "            }                                                                    \n"
			+ "            public double getScore() {                                           \n"
			+ "                return _score;                                                   \n"
			+ "            }                                                                    \n"
			+ "        }                                                                        \n"
			+ "    }                                                                            \n"
			+ "}                                                                                \n";

	protected String getTemplate() {
		return TEMPLATE;
	}

	@Override
	public String getFileName(Class<?> clazz) {
		return clazz.getSimpleName() + "HitsResultWrapper.java";
	}

	@Override
	public String getClassName(Class<?> clazz) {
		return clazz.getName() + "HitsResultWrapper";
	}

}
