package org.netroc.luabindj.test;

import org.luaj.vm2.LuaValue;
import org.netroc.luabindj.LuaBindJ;
import org.netroc.luabindj.LuaExport;

public class MainClass {
	static class TestClass{
		@LuaExport
		public Integer n = 100;
		
		@LuaExport
		public int nn = 200;

		@LuaExport
		public Float ff = 300.20f;

		@LuaExport
		public TestClass child;

		public int add(int data) {
			return n + data;
		}

		@LuaExport
		public double lParam( int a, float b, double c, Integer d, int e) {
			return a+b+c+d+e;
		}

		@LuaExport
		public TestClass create() {
			return new TestClass();
		}
		
		@LuaExport
		public static String staticFunc() {
			return "staticFunc111";
		}

		@LuaExport
		public TestClass[] getArray() {
			TestClass[] arr = new TestClass[3];
			arr[0] = new TestClass();
			arr[0].n = 11111;
			arr[1] = new TestClass();
			arr[2] = new TestClass();
			return arr;
		}
	}
	
	public static void main(String[] args) {
		LuaBindJ lua = new LuaBindJ();
		
		LuaBindJ.registerClass(TestClass.class, "TestClass", true);
		
		lua.open();
		lua.openLibs();
		
		TestClass t = new TestClass();
		LuaValue val = LuaBindJ.toLuaValue(t);
		
		lua.addGlobal("obj", val);
		
		lua.runScript("oo={aa=1,bb=2}");
		
		lua.runScript("function pp( p1, p2) \n print(p1..p2) \n return p1..p2\n end");
		try {
			lua.call("pp", "13213", "aadsada");
		} catch (Exception e) {
			e.printStackTrace();
		}
		lua.runScript("print(obj:add(3))");
		
		//lua.runScript("obj.child = obj:create() \n print (obj.child.n..0.3)");
		//lua.runScript("print(obj.child.nn)");
		
		//lua.runScript("print(TestClass.staticFunc())");
		
		//lua.runScript("print(obj.child:getArray()[1].n, obj.child:getArray()[2].n, obj.child:getArray()[3].n)");
		
		System.out.print(t.n);
		
		lua.addGlobal("obj", LuaValue.NIL);
	}
}
