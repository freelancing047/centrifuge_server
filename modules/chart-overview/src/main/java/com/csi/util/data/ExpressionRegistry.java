package com.csi.util.data;

import java.util.*;

import org.apache.empire.data.DataType;
import org.apache.empire.db.DBColumnExpr;
import org.apache.empire.db.expr.column.DBCalcExpr;
import org.apache.empire.db.expr.column.DBFuncExpr;
import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import csi.server.common.util.sql.PostgreSQLDriverExtensions;

/*
 * TODO:
 * - support aliases for functions so that we don't have hard-codings from
 * client e.g. 'year and month' should have aliases for yearAndMonth, yearMonth, etc.
 * 
 * Note: Using registry here so that we can avoid huge switch/if then else blocks.
 * This approach also places foundation for dynamic registration of various functions.
 */

public class ExpressionRegistry
{

    static ExpressionRegistry Registry;

    public synchronized static ExpressionRegistry instance()
    {
        if (Registry == null) {
            Registry = new ExpressionRegistry();
            Registry.bootstrap();
        }

        return Registry;
    }

    static interface Function2<P1, P2, R>
    {
        R apply( P1 p1, P2 p2 );
    }

    static interface Named
    {
        String name();
    }

    /*
     * Instances of this class are responsible for resolving the context of an expression and an array
     * of parameters. The current implementations do not currently enforce type-safe resolution;
     * type-safety is presumed to be handled via configuration.
     */
    static abstract class DBExprResolver implements Named, Function2<DBColumnExpr, Object[], DBColumnExpr>
    {
        protected String name;

        public DBExprResolver(String name) {
            this.name = name;
        }

        public String name()
        {
            return name;
        }

        abstract public DBColumnExpr apply( DBColumnExpr expr, Object[] args );

    }

    protected Logger log;
    protected Map<String, DBExprResolver> lookup;

    protected SetMultimap<String, String> functionsByType;

    public ExpressionRegistry() {
        lookup = new HashMap<String, ExpressionRegistry.DBExprResolver>();
        functionsByType = HashMultimap.create();
        log = Logger.getLogger(ExpressionRegistry.class);
    }

    public DBColumnExpr getExpression( String name, DBColumnExpr expr, Object[] params )
    {
        name = Strings.nullToEmpty(name).toLowerCase();

        if (lookup.containsKey(name) == false) {
            throw new IllegalArgumentException("Function " + name + " does not exist");
        }

        DBExprResolver resolver = lookup.get(name);
        DBColumnExpr resolvedExpr = resolver.apply(expr, params);
        return resolvedExpr;
    }

    public void registerFunction( DBExprResolver resolver )
    {
        lookup.put(resolver.name(), resolver);
    }

    public void registerFunctionType( String typeName, DBExprResolver function )
    {
        functionsByType.put(typeName, function.name());
    }

    public Collection<String> getFunctionsByType( String typeName )
    {
        Set<String> functionNames = functionsByType.get(typeName);
        return functionNames;
    }

    protected void bootstrap()
    {
        StandardExpressions.bootstrap(this);
        NumericExpressions.bootstrap(this);
        StringExpressions.bootstrap(this);
        TemporalExpressions.bootstrap(this);

        // strings....
        String typeName = csi.server.common.DataType.STRING.toString().toLowerCase();
        functionsByType.putAll(typeName, StandardExpressions.getFunctionNames());
        functionsByType.putAll(typeName, StringExpressions.getFunctionNames());

        // numbers...
        typeName = csi.server.common.DataType.INTEGER.toString().toLowerCase();
        functionsByType.putAll(typeName, StandardExpressions.getFunctionNames());
        functionsByType.putAll(typeName, NumericExpressions.getFunctionNames());

        typeName = csi.server.common.DataType.NUMBER.toString().toLowerCase();
        functionsByType.putAll(typeName, StandardExpressions.getFunctionNames());
        functionsByType.putAll(typeName, NumericExpressions.getFunctionNames());

        // temporal...
        // NB: subset of functions relevant to type
        //
        typeName = csi.server.common.DataType.DATE.toString().toLowerCase();
        functionsByType.putAll(typeName, StandardExpressions.getFunctionNames());
        functionsByType.putAll(typeName, TemporalExpressions.getDateFunctions());

        typeName = csi.server.common.DataType.TIME.toString().toLowerCase();
        functionsByType.putAll(typeName, StandardExpressions.getFunctionNames());
        functionsByType.putAll(typeName, TemporalExpressions.getTimeFunctions());

        typeName = csi.server.common.DataType.TIMESTAMP.toString().toLowerCase();
        functionsByType.putAll(typeName, StandardExpressions.getFunctionNames());
        functionsByType.putAll(typeName, TemporalExpressions.getFunctionNames());

    }

