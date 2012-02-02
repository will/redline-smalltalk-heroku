/* Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution */
package st.redline.compiler;

import st.redline.PrimObjectMetaclass;
import st.redline.SmalltalkClassLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class ProgramAnalyser implements AnalyserDelegate {

	protected final Analyser analyser;
	private final ClassBytecodeWriter writer;
	private final boolean verbose;
	private Map<String, Integer> temporariesRegistry;
	private int temporariesIndex = 0;
	private int blockSequence = 0;

	ProgramAnalyser(Analyser analyser, String className, String packageName, boolean verbose) {
		this(analyser, new ClassBytecodeWriter(className, packageName, verbose), verbose);
	}

	ProgramAnalyser(Analyser analyser, ClassBytecodeWriter classBytecodeWriter, boolean verbose) {
		this.analyser = analyser;
		this.writer = classBytecodeWriter;
		this.verbose = verbose;
	}

	ClassBytecodeWriter classBytecodeWriter() {
		return writer;
	}

	int temporariesIndex() {
		return temporariesIndex;
	}

	Map<String, Integer> temporariesRegistry() {
		return temporariesRegistry;
	}

	public byte[] classBytes() {
		return writer.contents();
	}

	public void visitBegin(Program program) {
		writer.openClass();
	}

	public void visitEnd(Program program) {
		writer.closeClass();
	}

	public void visitBegin(Temporaries temporaries) {
		initializeTemporariesRegistration();
		writer.invokeContextTemporariesInit(temporaries.size());
	}

	void initializeTemporariesRegistration() {
		temporariesIndex = 0;
		temporariesRegistry = new HashMap<String, Integer>();
	}

	public void visitEnd(Temporaries temporaries) {
	}

	public void visitBegin(Statements statements) {
	}

	public void visitEnd(Statements statements) {
	}

	public void visit(Temporary temporary, String value, int line) {
		// todo.jcl - output a warning if registered twice?
		temporariesRegistry.put(value, temporariesIndex++);
	}

	public void visit(PrimaryExpression primaryExpression, int line) {
	}

	public void visit(PrimaryStatements primaryStatements, int line) {
	}

	public void visitBegin(SimpleExpression simpleExpression) {
	}

	public void visitEnd(SimpleExpression simpleExpression) {
		if (simpleExpression.isResultDuplicatedOnStack())
			writer.pushDuplicate();
		if (!simpleExpression.isResultLeftOnStack())
			writer.pop();
	}

	public void visitBegin(Cascade cascade) {
		writer.pushDuplicate();
	}

	public void visitEnd(Cascade cascade) {
		writer.pop();
	}

	public void visitBegin(UnaryExpression unaryExpression) {
	}

	public void visitEnd(UnaryExpression unaryExpression) {
	}

	public void visitBegin(BinaryExpression binaryExpression) {
	}

	public void visitEnd(BinaryExpression binaryExpression) {
	}

	public void visitBegin(KeywordExpression keywordExpression, String selector, int argumentCount, int line) {
	}

	public void visitEnd(KeywordExpression keywordExpression, String selector, int argumentCount, int line) {
		invokeObjectPerform(selector, argumentCount, line);
	}

	public void visitBegin(KeywordMessageElement keywordMessageElement, String selector, int argumentCount, int line) {
	}

	public void visitEnd(KeywordMessageElement keywordMessageElement, String selector, int argumentCount, int line) {
		invokeObjectPerform(selector, argumentCount, line);
	}

	public void visitBegin(AssignmentExpression assignmentExpression) {
	}

	public void visitEnd(AssignmentExpression assignmentExpression) {
	}

	public void visitBegin(Array array) {
	}

	public void visitEnd(Array array) {
	}

	public void visitBegin(BinarySelectorMessageElement binarySelectorMessageElement, String selector, int line) {
	}

	public void visitEnd(BinarySelectorMessageElement binarySelectorMessageElement, String selector, int line) {
	}

	public void visitBegin(Block block, int line) {
		String blockClassName = createBlockName();
		String fullBlockClassName = analyser.packageName() + (analyser.packageName() == "" ? "" : File.separator) + blockClassName;
		BlockAnalyser blockAnalyser = new BlockAnalyser(analyser, blockClassName, analyser.packageName(), verbose);
		block.analyser(blockAnalyser);
		smalltalkClassLoader().registerBlockToBeCompiled(block, fullBlockClassName);
		analyser.currentDelegate(new NoOpAnalyser(analyser));
		writer.invokeObjectCompileBlock(fullBlockClassName, line);
	}

	public SmalltalkClassLoader smalltalkClassLoader() {
		return (SmalltalkClassLoader) Thread.currentThread().getContextClassLoader();
	}

	String createBlockName() {
		blockSequence++;
		return analyser.className() + "$M" + blockSequence;
	}

	public void visitEnd(Block block, int line) {
	}

	public void visitBegin(BlockArguments blockArguments, int argumentCount) {
	}

	public void visitEnd(BlockArguments blockArguments, int argumentCount) {
	}

	public void visit(BinaryObjectDescription binaryObjectDescription) {
	}

	public void visit(UnaryObjectDescription unaryObjectDescription) {
	}

	public void visit(Identifier identifier, String value, int line) {
		if (identifier.isOnLoadSideOfExpression())
			writer.invokeVariableAt(value, line);
	}

	public void visit(Number number, String value, int index, boolean insideArray, int line) {
		writer.invokeObjectNumber(value, line);
		if (insideArray)
			writer.invokeArrayPut(index, line);
	}

	public void visit(BlockArgument blockArgument, String value, int line) {
	}

	public void visit(Self self, int line) {
		writer.visitLine(line);
		writer.pushReceiver();
	}

	public void visit(Super aSuper, int line) {
	}

	public void visit(True aTrue, int line) {
		pushPrimObjectField("TRUE", line);
	}

	public void visit(False aFalse, int line) {
		pushPrimObjectField("FALSE", line);
	}

	public void visit(Nil nil, int line) {
		pushPrimObjectField("NIL", line);
	}

	public void visit(JVM jvm, int line) {
	}

	public void visit(ArrayConstant arrayConstant, int line) {
	}

	public void visit(UnarySelector unarySelector, String selector, int line) {
		invokeObjectPerform(selector, 0, line);
	}

	public void visit(BinarySelector binarySelector, String selector, int line) {
		invokeObjectPerform(selector, 1, line);
	}

	public void visit(CharacterConstant characterConstant, String value, int index, boolean insideArray, int line) {
		writer.invokeObjectCharacter(value, line);
		if (insideArray)
			writer.invokeArrayPut(index, line);
	}

	public void visit(StringConstant stringConstant, String value, int index, boolean insideArray, int line) {
		writer.invokeObjectString(value, line);
		if (insideArray)
			writer.invokeArrayPut(index, line);
	}

	public void visit(Symbol symbol, String value, int index, boolean insideArray, int line) {
		writer.invokeObjectSymbol(value, line);
		if (insideArray)
			writer.invokeArrayPut(index, line);
	}

	public void visit(SymbolConstant symbolConstant, String value, int line) {
		writer.invokeObjectSymbol(value, line);
	}

	public void visit(UnarySelectorMessageElement unarySelectorMessageElement, String selector, int line) {
		invokeObjectPerform(selector, 0, line);
	}

	public void visit(Primitive primitive, String keyword, int line, String digits) {
	}

	void invokeObjectPerform(String selector, int argumentCount, int line) {
		writer.visitLine(line);
		writer.invokeObjectPerform(selector, argumentCount);
	}

	void pushPrimObjectField(String field, int line) {
		writer.visitLine(line);
		writer.pushObjectStaticField(field);
	}
}
