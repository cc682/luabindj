# luabindj #

luabindj is a simple,easy to use lua binding in java.

# Quick sample: #

    package org.netroc.luabindj;
    
    import org.netroc.luabindj.LuaBindJ;
    import org.netroc.luabindj.LuaExport;
    
    import junit.framework.Test;
    import junit.framework.TestCase;
    import junit.framework.TestSuite;
    
    public class AppTest 
    extends TestCase
    {
    	static {
    		LuaBindJ.registerClass(AppTest.class, "AppTest");
    	}
    	
    	//Vars export to lua
    	@LuaExport
    	private int nVar1 = 10;
    	
    	@LuaExport
    	private int nVar2 = 20;
    	
    	@LuaExport
    	private int Var1PlusVar2() {
    		return nVar1 + nVar2;
    	}
    	
    	public AppTest( String testName )
    	{
    		super( testName );
    	}
    	
    	
    	public static Test suite()
    	{
    		return new TestSuite( AppTest.class );
    	}
    	
    	public void testApp()
    	{
    		LuaBindJ lua = new LuaBindJ();
    		lua.open();
    		lua.openLibs();
    		lua.addGlobal("Test", this);
    		
    		String strScript = 
    			"function luaFunction(a,b)\n" +
    			"print('call luaFunction:', a, b)" +
    			"return a*b\n" +
    			"end\n" +
    			"local sum = Test.nVar1 + Test.nVar2\n" +
    			"print('sum=' .. sum)\n" +
    			"local sum2 = Test:Var1PlusVar2()\n" +
    			"print('sum2=' .. sum2)\n" +
    			"return sum == sum2";
    	
	    	boolean blResult = lua.runScript(strScript).toboolean();
	    	int data = (Integer)lua.call("luaFunction", 14, 20);
	    	
	        assertTrue( blResult );
	        assertEquals(data, 14 * 20);
    	}
    }


License

luabindj is distributed with an MIT License. 