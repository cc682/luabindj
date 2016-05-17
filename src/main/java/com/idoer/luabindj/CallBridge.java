package com.idoer.luabindj;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class CallBridge extends VarArgFunction {
	private Method mMethod;
	private boolean mIsStatic;
	
	public CallBridge(Method method) {
		mMethod = method;
		mIsStatic = Modifier.isStatic(mMethod.getModifiers());
	}
	
	@Override
	public Varargs invoke(Varargs arg0) {
		int n = mMethod.getParameterCount();
		Object[] obj = new Object[n];
		
		Object mObj = null;
		if( !mIsStatic) {
			LuaValue val = arg0.arg(1);
			mObj = val.touserdata();
		}
		
		int nCurrent = 0;
		int nParamCount = mIsStatic ? 1 : 2;
		while( nCurrent < n) {
			LuaValue val = arg0.arg(nParamCount + nCurrent);
			obj[nCurrent] = LuaValueHelper.toObject(val, mMethod.getParameters()[nCurrent].getType());
			nCurrent++;
		}
		
		try {
			mMethod.setAccessible(true);
			Object rtn;
			if( mIsStatic) {
				//静态方法第一个参数不是对象
				rtn = mMethod.invoke(mObj, obj);
			} else {
				rtn = mMethod.invoke(mObj, obj);
			}
			return LuaValueHelper.toLuaObj(rtn);
		} catch (Exception e) {
			e.printStackTrace();
			return LuaValue.error("Call java method fail:" + mMethod.getName() + ".msg:" + e.toString());
		}
	}

}
