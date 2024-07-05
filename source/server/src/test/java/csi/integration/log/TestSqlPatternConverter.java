package csi.integration.log;

/**
 * Tests the pattern converter methods.
 */
public class TestSqlPatternConverter {
//
//    /**
//     * Success flow: parse method should replace the codes like %d, %t, %X{MDC_variable} with ? and for each question mark, a column should be found in the columns list. }
//     */
//    @Test
//    public void testParseSuccessFlow() {
//        String pattern = "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (%d,%p,%t,%l, %X{application_id},%X{server_ip_address},%X{action_uri},%X{client_ip_address},%X{user_name},%X{session_id},%m)";
//        SQLPatternConverter converter = new SQLPatternConverter(pattern);
//        converter.parse();
//        assertEquals(
//                converter.getSql(),
//                "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (?,?,?,?, ?,?,?,?,?,?,?)");
//        assertTrue(converter.getColumns() != null);
//        assertEquals(converter.getColumns().size(), 11);
//        assertTrue(converter.getColumns().get(0) instanceof DateLogColumn);
//        assertTrue(converter.getColumns().get(1) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(2) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(3) instanceof LocationLogColumn);
//        assertTrue(converter.getColumns().get(4) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(5) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(6) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(7) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(8) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(9) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(10) instanceof StringLogColumn);
//    }
//
//    @Test
//    public void testParseEscapePercent() {
//        String pattern = "INSERT INTO %%aa (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (%d,%p,%t,%l, %X{application_id},%X{server_ip_address},%X{action_uri},%X{client_ip_address},%X{user_name},%X{session_id},%m)";
//        SQLPatternConverter converter = new SQLPatternConverter(pattern);
//        converter.parse();
//        assertEquals(
//                converter.getSql(),
//                "INSERT INTO %aa (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (?,?,?,?, ?,?,?,?,?,?,?)");
//        assertTrue(converter.getColumns() != null);
//        assertEquals(converter.getColumns().size(), 11);
//        assertTrue(converter.getColumns().get(0) instanceof DateLogColumn);
//        assertTrue(converter.getColumns().get(1) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(2) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(3) instanceof LocationLogColumn);
//        assertTrue(converter.getColumns().get(4) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(5) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(6) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(7) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(8) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(9) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(10) instanceof StringLogColumn);
//    }
//
//    @Test
//    public void testParseWrongCharacter() {
//        String pattern = "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (%z,%p,%t,%l, %X{application_id},%X{server_ip_address},%X{action_uri},%X{client_ip_address},%X{user_name},%X{session_id},%m)";
//        SQLPatternConverter converter = new SQLPatternConverter(pattern);
//        converter.parse();
//        assertEquals(
//                converter.getSql(),
//                "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (?,?,?,?, ?,?,?,?,?,?,?)");
//        assertTrue(converter.getColumns() != null);
//        assertEquals(converter.getColumns().size(), 11);
//        assertTrue(converter.getColumns().get(0) == null);
//        assertTrue(converter.getColumns().get(1) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(2) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(3) instanceof LocationLogColumn);
//        assertTrue(converter.getColumns().get(4) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(5) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(6) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(7) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(8) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(9) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(10) instanceof StringLogColumn);
//    }
//
//    @Test
//    public void testParseIgnoreFormat() {
//        String pattern = "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (%-5d,%p,%t,%l, %X{application_id},%X{server_ip_address},%X{action_uri},%X{client_ip_address},%X{user_name},%X{session_id},%m)";
//        SQLPatternConverter converter = new SQLPatternConverter(pattern);
//        converter.parse();
//        assertEquals(
//                "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (?,?,?,?, ?,?,?,?,?,?,?)",
//                converter.getSql());
//        assertTrue(converter.getColumns() != null);
//        assertEquals(converter.getColumns().size(), 11);
//        assertTrue(converter.getColumns().get(0) instanceof DateLogColumn);
//        assertTrue(converter.getColumns().get(1) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(2) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(3) instanceof LocationLogColumn);
//        assertTrue(converter.getColumns().get(4) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(5) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(6) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(7) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(8) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(9) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(10) instanceof StringLogColumn);
//    }
//
//    @Test
//    public void testParseIgnorePrecision() {
//        String pattern = "INSERT INTO logs (location_info,thread_name,message) VALUES (%c{2},%C{2})";
//        SQLPatternConverter converter = new SQLPatternConverter(pattern);
//        converter.parse();
//        assertEquals("INSERT INTO logs (location_info,thread_name,message) VALUES (?,?)", converter.getSql());
//        assertTrue(converter.getColumns() != null);
//        assertEquals(converter.getColumns().size(), 2);
//        assertTrue(converter.getColumns().get(0) instanceof CategoryLogColumn);
//        assertTrue(converter.getColumns().get(1) instanceof ClassNameLogColumn);
//    }
//
//    @Test
//    public void testParseMessageFormat() {
//        String pattern = "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (%-5d,%p,%t,%l, %X{application_id},%X{server_ip_address},%X{action_uri},%X{client_ip_address},%X{user_name},%X{session_id},%.30m)";
//        SQLPatternConverter converter = new SQLPatternConverter(pattern);
//        converter.parse();
//        assertEquals(
//                "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (?,?,?,?, ?,?,?,?,?,?,?)",
//                converter.getSql());
//        assertTrue(converter.getColumns() != null);
//        assertEquals(converter.getColumns().size(), 11);
//        assertTrue(converter.getColumns().get(0) instanceof DateLogColumn);
//        assertTrue(converter.getColumns().get(1) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(2) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(3) instanceof LocationLogColumn);
//        assertTrue(converter.getColumns().get(4) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(5) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(6) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(7) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(8) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(9) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(10) instanceof StringLogColumn);
//        StringLogColumn logColumn = (StringLogColumn) converter.getColumns().get(10);
//        assertTrue(logColumn.getMaxLength() == 30);
//    }
//
//    @Test
//    public void testParseMessageFormatCase2() {
//        String pattern = "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (%-5d,%p,%t,%.60l, %X{application_id},%X-100{server_ip_address},%X{action_uri},%X.50{client_ip_address},%-3000m,%m,%.30m)";
//        SQLPatternConverter converter = new SQLPatternConverter(pattern);
//        converter.parse();
//        assertEquals(
//                "INSERT INTO logs (log_date,priority,thread_name,location_info,application_id,server_ip_address,action_uri,client_ip_address,user_name,session_id,message) VALUES (?,?,?,?, ?,?,?,?,?,?,?)",
//                converter.getSql());
//        assertTrue(converter.getColumns() != null);
//        assertEquals(converter.getColumns().size(), 11);
//        assertTrue(converter.getColumns().get(0) instanceof DateLogColumn);
//        assertTrue(converter.getColumns().get(1) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(2) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(3) instanceof LocationLogColumn);
//        assertTrue(converter.getColumns().get(4) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(5) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(6) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(7) instanceof MDCLogColumn);
//        assertTrue(converter.getColumns().get(8) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(9) instanceof StringLogColumn);
//        assertTrue(converter.getColumns().get(10) instanceof StringLogColumn);
//        LocationLogColumn location1 = (LocationLogColumn) converter.getColumns().get(3);
//        assertTrue(location1.getMaxLength() == 60);
//        MDCLogColumn mdc1 = (MDCLogColumn) converter.getColumns().get(4);
//        verifyMDCField(mdc1, 0, "application_id");
//        MDCLogColumn mdc2 = (MDCLogColumn) converter.getColumns().get(5);
//        verifyMDCField(mdc2, 100, "server_ip_address");
//        MDCLogColumn mdc3 = (MDCLogColumn) converter.getColumns().get(6);
//        verifyMDCField(mdc3, 0, "action_uri");
//        MDCLogColumn mdc4 = (MDCLogColumn) converter.getColumns().get(7);
//        verifyMDCField(mdc4, 50, "client_ip_address");
//        StringLogColumn logColumn = (StringLogColumn) converter.getColumns().get(8);
//        assertTrue(logColumn.getMaxLength() == 3000);
//        StringLogColumn logColumn2 = (StringLogColumn) converter.getColumns().get(9);
//        assertTrue(logColumn2.getMaxLength() == 0);
//        StringLogColumn logColumn3 = (StringLogColumn) converter.getColumns().get(10);
//        assertTrue(logColumn3.getMaxLength() == 30);
//    }
//
//    private void verifyMDCField(MDCLogColumn column, int maxlength, String key) {
//        assertTrue(column.getMaxLength() == maxlength);
//        assertTrue(column.getKey() != null);
//        assertTrue(key.equals(column.getKey()));
//    }
}
