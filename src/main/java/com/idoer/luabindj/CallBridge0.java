package com.idoer.luabindj;

import java.lang.reflect.Method;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class CallBridge0 extends ZeroArgFunction {
	private Method mMethod;

	public CallBridge0(Method method) {
		mMethod = method;
	}

	@Override
	public LuaValue call() {
		//先call
		
		try {
			mMethod.setAccessible(true);
			Object rtn = mMethod.invoke(null);
			return LuaValueHelper.toLuaObj(rtn);
		} catch (Exception e) {
			return LuaValue.error("Call java method fail:" + mMethod.getName() + ".msg:" + e.toString());
		}
	}

}
