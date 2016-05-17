package com.idoer.luabindj;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class MetaIndexBridge extends TwoArgFunction {
	private BindClassConfig mCfg;

	public MetaIndexBridge(BindClassConfig cfg) {
		mCfg = cfg;
	}

	@Override
	public LuaValue call(LuaValue table, LuaValue key) {
		if( !table.isuserdata()) {
			return table.rawget(key);
		}
		
		Object mObj = table.touserdata();
		
		Field f = mCfg.getField(key.toString());
		Method m = null;
		if( f == null) {
			m = mCfg.getMethod(key.toString());
		}
		
		if( f == null && m == null) {
			//非导出字段,返回null
			return LuaValue.NIL;
		}
		
		//没找到,从obj取
		if( f != null) {
			try {
				Class<?> cls = f.getType();
				f.setAccessible(true);
				//非基础类型,直接按Object处理
				Object obj = f.get(mObj);
				if( obj == null) {
					return LuaValue.NIL;
				}
				if(cls.equals(Integer.class)) {
					return LuaValue.valueOf(((Integer)obj).intValue());
				} else if( cls.equals(Long.class)) {
					return LuaValue.valueOf(((Long)obj).longValue());
				} else if( cls.equals(String.class)) {
					return LuaValue.valueOf(((String)obj));
				} else if( cls.equals(Double.class)) {
					return LuaValue.valueOf(((Double)obj).doubleValue());
				} else if( cls.equals(Float.class)) {
					return LuaValue.valueOf(((Float)obj).floatValue());
				} else if( cls.equals(Boolean.class)) {
					return LuaValue.valueOf(((Boolean)obj).booleanValue());
				} else if( cls.equals(Byte.class)) {
					return LuaValue.valueOf(((Byte)obj).byteValue());
				} else if( cls.equals(Short.class)) {
					return LuaValue.valueOf(((Short)obj).shortValue());
				} else {
					//非基础类型,直接按Object处理
					return LuaBindJ.toLuaValue(obj);
				}
			} catch (Exception e) {
				return LuaValue.error("Call java method fail:" + e.getMessage());
			}
		}
		
		if( m != null) {
			return mCfg.getFunctonBridge(m.getName());
		}
		
		return LuaValue.NIL;
	}

}
