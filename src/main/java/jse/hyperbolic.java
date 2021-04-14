package jse;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.List;

public class hyperbolic extends TwoArgFunction {
    public hyperbolic() {
    }

    public org.luaj.vm2.LuaValue call(LuaValue modname, org.luaj.vm2.LuaValue env) {
        LuaValue library = LuaValue.tableOf();
        library.set("sinh", new sinh());
        library.set("cosh", new cosh());
        library.set("extract", new extract());
        library.set("extractintfromlist", new extractintfromlist());
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

    static class extract extends OneArgFunction {
        public LuaValue call(LuaValue x) {
            StringBuffer sb = new StringBuffer();
            List sa = (List) x.checkuserdata(List.class);
            sa.forEach(o -> {
                sb.append(o.toString()).append(" ");
            });
            return LuaValue.valueOf(sb.toString());
        }
    }

    static class extractintfromlist extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
            List sa = (List) luaValue.checkuserdata(List.class);
            int index = luaValue1.checkint();
            if (sa.size() <= index) return LuaValue.valueOf(-1);
            return LuaValue.valueOf((Integer) sa.get(index));
        }
    }
}