    static class StandardExpressions
    {
        static DBExprResolver Count = new DBExprResolver("count") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                return expr.count();
            }
        };

        static DBExprResolver CountDistinct = new DBExprResolver("count distinct") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                return expr.countDistinct();
            }
        };

        static DBExprResolver Min = new DBExprResolver("min") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                return expr.min();
            }
        };
        static DBExprResolver Max = new DBExprResolver("max") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                return expr.max();
            }
        };

        static DBExprResolver[] Functions = { Count, CountDistinct, Min, Max };

        static void bootstrap( ExpressionRegistry registry )
        {
            for (int i = 0; i < Functions.length; i++) {
                registry.registerFunction(Functions[i]);
            }

        }

        static Collection<String> getFunctionNames()
        {
            Collection<String> names = new ArrayList<String>();
            for (int i = 0; i < Functions.length; i++) {
                names.add(Functions[i].name());
            }
            return names;
        }

    }

    static class NumericExpressions
    {
        static DBExprResolver Ceiling = new DBExprResolver("ceiling") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                DBFuncExpr function = new DBFuncExpr(expr, PostgreSQLDriverExtensions.SQL_FUNC_CEILING, args, null,
                        false, DataType.INTEGER);
                return function;
            }
        };

        static DBExprResolver Floor = new DBExprResolver("floor") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                DBFuncExpr function = new DBFuncExpr(expr, PostgreSQLDriverExtensions.SQL_FUNC_FLOOR, args, null,
                        false, DataType.INTEGER);
                return function;
            }
        };

        static DBExprResolver[] Functions = { Ceiling, Floor };

        static void bootstrap( ExpressionRegistry registry )
        {
            for (int i = 0; i < Functions.length; i++) {
                registry.registerFunction(Functions[i]);
            }

        }

        static Collection<String> getFunctionNames()
        {
            Collection<String> names = new ArrayList<String>();
            for (int i = 0; i < Functions.length; i++) {
                names.add(Functions[i].name());
            }
            return names;
        }
    }

    static class StringExpressions
    {

        static DBExprResolver Left = new DBExprResolver("left") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                int count = Integer.parseInt((String) args[0]);
                DBColumnExpr resolved = expr.substring(0, count);
                return resolved;
            }

        };

        static DBExprResolver Right = new DBExprResolver("right") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                int count = Integer.parseInt((String) args[0]);
                DBCalcExpr resolved = expr.length().minus(count).plus(1);
                return resolved;
            }
        };

        static DBExprResolver Length = new DBExprResolver("length") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                expr = expr.trim().length();
                return expr;
            }
        };

        static DBExprResolver Substring = new DBExprResolver("substr") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                int from = Integer.parseInt((String) args[0]);
                int count = Integer.parseInt((String) args[1]);
                expr = expr.substring(from, count);
                return expr;
            }
        };

        static DBExprResolver[] Functions = { Left, Right, Length, Substring };

        public static void bootstrap( ExpressionRegistry registry )
        {
            for (int i = 0; i < Functions.length; i++) {
                registry.registerFunction(Functions[i]);
            }
        }

        static Collection<String> getFunctionNames()
        {

            Collection<String> names = new ArrayList<String>();
            for (int i = 0; i < Functions.length; i++) {
                names.add(Functions[i].name());
            }
            return names;
        }

    }

    static class TemporalExpressions
    {
        static Object[] EmptyArgs = new Object[0];

        static DBExprResolver Day = new DBExprResolver("day") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                // expr = expr.day();
                DBFuncExpr function = new DBFuncExpr(expr, PostgreSQLDriverExtensions.SQL_FUNC_DAY, EmptyArgs, null,
                        false, DataType.INTEGER);
                return function;
            }
        };
        static DBExprResolver Hour = new DBExprResolver("hour") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                DBFuncExpr function = new DBFuncExpr(expr, PostgreSQLDriverExtensions.SQL_FUNC_HOUR, EmptyArgs, null,
                        false, DataType.INTEGER);
                return function;
            }
        };
        static DBExprResolver Minute = new DBExprResolver("minute") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                DBFuncExpr function = new DBFuncExpr(expr, PostgreSQLDriverExtensions.SQL_FUNC_MINUTE, EmptyArgs, null,
                        false, DataType.INTEGER);
                return function;
            }
        };
        static DBExprResolver Date = new DBExprResolver("date") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                DBFuncExpr function = new DBFuncExpr(expr, PostgreSQLDriverExtensions.SQL_FUNC_DATE, EmptyArgs, null,
                        false, DataType.DATE);
                return function;
            }
        };
        static DBExprResolver Month = new DBExprResolver("month") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                // DBColumnExpr month = expr.month();
                DBColumnExpr function = new DBFuncExpr(expr, PostgreSQLDriverExtensions.SQL_FUNC_MONTH, EmptyArgs,
                        null, false, DataType.INTEGER);
                return function;
            }
        };
        static DBExprResolver Year = new DBExprResolver("year") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {

                DBFuncExpr function = new DBFuncExpr(expr, PostgreSQLDriverExtensions.SQL_FUNC_YEAR, EmptyArgs, null,
                        false, DataType.INTEGER);
                return function;
            }
        };
        static DBExprResolver YearMonth = new DBExprResolver("year and month") {

            @Override
            public DBColumnExpr apply( DBColumnExpr expr, Object[] args )
            {
                DBFuncExpr function = new DBFuncExpr(expr, PostgreSQLDriverExtensions.SQL_FUNC_YEARMONTH, EmptyArgs,
                        null, false, DataType.TEXT);
                return function;
            }
        };

        static DBExprResolver[] Functions = { Date, Day, Hour, Minute, Month, Year, YearMonth };

        public static void bootstrap( ExpressionRegistry registry )
        {
            for (int i = 0; i < Functions.length; i++) {
                registry.registerFunction(Functions[i]);
            }
        }

        public static Collection<String> getTimeFunctions()
        {
            DBExprResolver[] timeFunctions = { Hour, Minute };
            Collection<String> names = new ArrayList<String>();
            for (int i = 0; i < timeFunctions.length; i++) {
                names.add(timeFunctions[i].name());
            }
            return names;
        }

        public static Iterable<? extends String> getDateFunctions()
        {
            DBExprResolver[] dateFunctions = { Date, Day, Month, Year, YearMonth };
            Collection<String> names = new ArrayList<String>();
            for (int i = 0; i < dateFunctions.length; i++) {
                names.add(dateFunctions[i].name());
            }
            return names;

        }

        static Collection<String> getFunctionNames()
        {

            Collection<String> names = new ArrayList<String>();
            for (int i = 0; i < Functions.length; i++) {
                names.add(Functions[i].name());
            }
            return names;
        }
    }
}
