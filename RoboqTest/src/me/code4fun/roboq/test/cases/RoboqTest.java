package me.code4fun.roboq.test.cases;


import android.test.InstrumentationTestCase;
import me.code4fun.roboq.Request;
import me.code4fun.roboq.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

import static me.code4fun.roboq.Request.*;

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
        final String BYTES_FN = "file1.txt";
        final String IS_FN = "file2.txt";
        final String FILE_FN = "file3.txt";
        final String TF = "textfield1";

        byte[] bytes = "HELLO".getBytes("UTF-8");
        InputStream is = new ByteArrayInputStream(bytes);
        File file3 = getInstrumentation().getContext().getFileStreamPath(FILE_FN);
        saveBytesToFile(file3, bytes);


        Response resp = new Request(prepared, POST, "/test_multipart_post")
                .with("bytes@", bytes)
                .with("is@", contentBody(is, bytes.length, "text/plain", IS_FN))
                .with("file@", file3)
                .with("tf@", TF)
                .execute();

        JSONObject r = resp.asJsonObject();
        JSONObject bytesObj = r.optJSONObject("bytes");
        JSONObject isObj = r.optJSONObject("is");
        JSONObject fileObj = r.optJSONObject("file");
        String tf = r.optString("tf");

        assertEquals("/test_multipart_post", bytesObj.optString("filename"), "");
        assertEquals("/test_multipart_post", bytesObj.optString("mimetype"), "application/octet-stream");
        assertEquals("/test_multipart_post", bytesObj.optLong("content-length"), bytes.length);

        assertEquals("/test_multipart_post", isObj.optString("filename"), IS_FN);
        assertEquals("/test_multipart_post", isObj.optString("mimetype"), "text/plain");
        assertEquals("/test_multipart_post", isObj.optLong("content-length"), bytes.length);

        assertEquals("/test_multipart_post", fileObj.optString("filename"), FILE_FN);
        assertEquals("/test_multipart_post", fileObj.optString("mimetype"), "application/octet-stream");
        assertEquals("/test_multipart_post", fileObj.optLong("content-length"), file3.length());

        assertEquals("/test_multipart_post", tf, TF);
    }

    public void testFormPost() throws Exception {
        Response resp = new Request(prepared, POST, "/test_form_post")
                .setMultipart(false)
                .with("f1@", "v1")
                .with("f2@", "v2")
                .execute();

        JSONObject r = resp.asJsonObject();
        assertEquals("/test_form_post", r.optString("f1"), "v1");
        assertEquals("/test_form_post", r.optString("f2"), "v2");
    }

    public void testBodyPost() throws Exception {

        final String TEXT = "hello, Roboq!";

        Response resp;

        resp = new Request(prepared, POST, "/test_body_post")
                .with("@", textBody(TEXT, "text/ppp"))
                .with("type=", "text")
                .execute();

        JSONObject r = resp.asJsonObject();
        assertEquals("/test_body_post", "text/ppp", r.optString("mimetype"));
        assertEquals("/test_body_post", TEXT.length(), r.optInt("content-length", 0));
        assertEquals("/test_body_post", TEXT, r.optString("content"));


        resp = new Request(prepared, POST, "/test_body_post")
                .with("@", TEXT.getBytes("UTF-8"))
                .with("type=", "bytes")
                .execute();

        r = resp.asJsonObject();
        assertEquals("/test_body_post", "application/octet-stream", r.optString("mimetype"));
        assertEquals("/test_body_post", TEXT.getBytes("UTF-8").length, r.optInt("content-length", 0));
        assertEquals("/test_body_post", TEXT, r.optString("content"));


        ByteArrayInputStream is = new ByteArrayInputStream(TEXT.getBytes("UTF-8"));
        resp = new Request(prepared, POST, "/test_body_post")
                .with("@", contentBody(is, is.available()))
                .with("type=", "is")
                .execute();

        r = resp.asJsonObject();
        assertEquals("/test_body_post", "application/octet-stream", r.optString("mimetype"));
        assertEquals("/test_body_post", TEXT.getBytes("UTF-8").length, r.optInt("content-length", 0));
        assertEquals("/test_body_post", TEXT, r.optString("content"));


        File f = getInstrumentation().getContext().getFileStreamPath("body_file.txt");
        saveBytesToFile(f, TEXT.getBytes("UTF-8"));

        resp = new Request(prepared, POST, "/test_body_post")
                .with("@", f)
                .with("type=", "file")
                .execute();

        r = resp.asJsonObject();
        assertEquals("/test_body_post", "application/octet-stream", r.optString("mimetype"));
        assertEquals("/test_body_post", TEXT.getBytes("UTF-8").length, r.optInt("content-length", 0));
        assertEquals("/test_body_post", TEXT, r.optString("content"));

    }

    public void testRedirect() throws Exception {
        Response resp = new Request(prepared, GET, "/test_redirect")
                .execute();

        assertEquals("/test_redirect", "hello", resp.asText());

        resp = new Request(prepared, GET, "/test_redirect")
                .setRedirect(false)
                .execute();
        assertEquals("/test_redirect", 302, resp.statusCode());
        assertTrue("/test_redirect", resp.header("Location", "").endsWith("/test_plain_text"));
    }

    public void testHTTPS() throws Exception {
        Response resp = new Request(GET, "https://localhost:24444/test_ssl")
                .execute();

        String text = resp.asText();
        assertEquals("testHTTPS", 200, resp.statusCode());
        assertEquals("testHTTPS", "SSL hello", text);
    }

    public void testDownload() throws Exception {
        File f = getInstrumentation().getContext().getFileStreamPath("download1");
        new Request(prepared, GET, "/static/kk.png")
                .execute().writeFile(f);

        assertEquals("testDownload", 84688L, f.length());
    }

    private static void saveBytesToFile(File file, byte[] bytes) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
    }


}
