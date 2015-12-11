/**
 * 
 */
package soottocfg.soot.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.VoidType;
import soot.jimple.ParameterRef;
import soottocfg.cfg.SourceLocation;
import soottocfg.cfg.Variable;
import soottocfg.cfg.expression.Expression;
import soottocfg.cfg.expression.IdentifierExpression;
import soottocfg.cfg.method.CfgBlock;
import soottocfg.cfg.method.Method;
import soottocfg.cfg.type.Type;

/**
 * @author schaef
 *
 */
public class MethodInfo {

	private final String sourceFileName;

	private static final String parameterPrefix = "$in_";
	private static final String returnVariableName = "$ret_";
	private static final String thisVariableName = "$this_";

	private final SootMethod sootMethod;

	private CfgBlock source = null, sink = null;
	private Map<Unit, CfgBlock> unitToBlockMap = new HashMap<Unit, CfgBlock>();
	private Map<Local, Variable> localsMap = new HashMap<Local, Variable>();
	private List<Variable> parameterList = new LinkedList<Variable>();

	private Variable returnVariable, thisVariable;

	private final Set<Variable> freshLocals = new LinkedHashSet<Variable>();

	private SourceLocation methodLoc = null;
	
	public MethodInfo(SootMethod sm, String sourceFileName) {
		sootMethod = sm;
		methodLoc = SootTranslationHelpers.v().getSourceLocation(sm);
		sink = new CfgBlock(getMethod());
		this.sourceFileName = sourceFileName;

		// create a return variable if the method does not
		// return void.
		if (!(sm.getReturnType() instanceof VoidType)) {
			this.returnVariable = new Variable(returnVariableName,
					SootTranslationHelpers.v().getMemoryModel().lookupType(sm.getReturnType()));
		} else {
			this.returnVariable = null;
		}

		// create a return variable for exceptional returns.
		// for (Local l : sm.getActiveBody().getLocals()) {
		// if (l.getName().equals(ExceptionTransformer.getExceptionLocalName()))
		// {
		// this.exceptionalReturnVariable = lookupLocalVariable(l);
		// break;
		// }
		// }
		// assert (exceptionalReturnVariable!=null);

		// If the method is not static, create a this-variable which is
		// passed as the first parameter to the method.
		if (!sm.isStatic()) {
			this.thisVariable = new Variable(thisVariableName,
					SootTranslationHelpers.v().getMemoryModel().lookupType(sm.getDeclaringClass().getType()));
		}

		for (int i = 0; i < sm.getParameterCount(); i++) {
			String param_name = parameterPrefix + i;
			parameterList.add(new Variable(param_name,
					SootTranslationHelpers.v().getMemoryModel().lookupType(sm.getParameterType(i))));

			// assumeParameterType(id, sm.getParameterType(i));

		}
	}

	public Method getMethod() {
		return SootTranslationHelpers.v().getProgram().loopupMethod(this.sootMethod.getSignature());
	}

	/**
	 * Called after the method has been translated. Looks up the corresponding
	 * Method object in Program and fills in all the information. Should only be
	 * called once per method.
	 */
	public void finalizeAndAddToProgram() {
		Method m = SootTranslationHelpers.v().getProgram().loopupMethod(this.sootMethod.getSignature());
		Collection<Variable> locals = new LinkedHashSet<Variable>();
		locals.addAll(this.localsMap.values());
		locals.addAll(this.freshLocals);
		m.initialize(this.thisVariable, this.returnVariable, this.parameterList, locals, source,
				sootMethod.isEntryMethod());
		CfgBlock uniqueSink = m.findOrCreateUniqueSink();
		if (sink != uniqueSink) {
			System.err.println("Something strange with the CFG. More than one sink found for " + m.getMethodName());
		}
		sink = uniqueSink;
	}

	public String getSourceFileName() {
		return this.sourceFileName;
	}

	public Expression getReturnVariable() {		
		return new IdentifierExpression(methodLoc, this.returnVariable);
	}

	// public Expression getExceptionVariable() {
	// return new IdentifierExpression(this.exceptionalReturnVariable);
	// }

	public Expression getThisVariable() {
		return new IdentifierExpression(methodLoc, this.thisVariable);
	}

	public Expression lookupParameterRef(ParameterRef arg0) {
		return new IdentifierExpression(methodLoc, parameterList.get(arg0.getIndex()));
	}

	public Variable lookupLocalVariable(Local arg0) {
		if (!localsMap.containsKey(arg0)) {
			localsMap.put(arg0, createLocalVariable(arg0));
		}
		return localsMap.get(arg0);
	}

	public Expression lookupLocal(Local arg0) {
		return new IdentifierExpression(methodLoc, lookupLocalVariable(arg0));
	}

	private Variable createLocalVariable(Local arg0) {
		return new Variable(arg0.getName(), SootTranslationHelpers.v().getMemoryModel().lookupType(arg0.getType()),
				false, false);
	}

	public Variable createFreshLocal(String prefix, Type t, boolean constant, boolean unique) {
		Variable v = new Variable(prefix + this.freshLocals.size(), t, constant, unique);
		this.freshLocals.add(v);
		return v;
	}

	public CfgBlock lookupCfgBlock(Unit u) {
		if (!unitToBlockMap.containsKey(u)) {
			unitToBlockMap.put(u, new CfgBlock(getMethod()));
		}
		return unitToBlockMap.get(u);
	}

	public CfgBlock findBlock(Unit u) {
		return unitToBlockMap.get(u);
	}

	public CfgBlock getSource() {
		return this.source;
	}

	public void setSource(CfgBlock source) {
		this.source = source;
	}

	public CfgBlock getSink() {
		return sink;
	}

}
