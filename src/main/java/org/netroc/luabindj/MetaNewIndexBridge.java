package org.netroc.luabindj;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class MetaNewIndexBridge extends VarArgFunction {
	private BindClassConfig mCfg;

	public MetaNewIndexBridge(BindClassConfig cfg) {
		mCfg = cfg;
	}
	
	@Override
	public Varargs invoke(Varargs value) {
		LuaValue table = value.arg(1);
		LuaValue key = value.arg(2);
		LuaValue val = value.arg(3);
		
		if( !table.isuserdata()) {
			table.rawset(key.toString(), val);
			return val;
		}
		
		Object mObj = table.touserdata();
		
		Field f = mCfg.getField(key.toString());
		Method m = null;
		if( f == null) {
			m = mCfg.getMethod(key.toString());
		}
		
		if( f == null && m == null) {
			//非导出字段,返回null
			table.rawset(key.toString(), val);
			return val;
		}
		
		//没找到,从obj取
		if( f != null) {
			try {
				Class<?> cls = f.getType();
				f.setAccessible(true);
				if( cls.equals(Integer.class) || cls.equals(int.class)) {
					f.set(mObj, val.toint());
				} else if( cls.equals(Long.class) || cls.equals(long.class)) {
					f.set(mObj, val.tolong());
				} else if( cls.equals(String.class)) {
					f.set(mObj, val.toString());
				} else if( cls.equals(Double.class) || cls.equals(double.class)) {
					f.set(mObj, val.todouble());
				} else if( cls.equals(Float.class) || cls.equals(float.class)) {
					f.set(mObj, val.tofloat());
				} else if( cls.equals(Boolean.class) || cls.equals(boolean.class)) {
					f.set(mObj, val.toboolean());
				} else if( cls.equals(Byte.class) || cls.equals(byte.class)) {
					f.set(mObj, val.tobyte());
				} else if( cls.equals(Short.class) || cls.equals(short.class)) {
					f.set(mObj, val.toshort());
				} else {
					//非基础类型,直接按Object处理
					f.set(mObj, val.touserdata());
				}
				
				return value;
			} catch (Exception e) {
				return LuaValue.error("Call java method fail:" + e.getMessage());
			}
		}
		
		if( m != null) {
			return LuaValue.error("Can not set a java method value.");
		}
		
		return LuaValue.NIL;
	}

}
