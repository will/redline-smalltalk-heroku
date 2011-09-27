/* Redline Smalltalk, Copyright (c) James C. Ladd. All rights reserved. See LICENSE in the root of this distribution */
package st.redline;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public abstract class ProtoObjectData {

	private ProtoObject cls;
	protected Map<String, ProtoObject> variables;

	abstract void javaValue(Object value);
	abstract Object javaValue();
	abstract void superclass(ProtoObject superclass);
	abstract ProtoObject superclass();
	abstract ProtoMethod methodAt(String selector);
	abstract void methodAtPut(String selector, ProtoMethod method);
	abstract boolean isClass();
	abstract String packageAt(String name);
	abstract void packageAtPut(String name, String packageName);
	abstract void addVariableNamed(String name);
	abstract boolean hasVariableNamed(String name);
	abstract void category(String name);
	abstract String category();
	abstract void initializeVariables(ProtoObject instance, ProtoObjectData instanceData);

	public static ProtoObjectData classData() {
		return new ClassData();
	}

	public static ProtoObjectData instanceData() {
		return new InstanceData();
	}

	protected void cls(ProtoObject cls) {
		this.cls = cls;
	}

	protected ProtoObject cls() {
		return cls;
	}

	protected ProtoObject variableAt(String name) {
		return variables != null ? variables.get(name) : null;
	}

	protected ProtoObject variableAtPut(String name, ProtoObject value) {
		return variables.put(name, value);
	}

	private static class InstanceData extends ProtoObjectData {

		private Object javaValue;

		protected void javaValue(Object value) {
			javaValue = value;
		}

		protected Object javaValue() {
			return javaValue;
		}

		protected boolean isClass() {
			return false;
		}

		protected void superclass(ProtoObject superclass) {
			throw new IllegalStateException("An instance can't have a superclass.");
		}

		protected ProtoObject superclass() {
			throw new IllegalStateException("An instance doesn't have a superclass.");
		}

		protected ProtoMethod methodAt(String selector) {
			throw new IllegalStateException("An instance doesn't have a method dictionary.");
		}

		protected void methodAtPut(String selector, ProtoMethod method) {
			throw new IllegalStateException("An instance can't have a method dictionary.");
		}

		protected String packageAt(String name) {
			throw new IllegalStateException("An instance can't have a packages.");
		}

		protected void packageAtPut(String name, String packageName) {
			throw new IllegalStateException("An instance can't have a packages.");
		}

		protected void addVariableNamed(String name) {
			throw new IllegalStateException("An instance can't have a variable name dictionary.");
		}

		protected boolean hasVariableNamed(String name) {
			throw new IllegalStateException("An instance doesn't have a variable name dictionary.");
		}

		protected void category(String name) {
			throw new IllegalStateException("An instance doesn't have a Category.");
		}

		protected String category() {
			throw new IllegalStateException("An instance doesn't have a Category.");
		}

		protected void initializeVariables(ProtoObject instance, ProtoObjectData instanceData) {
			throw new IllegalStateException("An instance doesn't initialize instance variables.");
		}
	}

	private static class ClassData extends ProtoObjectData {

		private ProtoObject superclass;
		private Map<String, ProtoMethod> methods = new HashMap<String, ProtoMethod>();
		private Map<String, String> packages;
		private Map<String, String> variableNames;
		private boolean initialized = false;
		private String category;

		protected void javaValue(Object value) {
			throw new IllegalStateException("A Class can't have a javaValue.");
		}

		protected Object javaValue() {
			throw new IllegalStateException("A Class doesn't have a javaValue.");
		}

		protected boolean isClass() {
			return true;
		}

		protected void superclass(ProtoObject superclass) {
			this.superclass = superclass;
		}

		protected ProtoObject superclass() {
			return superclass;
		}

		protected ProtoMethod methodAt(String selector) {
			return methods.get(selector);
		}

		protected void methodAtPut(String selector, ProtoMethod method) {
			// System.out.println("methodAtPut() " + selector + " " + method);
			methods.put(selector, method);
		}

		protected String packageAt(String name) {
			return packages != null ? packages.get(name) : null;
		}

		protected void packageAtPut(String name, String packageName) {
			if (packages == null)
				packages = new Hashtable<String, String>();
			packages.put(name, packageName);
		}

		protected void addVariableNamed(String name) {
			if (hasVariableNamed(name))
				throw new IllegalStateException("Variable name '" + name + " already defined.");
			if (variableNames == null)
				variableNames = new HashMap<String, String>();
			variableNames.put(name, name);
		}

		protected boolean hasVariableNamed(String name) {
			if (variableNames != null && variableNames.containsKey(name))
				return true;
			if (superclass != null)
				return superclass.hasVariableNamed(name);
			return false;
		}

		protected void initializeVariables(ProtoObject instance, ProtoObjectData instanceData) {
			if (!initialized && variableNames != null && !variableNames.isEmpty()) {
				if (instanceData.variables == null)
					instanceData.variables = new HashMap<String, ProtoObject>();
				for (Map.Entry<String, String> entry : variableNames.entrySet()) {
					instanceData.variables.put(entry.getKey(), ProtoObject.instanceOfUndefinedObject);
				}
			}
			initialized = true;
			if (superclass != null)
				superclass.initializeVariables(instance);
		}

		protected void category(String category) {
			this.category  = category;
		}

		protected String category() {
			return category;
		}
	}
}
