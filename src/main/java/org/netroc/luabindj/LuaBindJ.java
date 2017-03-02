package org.netroc.luabindj;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

public class LuaBindJ {
	
	/**
	 * 保存已注册的类
	 */
	private static Map<String, BindClassConfig> S_CLASS_REG = new HashMap<String, BindClassConfig>();
	
	/**
	 * lua的state
	 */
	private Globals mLuaState;

	public LuaBindJ() {
	}

	/**
	 * 注册一个导出到lua的类
	 * @param cls
	 */
	public static void registerClass(Class<?> cls, String strLibName) {
		registerClass( cls, strLibName, false);
	}
	
	/**
	 * 注册一个导出到lua的类,如果blExportAll为true,则不管标注,导出所有的字段和方法
	 * @param cls
	 * @param strLibName
	 * @param blExportAll
	 */
	public static void registerClass(Class<?> cls, String strLibName, boolean blExportAll) {
		synchronized (S_CLASS_REG) {
			if( S_CLASS_REG.containsKey(cls.getName())) {
				return;
			} else {
				BindClassConfig cfg = new BindClassConfig(strLibName, cls, blExportAll);
				S_CLASS_REG.put(cls.getName(), cfg);
			}
		}
	}
	
	/**
	 * 取消一个导出类的注册
	 * @param cls
	 */
	public static void unregisterClass(Class<?> cls) {
		synchronized (S_CLASS_REG) {
			S_CLASS_REG.remove(cls.getName());
		}
	}
	
	/**
	 * 获得类的绑定配置
	 * @param cls
	 * @return
	 */
	public static BindClassConfig getBindClass(Class<?> cls) {
		synchronized (S_CLASS_REG) {
			return S_CLASS_REG.get(cls.getName());
		}
	}
	
	/**
	 * 关闭LuaState
	 */
	public void close() {
		mLuaState = null;
	}
	
	/**
	 * 初始化Lua state
	 */
	public void open() {
		close();
		mLuaState = new Globals();
		LoadState.install(mLuaState);
		LuaC.install(mLuaState);
		
		initStaticMethods();
		
		//补一下_G.loadstring
		mLuaState.set("loadstring", new OneArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg0) {
				return LuaBindJ.this.mLuaState.load(arg0.toString());
			}
		});
	}
	
	/**
	 * 将已注册的class中的静态方法注册到luaState中
	 */
	private void initStaticMethods() {
		for( Entry<String, BindClassConfig> ent : S_CLASS_REG.entrySet()) {
			LuaValue lib = LuaValue.tableOf();
			for( Entry<String, Method> entM : ent.getValue().getMethodMap().entrySet()) {
				if( Modifier.isStatic(entM.getValue().getModifiers())) {
					//静态方法,注册掉
					lib.set(entM.getValue().getName(), ent.getValue().getFunctonBridge(entM.getValue().getName()));
				}
			}
			mLuaState.set(ent.getValue().getLibName(), lib);
		}
	}
	
	/**
	 * 获得lua state
	 * @return
	 */
	public Globals getLuaState() {
		return mLuaState;
	}
	
	/**
	 * 打开Luanda标准库.包含:
	 * basic
	 * coroutine
	 * package
	 * string
	 * table
	 * math
	 * 
	 */
	public void openLibs() {
		mLuaState.load(new JseBaseLib());
		mLuaState.load(new PackageLib());
		mLuaState.load(new Bit32Lib());
		mLuaState.load(new TableLib());
		mLuaState.load(new StringLib());
		mLuaState.load(new JseMathLib());
		mLuaState.load(new CoroutineLib());
		mLuaState.load(new MyOsLib());
		mLuaState.load(new DebugLib());

		
		//补一下table.getn
		LuaValue val = mLuaState.get("table");
		val.set("getn", new OneArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg0) {
				if ( !(arg0 instanceof LuaTable)) {
					throw new LuaError("table expected for table.getn");
				}
				LuaTable table = (LuaTable)arg0;
				return table.len();
			}
		});
	}
	
	/**
	 * 执行一段脚本
	 * @param strScript
	 * @return
	 */
	public LuaValue runScript( String strScript) {
		LuaValue val = mLuaState.load(strScript);
		return val.call();
	}
	
	/**
	 * 执行一个脚本文件
	 * @param strPath
	 * @return
	 */
	public LuaValue runFile( String strPath) {
		LuaValue val = mLuaState.loadfile(strPath);
		return val.call();
	}
	
	/**
	 * 将一个对象转换为LuaValue
	 * @param obj
	 * @return
	 */
	public static LuaValue toLuaValue(Object obj) {
		if( obj == null) {
			return LuaValue.NIL;
		}
		
		if( obj instanceof LuaValue) {
			return (LuaValue)obj;
		}
		
		//如果是数组
		if( obj.getClass().isArray()) {
			int nCount = Array.getLength(obj);
			LuaTable valArray = LuaValue.tableOf();
			for( int i = 0; i < nCount; i++) {
				Object objData = Array.get(obj, i);
				valArray.set(i+1, toLuaValue(objData));
			}
			return valArray;
		}
		
		BindClassConfig cfg = LuaBindJ.getBindClass(obj.getClass());
		if( cfg == null) {
			//非注册类,直接转换成userdata
			LuaValue val = LuaValue.userdataOf(obj);
			return val;
		}
		
		//注册类,通过bridge转换
		MetaIndexBridge index = cfg.getMetaIndexBridge();
		MetaNewIndexBridge newIndex = cfg.getMetaNewIndexBridge();
		
		//设置元表
		LuaUserdata val = LuaValue.userdataOf(obj);
		LuaTable meta = LuaTable.tableOf();
		meta.set("__index", index);
		meta.set("__newindex", newIndex);
		val.setmetatable(meta);
		
		return val;
	}
	
	public static LuaValue toLuaValue( int n) {
		return LuaValue.valueOf(n);
	}
	
	public static LuaValue toLuaValue( long l) {
		return LuaInteger.valueOf(l);
	}
	
	public static LuaValue toLuaValue(String s) {
		return LuaValue.valueOf(s);
	}
	
	public static LuaValue toLuaValue( double d) {
		return LuaValue.valueOf(d);
	}
	
	public static LuaValue toLuaValue(short s) {
		return LuaValue.valueOf(s);
	}
	
	public static LuaValue toLuaValue(byte by) {
		return LuaValue.valueOf(by);
	}
	
	/**
	 * 增加一个Lua全局变量
	 * @param strName
	 * @param obj
	 */
	public void addGlobal( String strName, Object obj) {
		mLuaState.set(strName, LuaValueHelper.toLuaObj(obj));
	}
	
	/**
	 * 增加一个全局变量
	 * @param strName
	 * @param val
	 */
	public void addGlobal( String strName, LuaValue val) {
		mLuaState.set(strName, val);
	}
	
	/**
	 * 调用lua方法
	 * @param strName
	 * @param args
	 * @return
	 */
	public Object call(String strName, Object ...args) {
		try {
			if( args == null || args.length == 0) {
				LuaValue pfn = mLuaState.get(strName);
				return LuaValueHelper.toObject(pfn.call());
			} else {
				LuaValue arrArgs[] = new LuaValue[args.length];
				for( int i = 0; i < args.length; i++) {
					LuaValue val = LuaValueHelper.toLuaObj(args[i]);
					arrArgs[i] = val;
				}
				LuaValue pfn = mLuaState.get(strName);
				Varargs rtn = pfn.invoke(arrArgs);
				return LuaValueHelper.toObject(rtn.arg1());
			}
		} catch (Exception e) {
			return null;
		}
	}
}
