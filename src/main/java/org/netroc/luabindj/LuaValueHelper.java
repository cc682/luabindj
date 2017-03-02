package org.netroc.luabindj;

import org.luaj.vm2.LuaValue;

public class LuaValueHelper {

	public LuaValueHelper() {
	}

	/**
	 * 从LuaValue转换为java对象
	 * @param val
	 * @return
	 */
	public static Object toObject(LuaValue val) {
		if( val.isnil()) {
			return null;
		}
		if(val.isboolean()) {
			return val.toboolean();
		} else if( val.isint()) {
			return val.toint();
		} else if( val.islong()) {
			return val.tolong();
		} else if( val.isnil()) {
			return null;
		} else if( val.isnumber()) {
			return val.todouble();
		} else if( val.isstring()) {
			return val.toString();
		} else if( val.isuserdata()) {
			return val.touserdata();
		} else {
			return val;
		}
	}
	
	/**
	 * 根据cls转换val的类型
	 * @param val
	 * @param cls
	 * @return
	 */
	public static Object toObject( LuaValue val, Class<?> cls) {
		if( val.isnil()) {
			return null;
		}
		if( cls.equals(Boolean.class) || cls.equals(boolean.class)) {
			return val.toboolean();
		} else if(cls.equals(Integer.class) || cls.equals(int.class)) {
			return val.toint();
		} else if(cls.equals(Long.class) || cls.equals(long.class)) {
			return val.tolong();
		} else if(cls.equals(Double.class) || cls.equals(double.class)) {
			return val.todouble();
		} else if(cls.equals(Float.class) || cls.equals(float.class)) {
			return val.tofloat();
		} else if(cls.equals(Short.class) || cls.equals(short.class)) {
			return val.toshort();
		} else if(cls.equals(Byte.class) || cls.equals(byte.class)) {
			return val.tobyte();
		} else if(cls.equals(String.class)) {
			return val.toString();
		} else if(cls.isInstance(val)) {
			return val;
		} else {
			return val.touserdata();
		}
	}
	
	/**
	 * 从Object转换为LuaValue
	 * @param obj
	 * @return
	 */
	public static LuaValue toLuaObj( Object obj) {
		if( obj == null) {
			return LuaValue.NIL;
		}
		
		if( obj instanceof LuaValue) {
			return (LuaValue)obj;
		}
		
		Class<?> cls = obj.getClass();
		
		if( cls.equals(Integer.class) || cls.equals(int.class)) {
			return LuaValue.valueOf((Integer)obj);
		} else if( cls.equals(Long.class) || cls.equals(long.class)) {
			return LuaValue.valueOf((Long)(obj));
		} else if( cls.equals(String.class)) {
			return LuaValue.valueOf((String)(obj));
		} else if( cls.equals(Double.class) || cls.equals(double.class)) {
			return LuaValue.valueOf((Double)(obj));
		} else if( cls.equals(Float.class) || cls.equals(float.class)) {
			return LuaValue.valueOf((Float)(obj));
		} else if( cls.equals(Boolean.class) || cls.equals(boolean.class)) {
			return LuaValue.valueOf((Boolean)(obj));
		} else if( cls.equals(Byte.class) || cls.equals(byte.class)) {
			return LuaValue.valueOf((Byte)(obj));
		} else if( cls.equals(Short.class) || cls.equals(short.class)) {
			return LuaValue.valueOf((Short)(obj));
		} else if( cls.isAssignableFrom(LuaValue.class)) {
			return (LuaValue)obj;
		} else {
			return LuaBindJ.toLuaValue(obj);
		}
	}
}
