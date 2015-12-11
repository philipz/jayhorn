/**
 * 
 */
package soottocfg.soot.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Jimple;
import soot.tagkit.AbstractHost;
import soot.tagkit.SourceFileTag;
import soot.tagkit.Tag;
import soottocfg.cfg.Program;
import soottocfg.cfg.SourceLocation;
import soottocfg.cfg.Variable;
import soottocfg.soot.SootRunner;
import soottocfg.soot.memory_model.MemoryModel;
import soottocfg.soot.memory_model.NewMemoryModel;
import soottocfg.soot.memory_model.SimpleBurstallBornatModel;

/**
 * @author schaef
 *
 */
public enum SootTranslationHelpers {
	INSTANCE;

	public static SootTranslationHelpers v() {
		return INSTANCE;
	}

	private final Map<soot.Type, Variable> typeVariables = new HashMap<soot.Type, Variable>();

	private SootMethod currentMethod;
	private SootClass currentClass;
	private String currentSourceFileName;

	private MemoryModel memoryModel;
	private Program program;

	public void reset() {
		typeVariables.clear();
		currentMethod = null;
		currentClass = null;
		currentSourceFileName = null;
		memoryModel = null;
		program = null;
	}

	public void setProgram(Program p) {
		this.program = p;
	}

	public Program getProgram() {
		return this.program;
	}

	public SootClass getAssertionClass() {
		return Scene.v().getSootClass(SootRunner.assertionClassName);
	}

	public SootMethod getAssertMethod() {
		SootClass assertionClass = Scene.v().getSootClass(SootRunner.assertionClassName);
		return assertionClass.getMethodByName(SootRunner.assertionProcedureName);
	}

	public SootField getExceptionGlobal() {
		SootClass assertionClass = Scene.v().getSootClass(SootRunner.assertionClassName);
		return assertionClass.getFieldByName(SootRunner.exceptionLocalName);
	}

	public Value getExceptionGlobalRef() {
		return Jimple.v().newStaticFieldRef(getExceptionGlobal().makeRef());
	}

	public Unit makeAssertion(Value cond) {
		List<Value> args = new LinkedList<Value>();
		args.add(cond);
		return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(getAssertMethod().makeRef(), args));
	}

	public SourceLocation getSourceLocation(Unit u) {
		int lineNumber = u.getJavaSourceStartLineNumber();

		if (lineNumber < 0) {
			lineNumber = SootTranslationHelpers.v().getJavaSourceLine(SootTranslationHelpers.v().getCurrentMethod());
		}
		return new SourceLocation(this.currentSourceFileName, lineNumber);
	}
	
	public SourceLocation getSourceLocation(SootMethod sm) {
		int lineNumber = sm.getJavaSourceStartLineNumber();

		if (lineNumber < 0) {
			lineNumber = SootTranslationHelpers.v().getJavaSourceLine(SootTranslationHelpers.v().getCurrentMethod());
		}
		return new SourceLocation(this.currentSourceFileName, lineNumber);
	}

	protected boolean debug = true;

	public MemoryModel getMemoryModel() {
		if (this.memoryModel == null) {
			// TODO:
			if (debug) {
				this.memoryModel = new NewMemoryModel();
			} else {
				this.memoryModel = new SimpleBurstallBornatModel();
			}
		}
		return this.memoryModel;
	}

	public Variable lookupTypeVariable(soot.Type t) {
		if (!typeVariables.containsKey(t)) {
			Variable var = new Variable(createTypeName(t), this.memoryModel.lookupType(t));
			typeVariables.put(t, var);
		}
		return typeVariables.get(t);
	}

	private String createTypeName(soot.Type t) {
		if (t instanceof RefType) {
			RefType rt = (RefType) t;
			return rt.getClassName();
		} else {
			return "HansWurst";
			// TODO: handle this case ...
			// throw new UnsupportedOperationException(
			// "Did not expect type to be " + (null == t ? "null" :
			// t.getClass().getSimpleName()));
		}
	}

	public SootClass getCurrentClass() {
		return currentClass;
	}

	public void setCurrentClass(SootClass currentClass) {
		this.currentClass = currentClass;
		for (Tag tag : this.currentClass.getTags()) {
			if (tag instanceof SourceFileTag) {
				SourceFileTag t = (SourceFileTag) tag;
				currentSourceFileName = t.getAbsolutePath();
			} else {
				// System.err.println("Unprocessed tag " + tag.getClass() + " -
				// " + tag);
			}
		}
	}

	public SootMethod getCurrentMethod() {
		return currentMethod;
	}

	public void setCurrentMethod(SootMethod currentMethod) {
		this.currentMethod = currentMethod;
	}

	public String getCurrentSourceFileName() {
		return this.currentSourceFileName;
	}

	public int getJavaSourceLine(AbstractHost ah) {
		return ah.getJavaSourceStartLineNumber();
	}
}
