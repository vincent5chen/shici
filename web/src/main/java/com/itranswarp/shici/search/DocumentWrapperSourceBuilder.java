package com.itranswarp.shici.search;

public class DocumentWrapperSourceBuilder extends SourceBuilder {

	static final String TEMPLATE = "package ${package};                                         \n"
			+ "import ${package}.${name};                                                       \n"
			+ "import                                   " + DocumentWrapper.class.getName() + ";\n"
			+ "public class ${name}DocumentWrapper implements DocumentWrapper<${name}> {        \n"
			+ "	   public double _score;                                                        \n"
			+ "    public ${name} _source;                                                      \n"
			+ "    public ${name} getDocument() {                                               \n"
			+ "        return _source;                                                          \n"
			+ "    }                                                                            \n"
			+ "    public double getScore() {                                                   \n"
			+ "        return _score;                                                           \n"
			+ "    }                                                                            \n"
			+ "}                                                                                \n";

	protected String getTemplate() {
		return TEMPLATE;
	}

	@Override
	public String getFileName(Class<?> clazz) {
		return clazz.getSimpleName() + "DocumentWrapper.java";
	}

	@Override
	public String getClassName(Class<?> clazz) {
		return clazz.getName() + "DocumentWrapper";
	}

}
