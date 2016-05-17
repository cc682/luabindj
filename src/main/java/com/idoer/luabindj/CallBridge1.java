package com.idoer.luabindj;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

public class CallBridge1 extends OneArgFunction {
	private Method mMethod;
	private boolean mIsStatic;

	public CallBridge1(Method method) {
		mMethod = method;
		mIsStatic = Modifier.isStatic(mMethod.getModifiers());
	}

	@Override
	public LuaValue call(LuaValue arg0) {
		//先call
		Object obj = null;
		
		try {
			mMethod.setAccessible(true);
			Object rtn;
			if( mIsStatic) {
				//静态方法第一个参数不是对象
				obj = LuaValueHelper.toObject(arg0, mMethod.getParameters()[0].getType());
				rtn = mMethod.invoke(null, obj);
			} else {
				obj = arg0.touserdata();
				rtn = mMethod.invoke(obj);
			}
			return LuaValueHelper.toLuaObj(rtn);
		} catch (Exception e) {
			return LuaValue.error("Call java method fail:" + mMethod.getName() + ".msg:" + e.toString());
		}
	}

}
