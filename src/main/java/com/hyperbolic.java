package com;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

public class hyperbolic {
    public hyperbolic() {
    }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = LuaValue.tableOf();
        library.set("sinh", new sinh());
        library.set("cosh", new cosh());
        env.set("hyperbolic", library);
        return library;
    }

    static class sinh extends OneArgFunction {
        public LuaValue call(LuaValue x) {
            return LuaValue.valueOf(Math.sinh(x.checkdouble()));
        }
    }

    static class cosh extends OneArgFunction {
        public LuaValue call(LuaValue x) {
            return LuaValue.valueOf(Math.cosh(x.checkdouble()));
        }
    }
}
