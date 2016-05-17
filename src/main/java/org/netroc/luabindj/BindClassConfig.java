package org.netroc.luabindj;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.luaj.vm2.lib.LibFunction;

public class BindClassConfig {
	
	/**
	 * 类名
	 */
	private String mLibName;
	
	/**
	 * 类引用
	 */
	private Class<?> mClass;
	
	/**
	 * 所有的导出field字段
	 */
	private Map<String, Field> mMapFields;
	
	/**
	 * 所有的导出方法
	 */
	private Map<String, Method> mMapMethods;
	
	/**
	 * 所有导出方法的调用桥
	 */
	private Map<String, LibFunction> mMethodBridges;
	
	/**
	 * 元表的index桥
	 */
	private MetaIndexBridge mMetaIndexBridge;
	
	private MetaNewIndexBridge mMetaNewIndexBridge;
	
	/**
	 * 是否导出全部的字段和方法
	 */
	private boolean mExportAll = false;

	/**
	 * 
	 * @param strClassName 类名
	 * @param objClass 类的class对象
	 */
	public BindClassConfig(String strClassName, Class<?> objClass, boolean blExportAll) {
		mLibName = strClassName;
		mClass = objClass;
		mExportAll = blExportAll;
		
		initFromClass();
	}

	/**
	 * 获得类名
	 * @return
	 */
	public String getLibName() {
		return mLibName;
	}
	
	/**
	 * 获得类对象
	 * @return
	 */
	public Class<?> getClassObject() {
		return mClass;
	}
	
	/**
	 * 从已设置的class对象进行初始化
	 */
	protected void initFromClass() {
		doFields();
		doMethod();
		mMetaIndexBridge = new MetaIndexBridge(this);
		mMetaNewIndexBridge = new MetaNewIndexBridge(this);
	}
	
	/**
	 * 处理fields
	 */
	private void doFields() {
		mMapFields = new HashMap<String, Field>();
		
		Field[] arrField = mClass.getDeclaredFields();
		for( Field f : arrField) {
			//无标注的不处理
			if( f.getAnnotation(LuaExport.class) == null && mExportAll == false) {
				continue;
			}
			
			mMapFields.put(f.getName(), f);
		}
	}
	
	/**
	 * 处理方法
	 */
	private void doMethod() {
		mMapMethods = new HashMap<String, Method>();
		mMethodBridges = new TreeMap<String, LibFunction>();
		
		Method[] arrMethod = mClass.getDeclaredMethods();
		for( Method m : arrMethod) {
			//无标注的不处理
			if( m.getAnnotation(LuaExport.class) == null && mExportAll == false) {
				continue;
			}
			
			mMapMethods.put(m.getName(), m);
			
			int nCount = m.getParameterCount();
			LibFunction bridge;
			if( !Modifier.isStatic(m.getModifiers())) {
				nCount++;
			}
			switch( nCount) {
			case 0:
				bridge = new CallBridge0(m);
				break;
			case 1:
				bridge = new CallBridge1(m);
				break;
			case 2:
				bridge = new CallBridge2(m);
				break;
			case 3:
				bridge = new CallBridge3(m);
				break;
			default:
				bridge = new CallBridge(m);
				break;
			}
			mMethodBridges.put(m.getName(), bridge);
		}
	}
	
	/**
	 * 通过名字获得field
	 * @param strName
	 * @return
	 */
	public Field getField( String strName) {
		return mMapFields.get(strName);
	}
	
	/**
	 * 通过名字获得method
	 * @param strMethod
	 * @return
	 */
	public Method getMethod( String strMethod) {
		return mMapMethods.get(strMethod);
	}
	
	/**
	 * 获得field map
	 * @return
	 */
	public Map<String, Field> getFieldMap() {
		return mMapFields;
	}
	
	/**
	 * 获得Method的map
	 * @return
	 */
	public Map<String, Method> getMethodMap() {
		return mMapMethods;
	}
	
	/**
	 * 获得Method的bridge map
	 * @return
	 */
	public Map<String, LibFunction> getLibFunctionMap() {
		return mMethodBridges;
	}
	
	/**
	 * 获得某个方法的bridge
	 * @param strName
	 * @return
	 */
	public LibFunction getFunctonBridge(String strName) {
		return mMethodBridges.get(strName);
	}
	
	/**
	 * 获得元表的__index桥
	 * @return
	 */
	public MetaIndexBridge getMetaIndexBridge() {
		return mMetaIndexBridge;
	}

	/**
	 * 获得元表的__newindex桥
	 * @return
	 */
	public MetaNewIndexBridge getMetaNewIndexBridge() {
		return mMetaNewIndexBridge;
	}
}
