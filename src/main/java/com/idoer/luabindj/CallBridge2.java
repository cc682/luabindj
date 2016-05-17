package com.idoer.luabindj;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class CallBridge2 extends TwoArgFunction {
	private Method mMethod;
	private boolean mIsStatic;

	public CallBridge2(Method method) {
		mMethod = method;
		mIsStatic = Modifier.isStatic(mMethod.getModifiers());
	}

	@Override
	public LuaValue call(LuaValue arg0, LuaValue arg1) {
		//先call
		Object obj;
		Object obj2;
		
		try {
			mMethod.setAccessible(true);
			Object rtn;
			if( mIsStatic) {
				//静态方法第一个参数不是对象
				obj = LuaValueHelper.toObject(arg0, mMethod.getParameters()[0].getType());
				obj2 = LuaValueHelper.toObject(arg1, mMethod.getParameters()[1].getType());
				rtn = mMethod.invoke(null, obj, obj2);
			} else {
				obj = arg0.touserdata();
				obj2 = LuaValueHelper.toObject(arg1, mMethod.getParameters()[0].getType());
				rtn = mMethod.invoke(obj, obj2);
			}
			return LuaValueHelper.toLuaObj(rtn);
		} catch (Exception e) {
			return LuaValue.error("Call java method fail:" + mMethod.getName() + ".msg:" + e.toString());
		}
	}

}
