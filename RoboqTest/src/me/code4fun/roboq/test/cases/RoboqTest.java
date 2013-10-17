package me.code4fun.roboq.test.cases;


import android.test.InstrumentationTestCase;
import me.code4fun.roboq.Request;
import me.code4fun.roboq.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import static me.code4fun.roboq.Request.GET;

public class RoboqTest extends InstrumentationTestCase {

    // 测试时改为RoboqTestServer运行时的IP:port
    public static final String TEST_HOST = "http://192.168.1.107:23333";

    public static final String UA = "Roboq-Test";

    private Request.Prepared prepared;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        prepared = new Request.Prepared(TEST_HOST,
                        "User-Agent:", UA
                )
                .setModifier(new Request.Modifier() {
                    @Override
                    public Request.Options modifyOptions(Request.Options options) {
                        return options.add("dyna-opt=", "zzz");
                    }
                });
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testResponse() throws Exception {
        Response resp;

        resp = new Request(prepared, GET, "/test_plain_text").execute();
        String text = resp.asText();
        assertEquals("/test_plain_text", text, "hello");


        resp = new Request(prepared, GET, "/test_json_object").execute();
        JSONObject jo1 = resp.asJsonObject();
        assertEquals("/test_json_object", jo1.optString("k1"), "v1");

        resp = new Request(prepared, GET, "/test_json_array").execute();
        JSONArray ja1 = resp.asJsonArray();
        assertEquals("/test_json_array", ja1.length(), 2);
        assertEquals("/test_json_array", ja1.optJSONObject(0).optString("k1"), "v1a");
        assertEquals("/test_json_array", ja1.optJSONObject(1).optString("k1"), "v1b");
    }

    public void testOptions() throws Exception {
        String pv1 = "pv1";
        String pv2 = "pv2";
        String p1 = "p1";
        String h1 = "h1";

        Response resp = new Request(prepared, GET, "/test_options/${pv1}/${pv2}",
                "pv1   $", pv1,
                "pv2   $", pv2,
                "p1    =", p1,
                "X-H1  :", h1
        ).execute();
        JSONObject jo = resp.asJsonObject();
        assertEquals("/test_options", pv1, jo.opt("pv1"));
        assertEquals("/test_options", pv2, jo.opt("pv2"));
        assertEquals("/test_options", p1, jo.opt("p1"));
        assertEquals("/test_options", h1, jo.opt("h1"));
        assertEquals("/test_options", UA, jo.opt("ua"));
        assertEquals("/test_options", "zzz", jo.opt("dynaopt1"));
    }

    public void testMultipartPost() throws Exception {
//        Response resp = new Request(Request.POST, "http://192.168.1.107:23333/test_multipart_post")
//                .setMultipart(true)
//                .with("f1@", new Request.Value("HELLO WORLD!".getBytes("UTF-8"), "text/plain", "test.txt"))
//                .with("f2@", "FIELD2")
//                .execute();
//        String text = resp.asText();
//        assertEquals("Multipart post", text, "test.txt");

    }
}
